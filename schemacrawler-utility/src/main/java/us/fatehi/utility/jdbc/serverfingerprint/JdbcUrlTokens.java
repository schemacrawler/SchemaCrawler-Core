/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.jdbc.serverfingerprint;

import static us.fatehi.utility.Utility.isBlank;
import static us.fatehi.utility.Utility.trimToEmpty;

/**
 * The structured result of tokenizing a JDBC connection URL, as produced by {@link
 * JdbcUrlTokenizer#tokenize(String)}.
 *
 * <p>Blank/{@code null} input is normalized to {@code ""} for all three string components by the
 * compact constructor, so callers never need to null-check these fields themselves. However, only
 * {@code databaseSystemIdentifier} is also case-normalized (lower-cased) here, since it is an
 * internal driver-prefix token, not user-facing data. {@code host} and {@code databaseName} are
 * reported <b>exactly as they appear in the URL</b> - actual case, unmasked - because this record
 * is package-private and consumed only by {@link DatabaseServerFingerprintBuilder}, which is
 * responsible for applying any masking (of privately-scoped hosts) or case normalization (of public
 * hostnames and database names) when it builds a fingerprint. Keeping this record free of that
 * policy means it always reflects the URL's literal contents, which is easier to reason about and
 * test.
 *
 * @param databaseSystemIdentifier the database system/driver identifier (e.g. {@code "mysql"},
 *     {@code "postgresql"}), taken from the {@code jdbc:<identifier>:...} prefix, lower-cased.
 *     Blank if the input was not a well-formed {@code jdbc:} URL.
 * @param host the primary host, reported verbatim (actual case, unmasked). This is either: (a) a
 *     real hostname/address exactly as written in the URL, or (b) a literal embedded-database mode
 *     token (e.g. {@code "mem"}, {@code "file"}, {@code "res"}, or SQLite's {@code ":memory:"}),
 *     even though such tokens usually carry a {@code LOCALHOST} or {@code INTERNAL} classification.
 *     Blank if no host or mode token was present. Callers needing a masked and/or lower-cased value
 *     for hashing or display should derive it from this field and {@code hostClassification}.
 * @param databaseName the database/schema name, reported verbatim (actual case), or the best
 *     available fallback (a semicolon-property value, a mode token's remainder, or an unparsed
 *     local path). Blank if none was present. Callers needing a lower-cased value for hashing
 *     should derive it from this field.
 * @param hostClassification the network reachability classification of {@code host}; see {@link
 *     HostClassification}. Never {@code null} - defaults to {@code UNKNOWN} when no host
 *     information is available at all.
 * @param port the primary host's port number, if one was present and parsed as a valid integer;
 *     {@code null} otherwise (no port specified, a non-numeric port segment, or a host form - such
 *     as an embedded-database mode token - that has no notion of a port). This field is
 *     deliberately <b>not</b> used by {@link DatabaseServerFingerprintBuilder} for hashing or
 *     confidence scoring: two connections to the same logical server on different ports are still
 *     considered the same server identity for fingerprinting purposes.
 */
record JdbcUrlTokens(
    String databaseSystemIdentifier,
    String host,
    Integer port,
    String databaseName,
    HostClassification hostClassification) {

  JdbcUrlTokens {
    databaseSystemIdentifier = normalizeIdentifier(databaseSystemIdentifier);
    host = trimToEmpty(host);
    databaseName = trimToEmpty(databaseName);
    if (hostClassification == null) {
      hostClassification = HostClassification.UNKNOWN;
    }
  }

  JdbcUrlTokens() {
    this(null, null, null, null, null);
  }

  boolean hasDatabaseName() {
    return !isBlank(databaseName);
  }

  boolean hasDatabaseSystemIdentifier() {
    return !isBlank(databaseSystemIdentifier);
  }

  boolean hasPort() {
    return port != null;
  }

  boolean hasHost() {
    return !isBlank(host);
  }

  boolean hasPublicHost() {
    return hasHost() && hostClassification == HostClassification.PUBLIC;
  }

  /**
   * Normalizes the database system identifier: trims/blank-normalizes and lower-cases it, since it
   * is an internal driver-prefix token (e.g. {@code "mysql"}) rather than user-facing host or
   * database data.
   */
  private static String normalizeIdentifier(final String value) {
    return trimToEmpty(value).toLowerCase();
  }
}
