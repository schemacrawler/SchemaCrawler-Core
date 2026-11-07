/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.test.utility.extensions;

import java.util.function.Predicate;
import java.util.regex.Pattern;

final class NeuteredLinesFilter implements Predicate<String> {

  private static final Predicate<String> neuters =
      matching(".*jdbc:.*")
          .or(matching("database product version.*"))
          .or(matching("database version.*"))
          .or(matching("driver version.*"))
          .or(matching("-- operating system:.*"))
          .or(matching("-- JVM system:.*"))
          .or(matching("\\s+<schemaCrawler(Version|About|Info)>.*"))
          .or(matching(".*[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}.*")) // UUID
          .or(matching("\\s+<product(Name|Version)>.*"))
          .or(matching("PRODUCT_(NAME|VERSION).*"))
          .or(matching("DATABASE_(NAME|VERSION).*"))
          .or(matching("driver (minor |major )?version                      .*"))
          .or(matching("TIMEZONE.*"))
          .or(matching(".*[A-Za-z]+ \\d+, 20[12]\\d \\d+:\\d+ [AP]M.*")) // date and time
          .or(matching(".*20[12]\\d-\\d\\d-\\d\\d[ T]\\d\\d:\\d\\d.*")) // date and time
          // ANSI color sequences
          .or(matching("\\x1B\\[([0-9]{1,2}(;[0-9]{1,2})?)?[mGK]"))
          // JSON and YAML output
          .or(matching("- column @uuid: .*"))
          .or(matching("\\s+\"?run-id\"?\\s?: .*"))
          .or(matching("\\s+\"?crawl-timestamp\"?\\s?: .*"))
          .or(matching("\\s+\"?crawl-timestamp-instant\"?\\s?: .*"))
          .or(matching("\\s*(- )?\"?lint-id\"?\\s?: .*"))
          .or(matching("\\s+\"?linter-instance-id\"?\\s?: .*"))
          .or(matching("\\s+\"?product-version\"?\\s?: .*"))
          .or(matching("\\s+\"?value\"?\\s?: .*"))
          // Scribe
          .or(matching("\\sat:\\s*\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z"))
          .or(matching("\\sby:\\s*.*schemacrawler.*"))
          // Versions
          .or(matching(".*(1[6-9]|2\\d)\\.\\d{1,2}\\.\\d{1,2}.*"))
          // Operating systems and environment
          .or(matching(".*(Windows|Linux|Mac OS).*"))
          .or(matching(".*(Java|OpenJDK).*"))
          .or(matching(".*JVM Architecture.*"))
          // Files
          .or(matching(".*file:///.*"))
          // SQL Server
          // -- server-specific values
          .or(matching(".*ServerName.*"))
          // DB2
          // -- unnamed objects
          .or(matching("SQL\\d{15}.*"))
          // -- indexes
          .or(matching("[\"0-9A-Z]{28,30}.*"))
          // constraints
          // -- server-specific values
          .or(matching(".*HOST_NAME.*"))
          .or(matching(".*TOTAL_MEMORY.*"))
          .or(matching(".*TOTAL_CPUS.*"))
          .or(matching(".*OS_NAME.*"))
          // Apache Derby
          // -- unnamed objects
          .or(matching("SQL\\d+\\s+\\[primary key]"))
          .or(matching("SQL\\d+\\s+\\[foreign key, with no action]"))
          // MySQL
          // -- server-specific values
          .or(matching("server_uuid\\s+.*"))
          .or(matching("hostname\\s+.*"))
          .or(matching("  value\\s+\\d+\\s+"))
          // Oracle
          // -- server-specific values
          .or(matching("\\s+value\\s+localhost:\\d+:xe\\s+"))
          .or(matching("\\s+value\\s+localhost:\\d+\\/xepdb1\\s+"))
          .or(matching("\\s+value\\s+localhost:\\d+\\/freepdb1\\s+"))
          .or(matching("BOOKS\\.\\\"ISEQ\\$\\$_\\d+\\\"\\s+\\[sequence\\]"))
          .or(matching("Version .*"))
          // PostgreSQL
          // -- unnamed objects
          .or(matching(".*pg_temp_.*"));

  private static Predicate<String> matching(final String regex) {
    return Pattern.compile(regex).asMatchPredicate();
  }

  /** Should we keep the line - that is, not ignore it? */
  @Override
  public boolean test(final String line) {
    return neuters.test(line);
  }
}
