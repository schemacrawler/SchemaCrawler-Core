/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.datasource;

import static us.fatehi.utility.Utility.isBlank;

import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public final class JdbcUrlParser {

  private record ParsedAuthority(Integer port, String databaseName, String options) {}

  private record ParsedHostPort(Integer port) {}

  public static JdbcUrl parse(final String url) {
    if (isBlank(url)) {
      return JdbcUrl.empty();
    }
    final String jdbc = url.trim();
    if (!jdbc.startsWith("jdbc:")) {
      return JdbcUrl.empty();
    }

    final int subprotocolEnd = jdbc.indexOf(':', "jdbc:".length());
    if (subprotocolEnd < 0) {
      return new JdbcUrl(jdbc.substring("jdbc:".length()), null, null);
    }

    final String databaseServerType = jdbc.substring("jdbc:".length(), subprotocolEnd);
    String body = stripQueryAndFragment(jdbc.substring(subprotocolEnd + 1));
    body = normalizeOracleAuthorityMarker(body);

    final boolean hasAuthority = hasAuthorityPrefix(body);
    final String normalizedBody = stripAuthorityPrefix(body);

    Integer port = null;
    String databaseName = null;

    if (hasAuthority) {
      // Branch A: authority form with host[:port][/db], optionally with properties.
      // Examples:
      // - jdbc:mysql://db.example.com:3306/appdb
      // - jdbc:postgresql://localhost:5432/postgres?ssl=true
      // - jdbc:sqlserver://sqlhost:1433;databaseName=Sales
      // - jdbc:oracle:thin:@//oracledb:1521/ORCLPDB1
      final ParsedAuthority parsed = parseAuthority(normalizedBody);
      port = parsed.port();
      databaseName = parsed.databaseName();
      if (isBlank(databaseName)) {
        databaseName = databaseNameFromOptions(parsed.options());
      }
    } else if (normalizedBody.contains(";")) {
      // Branch B: non-authority form with semicolon properties.
      // Examples:
      // - jdbc:sqlserver:sqlhost:1433;databaseName=Sales
      // - jdbc:jtds:sqlserver://host:1433;database=Sales
      // - jdbc:db2://host:50000/SAMPLE;retrieveMessagesFromServerOnGetMessage=true
      final String[] headAndOptions = normalizedBody.split(";", 2);
      final String head = headAndOptions[0];
      final String options = headAndOptions.length > 1 ? headAndOptions[1] : "";
      final ParsedHostPort parsedHostPort = parseHostPort(head);
      port = parsedHostPort.port();
      databaseName = databaseNameFromOptions(options);
      if (isBlank(databaseName)) {
        databaseName = firstToken(head);
      }
    } else {
      // Branch C: path-only/local forms (no host authority).
      // Examples:
      // - jdbc:sqlite:C:\data\sample.db
      // - jdbc:sqlite::memory:
      // - jdbc:offline:C:\snapshots\catalog.ser
      // - jdbc:h2:mem:testdb
      databaseName = firstToken(normalizedBody);
    }

    return new JdbcUrl(databaseServerType, port, databaseName);
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

    final String[] parts = hostAndDb.split("/", 2);
    final ParsedHostPort parsedHostPort = parseHostPort(parts[0]);
    final String databaseName = parts.length > 1 ? firstToken(parts[1]) : null;
    return new ParsedAuthority(parsedHostPort.port(), databaseName, options);
  }

  private static ParsedHostPort parseHostPort(final String hostPort) {
    if (isBlank(hostPort)) {
      return new ParsedHostPort(null);
    }
    String value = hostPort.trim();
    if (value.contains(",")) {
      value = value.split(",", 2)[0].trim();
    }

    if (value.startsWith("[")) {
      final int end = value.indexOf(']');
      if (end > 0) {
        Integer port = null;
        if (end + 2 <= value.length() && value.charAt(end + 1) == ':') {
          port = parsePort(value.substring(end + 2));
        }
        return new ParsedHostPort(port);
      }
    }

    final int firstColon = value.indexOf(':');
    final int lastColon = value.lastIndexOf(':');
    if (firstColon > 0 && firstColon == lastColon) {
      return new ParsedHostPort(parsePort(value.substring(firstColon + 1)));
    }
    return new ParsedHostPort(null);
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

  private JdbcUrlParser() {
    // Prevent instantiation
  }
}
