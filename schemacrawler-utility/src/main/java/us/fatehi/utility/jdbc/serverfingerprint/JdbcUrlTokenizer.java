/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.jdbc.serverfingerprint;

import static java.net.InetAddress.getByName;
import static us.fatehi.utility.Utility.isBlank;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;
import us.fatehi.utility.UtilityMarker;

/**
 * Tokenizes a JDBC connection URL into a {@link JdbcUrlTokens} structured result (database system
 * identifier, primary host, primary host's port, database name, and a host network-reachability
 * classification), for use by {@link DatabaseServerFingerprintBuilder}.
 *
 * <h2>Overall shape</h2>
 *
 * <p>{@link #tokenize(String)} works in stages:
 *
 * <ol>
 *   <li>Reject blank input or input not starting with {@code jdbc:} (returns an empty {@link
 *       JdbcUrlTokens}).
 *   <li>Extract the database system identifier - the token between {@code jdbc:} and the next
 *       {@code :}.
 *   <li>If that identifier is a known spy/proxy driver prefix ({@code p6spy}, {@code log4jdbc}),
 *       unwrap the inner URL it wraps and recurse - see {@link #isSpyPrefix(String)}.
 *   <li>Strip any {@code ?query} or {@code #fragment} suffix.
 *   <li>Normalize Oracle's {@code @//host:port/service} full authority form so it is recognized as
 *       a standard {@code //host:port/...} authority.
 *   <li>Branch on the remaining body's shape - see {@link #tokenize(String)}'s inline branch
 *       documentation for Branch A (authority form), Branch B (non-authority, semicolon
 *       properties), and Branch C (non-authority, no properties).
 * </ol>
 *
 * <h2>Design principles</h2>
 *
 * <ul>
 *   <li><b>No fixed database enumeration.</b> The database system identifier and any embedded-mode
 *       token (see below) are recognized purely by their position/shape in the URL, not by matching
 *       against a list of known driver names. This keeps the tokenizer open to arbitrary and future
 *       JDBC drivers.
 *   <li><b>Primary host only.</b> When a URL lists multiple hosts (comma-separated, for
 *       failover/clustering), only the first is parsed; later hosts are ignored. See {@link
 *       #parseHostPort(String)}.
 *   <li><b>Mode-token pseudo-hosts.</b> Embedded-database drivers (H2, HSQLDB, SQLite, and any
 *       future driver following the same convention) often prefix their body with a short
 *       alphabetic "mode" token before a colon, such as {@code mem:}, {@code file:}, {@code res:}.
 *       This tokenizer treats that token as a pseudo-host: it is reported verbatim as {@code host}
 *       and given a classification that reflects what the mode implies (memory-based modes are
 *       {@code LOCALHOST}, file/resource modes are {@code INTERNAL} or {@code LOCALHOST} per the
 *       driver's convention). See {@link #resolveEmbeddedOrHost(String)}.
 *   <li><b>No masking or case normalization here.</b> This class returns {@code host} and {@code
 *       databaseName} exactly as they appear in the URL - no lower-casing, no substitution of a
 *       masked placeholder for locally- or privately-scoped hosts. Callers that need a masked or
 *       case-normalized value for hashing or display (such as {@link
 *       DatabaseServerFingerprintBuilder}) are responsible for applying that transformation
 *       themselves, based on the reported {@link HostClassification}. (The database system
 *       identifier is the one exception: it is still lower-cased here, since it is an internal
 *       driver-prefix token rather than user-facing host or database data.)
 *   <li><b>Oracle TNS descriptors are out of scope.</b> Parenthesized {@code (DESCRIPTION=...)}
 *       connection strings are never decomposed into host/port; this is intentional and permanent.
 * </ul>
 */
@UtilityMarker
final class JdbcUrlTokenizer {

  /** The result of parsing an authority-form URL body ({@code host[:port]/db[;options]}). */
  private record ParsedAuthority(
      String host,
      Integer port,
      String databaseName,
      String options,
      HostClassification hostClassification) {}

  /** The result of parsing a single {@code host[:port]} token (the primary host only). */
  private record ParsedHostPort(String host, Integer port, HostClassification hostClassification) {}

  /**
   * The result of resolving a non-authority URL body (or the pre-semicolon "head" of one) that may
   * be a real {@code host:port} pair, an embedded-database mode-token pseudo-host, or a plain local
   * path with no host information at all. See {@link #resolveEmbeddedOrHost(String)}.
   *
   * @param host the host value, reported exactly as it appears in the URL (a real hostname/IP
   *     literal, a mode-token pseudo-host such as {@code "mem"}, or blank) - never masked or
   *     case-normalized
   * @param port the port number, if this turned out to be a real {@code host:port} pair; {@code
   *     null} otherwise (mode tokens and plain paths never carry a port)
   * @param databaseNameFallback the best available database-name value if no semicolon-property
   *     override is found by the caller: the mode token's remainder, the unsplit {@code host:port}
   *     text (for non-authority semicolon forms that fall back to the head token), or the full
   *     local path
   * @param hostClassification the reachability classification for {@code host}
   */
  private record EmbeddedOrHost(
      String host,
      Integer port,
      String databaseNameFallback,
      HostClassification hostClassification) {}

  // Known spy JDBC driver subprotocol prefixes. These prefixes wrap an inner JDBC
  // URL and delegate connection work to another driver. Add new spy drivers here.
  // We cannot make this detection generic, since drivers like HSQLDB support
  // sub-protocols - for example, jdbc:hsqldb:mysql://localhost:9001/schemacrawler
  private static final Set<String> SPY_PREFIXES = Set.of("p6spy", "log4jdbc");

  // Well-known embedded-database mode tokens and the classification each implies.
  // Any mode token not listed here still gets recognized (see resolveEmbeddedOrHost),
  // falling back to classifyHost() so unrecognized future tokens still receive a
  // reasonable classification instead of silently defaulting to PUBLIC.
  private static final Set<String> MEMORY_MODE_TOKENS = Set.of("mem", "memory");
  private static final Set<String> INTERNAL_MODE_TOKENS = Set.of("file");
  private static final Set<String> LOCALHOST_MODE_TOKENS = Set.of("res");

  static JdbcUrlTokens tokenize(final String url) {
    if (isBlank(url)) {
      return new JdbcUrlTokens();
    }
    final String jdbc = url.trim();
    if (!jdbc.startsWith("jdbc:")) {
      return new JdbcUrlTokens();
    }

    final int subprotocolEnd = jdbc.indexOf(':', "jdbc:".length());
    if (subprotocolEnd < 0) {
      // e.g. "jdbc:mysql" - a driver identifier with no further body at all.
      return new JdbcUrlTokens(
          jdbc.substring("jdbc:".length()), null, null, null, HostClassification.UNKNOWN);
    }

    final String databaseServerType = jdbc.substring("jdbc:".length(), subprotocolEnd);

    // Spy drivers wrap the original JDBC URL; unwrap to identify the real database
    // system.
    // Examples:
    // - P6Spy: jdbc:p6spy:mysql://host/db → inner URL: jdbc:mysql://host/db
    // - log4jdbc: jdbc:log4jdbc:mysql://host/db → inner URL: jdbc:mysql://host/db
    // The SPY_PREFIXES set enumerates known spy driver subprotocols.
    if (isSpyPrefix(databaseServerType)) {
      final String innerUrl = unwrapSpyUrl(jdbc.substring(subprotocolEnd + 1));
      if (!isBlank(innerUrl)) {
        return tokenize(innerUrl);
      }
    }

    String body = stripQueryAndFragment(jdbc.substring(subprotocolEnd + 1));
    body = normalizeOracleAuthorityMarker(body);
    if (!hasAuthorityPrefix(body) && body.contains("://")) {
      body = body.substring(body.indexOf("://") + 1);
    }

    final boolean hasAuthority = hasAuthorityPrefix(body);
    final String normalizedBody = stripAuthorityPrefix(body);

    Integer port = null;
    String host;
    String databaseName;
    HostClassification hostClassification;

    if (hasAuthority) {
      // Branch A: authority form, host[,host2,...][:port]/db, optionally with
      // semicolon properties. Only the first host in a comma-separated list is
      // used; subsequent hosts are ignored (see parseHostPort). Database names are
      // preserved in full, including any additional "/" separators (e.g. H2's
      // home-relative "~/production" syntax) - the split against the host already
      // isolated the database-name remainder, so it is not re-truncated.
      // Examples:
      // - jdbc:mysql://db.example.com:3306/appdb
      // - jdbc:postgresql://host1:5432,host2:5433,host3:5434/mydb
      // - jdbc:sqlserver://sqlhost:1433;databaseName=Sales
      // - jdbc:oracle:thin:@//oracledb:1521/ORCLPDB1
      // - jdbc:h2:tcp://localhost:9092/~/testdb
      final ParsedAuthority parsed = parseAuthority(normalizedBody);
      host = parsed.host();
      port = parsed.port();
      hostClassification = parsed.hostClassification();
      databaseName = parsed.databaseName();
      if (isBlank(databaseName)) {
        databaseName = databaseNameFromOptions(parsed.options());
      }
    } else if (normalizedBody.contains(";")) {
      // Branch B: non-authority form with semicolon-delimited properties. The
      // portion before the first ";" ("head") is resolved the same way as Branch
      // C's body (see resolveEmbeddedOrHost): it may be a real host[:port] pair, an
      // embedded-database mode-token pseudo-host, or a plain local path. The
      // database name prefers a recognized property key (databaseName/database/db)
      // and otherwise falls back to whatever resolveEmbeddedOrHost reports.
      // Examples:
      // - jdbc:sqlserver:sqlhost:1433;databaseName=Sales (host:port, db from property)
      // - jdbc:sqlserver:sqlhost:1433;encrypt=true (host:port, db falls back to head)
      // - jdbc:hsqldb:file:testdb;shutdown=true;hsqldb.tx=mvcc (mode token "file")
      // - jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL (mode token "mem")
      // - jdbc:h2:~/testdb;MODE=MySQL (plain local path, no host at all)
      final String[] headAndOptions = normalizedBody.split(";", 2);
      final String head = headAndOptions[0];
      final String options = headAndOptions.length > 1 ? headAndOptions[1] : "";
      final EmbeddedOrHost resolved = resolveEmbeddedOrHost(head);
      host = resolved.host();
      port = resolved.port();
      hostClassification = resolved.hostClassification();
      databaseName = databaseNameFromOptions(options);
      if (isBlank(databaseName)) {
        databaseName = resolved.databaseNameFallback();
      }
    } else {
      // Branch C: path-only/local forms - no host authority and no semicolon
      // properties. Resolved identically to Branch B's head token (see
      // resolveEmbeddedOrHost): a real host:port pair, an embedded-database
      // mode-token pseudo-host (including SQLite's special ":memory:"/"memory:"
      // form), or a plain local path/file reference with no host at all.
      // Examples:
      // - jdbc:sqlite:/data/databases/mydb.db (plain local path)
      // - jdbc:sqlite::memory: (SQLite in-memory special case)
      // - jdbc:h2:mem:testdb (mode token "mem")
      // - jdbc:hsqldb:res:/org/mydatabase/mydb (mode token "res")
      // - jdbc:oracle:thin:@oracledb:1521/ORCL (Oracle short-form carve-out)
      final EmbeddedOrHost resolved = resolveEmbeddedOrHost(normalizedBody);
      host = resolved.host();
      port = resolved.port();
      hostClassification = resolved.hostClassification();
      databaseName = resolved.databaseNameFallback();
    }

    return new JdbcUrlTokens(databaseServerType, host, port, databaseName, hostClassification);
  }

  private static HostClassification classifyHost(final String host) {
    if (isBlank(host)) {
      return HostClassification.UNKNOWN;
    }
    final String normalizedHost = host.strip().toLowerCase();
    if ("localhost".equals(normalizedHost)
        || normalizedHost.endsWith(".local")
        || normalizedHost.endsWith(".lan")) {
      return HostClassification.LOCALHOST;
    }
    if (normalizedHost.endsWith(".corp") || normalizedHost.endsWith(".internal")) {
      return HostClassification.INTERNAL;
    }
    final InetAddress hostAddress = hostAddress(normalizedHost);
    if (hostAddress == null) {
      return HostClassification.PUBLIC;
    }
    if (hostAddress.isLoopbackAddress()) {
      return HostClassification.LOCALHOST;
    }
    if (hostAddress.isAnyLocalAddress()
        || hostAddress.isLinkLocalAddress()
        || hostAddress.isSiteLocalAddress()) {
      return HostClassification.INTERNAL;
    }
    if (hostAddress.getAddress().length == 16) {
      final byte[] address = hostAddress.getAddress();
      if ((address[0] & 0xFE) == 0xFC) {
        return HostClassification.INTERNAL;
      }
    }
    return HostClassification.PUBLIC;
  }

  private static String databaseNameFromOptions(final String options) {
    String databaseName = optionValue(options, "databaseName");
    if (isBlank(databaseName)) {
      databaseName = optionValue(options, "database");
    }
    if (isBlank(databaseName)) {
      databaseName = optionValue(options, "db");
    }
    return databaseName;
  }

  private static String firstToken(final String value) {
    if (isBlank(value)) {
      return null;
    }
    final String tokenized = value.trim();
    final int slash = tokenized.indexOf('/');
    return slash > -1 ? tokenized.substring(0, slash) : tokenized;
  }

  private static boolean hasAuthorityPrefix(final String value) {
    return value.startsWith("//") || value.startsWith("@");
  }

  private static InetAddress hostAddress(final String host) {
    if (isBlank(host)) {
      return null;
    }
    final String normalizedHost = host.strip();
    if (!looksLikeIpLiteral(normalizedHost)) {
      return null;
    }
    try {
      return getByName(normalizedHost);
    } catch (final UnknownHostException e) {
      return null;
    }
  }

  private static boolean isSpyPrefix(final String subprotocol) {
    if (isBlank(subprotocol)) {
      return false;
    }
    return SPY_PREFIXES.contains(subprotocol.toLowerCase());
  }

  private static boolean looksLikeIpLiteral(final String host) {
    return host.matches("\\d{1,3}(?:\\.\\d{1,3}){3}")
        || host.matches("\\[[0-9a-fA-F:]+\\]")
        || host.contains(":");
  }

  /**
   * Classifies a recognized embedded-database mode token (e.g. {@code mem}, {@code file}, {@code
   * res}). Well-known tokens get an explicit classification reflecting their nature (memory-based
   * modes are {@code LOCALHOST}; file/resource modes are {@code INTERNAL} or {@code LOCALHOST} per
   * the driver's convention). Any other token falls back to {@link #classifyHost(String)}, so an
   * unrecognized future mode token still gets a reasonable classification instead of silently
   * defaulting to {@code PUBLIC}.
   */
  private static HostClassification modeClassification(final String token) {
    final String normalized = token.toLowerCase();
    if (MEMORY_MODE_TOKENS.contains(normalized)) {
      return HostClassification.LOCALHOST;
    }
    if (INTERNAL_MODE_TOKENS.contains(normalized)) {
      return HostClassification.INTERNAL;
    }
    if (LOCALHOST_MODE_TOKENS.contains(normalized)) {
      return HostClassification.LOCALHOST;
    }
    return classifyHost(token);
  }

  /**
   * Resolves a non-authority URL body - or, for Branch B, the portion of it preceding the first
   * {@code ;} - into one of three shapes, checked in order:
   *
   * <ol>
   *   <li><b>SQLite's special in-memory form</b>: the body is exactly {@code ":memory:"} or {@code
   *       "memory:"}. Reported verbatim as {@code host} (so e.g. {@code ":memory:"} stays {@code
   *       ":memory:"}), classified {@code LOCALHOST}, with a blank database name.
   *   <li><b>A real {@code host:port} pair</b>: the body contains a colon, and the text after that
   *       colon parses as a valid integer port (e.g. {@code sqlhost:1433}). The host is classified
   *       (see {@link #classifyHost(String)}) and reported verbatim, unmasked - masking is applied
   *       only when a fingerprint is built, not during tokenization - and the unsplit {@code
   *       host:port} text is offered as a database-name fallback for callers with no recognized
   *       database property (this intentionally preserves the pre-redesign fallback behavior for
   *       SQL Server-style URLs). An Oracle {@code thin:@host:port/service} short form is
   *       explicitly carved out ahead of this check and handled separately, since TNS/service-name
   *       parsing is out of scope.
   *   <li><b>An embedded-database mode-token pseudo-host</b>: the body contains a colon, the token
   *       before it is 2+ non-numeric characters (excluding Windows drive letters such as {@code
   *       C:}, which are always exactly one character), and the text after the colon is not a valid
   *       port number (e.g. {@code mem:testdb}, {@code file:/data/db}, {@code res:/org/db}). The
   *       token becomes {@code host}, reported verbatim (see {@link #modeClassification(String)}),
   *       and the remainder becomes the database-name fallback.
   * </ol>
   *
   * <p>If none of the above match, the body is a <b>plain local path or file reference</b> with no
   * host information at all (e.g. {@code /opt/databases/mydb}, {@code ~/test}, {@code test.db},
   * {@code C:\databases\test.db}): {@code host} is blank, the full body is the database-name
   * fallback (never truncated), and the classification is {@code INTERNAL} if the body is
   * non-blank, or {@code UNKNOWN} if it is blank (e.g. {@code jdbc:sqlite:} with an empty body).
   */
  private static EmbeddedOrHost resolveEmbeddedOrHost(final String value) {
    if (isBlank(value)) {
      return new EmbeddedOrHost("", null, "", HostClassification.UNKNOWN);
    }
    final String trimmed = value.trim();

    if (":memory:".equals(trimmed) || "memory:".equals(trimmed)) {
      return new EmbeddedOrHost(trimmed, null, "", HostClassification.LOCALHOST);
    }

    final int colon = trimmed.indexOf(':');
    if (colon > 0) {
      final String token = trimmed.substring(0, colon);
      final String rest = trimmed.substring(colon + 1);

      // Oracle's "thin:@host:port/service" short form: TNS/service-name parsing is
      // out of scope, so fall back to the legacy single-truncation behavior
      // (host blank, database name truncated at the first "/") rather than
      // treating "thin" as a mode token or "@oracledb:1521" as a real host.
      if (rest.startsWith("@")) {
        return new EmbeddedOrHost("", null, firstToken(trimmed), HostClassification.UNKNOWN);
      }

      final Integer restAsPort = parsePort(rest);
      if (restAsPort != null) {
        final HostClassification classification = classifyHost(token);
        return new EmbeddedOrHost(token, restAsPort, trimmed, classification);
      }

      final boolean isModeToken =
          token.length() >= 2 && !token.chars().allMatch(Character::isDigit);
      if (isModeToken) {
        return new EmbeddedOrHost(token, null, rest, modeClassification(token));
      }
    }

    return new EmbeddedOrHost(
        "",
        null,
        trimmed,
        isBlank(trimmed) ? HostClassification.UNKNOWN : HostClassification.INTERNAL);
  }

  private static String normalizeOracleAuthorityMarker(final String value) {
    // Oracle thin URLs commonly embed "@//" after the driver token:
    // jdbc:oracle:thin:@//host:1521/service
    final int authorityMarker = value.indexOf("@//");
    if (authorityMarker > -1) {
      return value.substring(authorityMarker + 1);
    }
    return value;
  }

  private static String optionValue(final String value, final String optionName) {
    if (isBlank(value)) {
      return null;
    }
    final String[] options = value.split(";");
    for (final String option : options) {
      final int equals = option.indexOf('=');
      if (equals > 0) {
        final String key = option.substring(0, equals).trim();
        if (optionName.equalsIgnoreCase(key)) {
          return option.substring(equals + 1).trim();
        }
      }
    }
    return null;
  }

  private static ParsedAuthority parseAuthority(final String authorityAndPath) {
    final String[] hostAndOptions = authorityAndPath.split(";", 2);
    final String hostAndDb = hostAndOptions[0];
    final String options = hostAndOptions.length > 1 ? hostAndOptions[1] : "";

    // Split host(s) from the database-name remainder at the first "/" only. The
    // remainder is used as-is (not re-tokenized) so that further "/" separators
    // within it - such as H2's home-relative "~/production" syntax - are preserved
    // in full rather than being truncated a second time.
    final String[] parts = hostAndDb.split("/", 2);
    final ParsedHostPort parsedHostPort = parseHostPort(parts[0]);
    final String databaseName = parts.length > 1 ? parts[1] : null;
    return new ParsedAuthority(
        parsedHostPort.host(),
        parsedHostPort.port(),
        databaseName,
        options,
        parsedHostPort.hostClassification());
  }

  /**
   * Parses a single {@code host[:port]} token, which may itself be the first of several
   * comma-separated hosts (failover/clustering syntax such as {@code
   * host1:5432,host2:5433,host3:5434}). Only the first host is parsed; later hosts are discarded,
   * per the "primary host only" design principle.
   */
  private static ParsedHostPort parseHostPort(final String hostPort) {
    if (isBlank(hostPort)) {
      return new ParsedHostPort(null, null, HostClassification.UNKNOWN);
    }
    String value = hostPort.trim();
    if (value.contains(",")) {
      value = value.split(",", 2)[0].trim();
    }

    if (value.startsWith("[")) {
      final int end = value.indexOf(']');
      if (end > 0) {
        final String host = value.substring(1, end);
        Integer port = null;
        if (end + 2 <= value.length() && value.charAt(end + 1) == ':') {
          port = parsePort(value.substring(end + 2));
        }
        return new ParsedHostPort(host, port, classifyHost(host));
      }
    }

    final int firstColon = value.indexOf(':');
    final int lastColon = value.lastIndexOf(':');
    if (firstColon > 0 && firstColon == lastColon) {
      final String host = value.substring(0, firstColon);
      return new ParsedHostPort(
          host, parsePort(value.substring(firstColon + 1)), classifyHost(host));
    }
    return new ParsedHostPort(value, null, classifyHost(value));
  }

  private static Integer parsePort(final String value) {
    if (isBlank(value)) {
      return null;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (final NumberFormatException e) {
      return null;
    }
  }

  private static String stripAuthorityPrefix(final String value) {
    if (value.startsWith("//")) {
      return value.substring(2);
    }
    if (value.startsWith("@")) {
      return value.substring(1);
    }
    return value;
  }

  private static String stripQueryAndFragment(final String value) {
    return value.split("[?#]", 2)[0];
  }

  private static String unwrapSpyUrl(final String remainder) {
    // Both P6Spy and log4jdbc use the form: jdbc:<spy>:<inner-subprotocol>:<rest>
    // where the inner part, prefixed with "jdbc:", is a complete JDBC URL.
    if (isBlank(remainder)) {
      return null;
    }
    final String inner = remainder.trim();
    return inner.startsWith("jdbc:") ? inner : "jdbc:" + inner;
  }

  private JdbcUrlTokenizer() {
    // Prevent instantiation
  }
}
