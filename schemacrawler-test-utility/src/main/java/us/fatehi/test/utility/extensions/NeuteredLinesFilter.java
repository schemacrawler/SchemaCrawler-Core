/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.test.utility.extensions;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

final class NeuteredLinesFilter implements Predicate<String> {

  private final List<Predicate<String>> neuters =
      List.of(
          //
          Pattern.compile(".*jdbc:.*").asMatchPredicate(),
          Pattern.compile("database product version.*").asMatchPredicate(),
          Pattern.compile("database version.*").asMatchPredicate(),
          Pattern.compile("driver version.*").asMatchPredicate(),
          Pattern.compile("-- operating system:.*").asMatchPredicate(),
          Pattern.compile("-- JVM system:.*").asMatchPredicate(),
          Pattern.compile("\\s+<schemaCrawler(Version|About|Info)>.*").asMatchPredicate(),
          Pattern.compile(".*[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}.*")
              .asMatchPredicate(), // UUID
          Pattern.compile("\\s+<product(Name|Version)>.*").asMatchPredicate(),
          Pattern.compile("PRODUCT_(NAME|VERSION).*").asMatchPredicate(),
          Pattern.compile("DATABASE_(NAME|VERSION).*").asMatchPredicate(),
          Pattern.compile("driver (minor |major )?version                      .*")
              .asMatchPredicate(),
          Pattern.compile("TIMEZONE.*").asMatchPredicate(),
          Pattern.compile(".*[A-Za-z]+ \\d+, 20[12]\\d \\d+:\\d+ [AP]M.*")
              .asMatchPredicate(), // date and time
          Pattern.compile(".*20[12]\\d-\\d\\d-\\d\\d[ T]\\d\\d:\\d\\d.*")
              .asMatchPredicate(), // date and time
          // ANSI color sequences
          Pattern.compile("\\x1B\\[([0-9]{1,2}(;[0-9]{1,2})?)?[mGK]").asMatchPredicate(),
          // JSON and YAML output
          Pattern.compile("- column @uuid: .*").asMatchPredicate(),
          Pattern.compile("\\s+\"?run-id\"?\\s?: .*").asMatchPredicate(),
          Pattern.compile("\\s+\"?crawl-timestamp\"?\\s?: .*").asMatchPredicate(),
          Pattern.compile("\\s+\"?crawl-timestamp-instant\"?\\s?: .*").asMatchPredicate(),
          Pattern.compile("\\s*(- )?\"?lint-id\"?\\s?: .*").asMatchPredicate(),
          Pattern.compile("\\s+\"?linter-instance-id\"?\\s?: .*").asMatchPredicate(),
          Pattern.compile("\\s+\"?product-version\"?\\s?: .*").asMatchPredicate(),
          Pattern.compile("\\s+\"?value\"?\\s?: .*").asMatchPredicate(),
          // Scribe
          Pattern.compile("\\sat:\\s*\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z")
              .asMatchPredicate(),
          Pattern.compile("\\sby:\\s*.*schemacrawler.*").asMatchPredicate(),
          // Versions
          Pattern.compile(".*1[67]\\.\\d{1,2}\\.\\d{1,2}.*").asMatchPredicate(),
          // Operating systems and environment
          Pattern.compile(".*(Windows|Linux|Mac OS).*").asMatchPredicate(),
          Pattern.compile(".*(Java|OpenJDK).*").asMatchPredicate(),
          Pattern.compile(".*JVM Architecture.*").asMatchPredicate(),
          // Files
          Pattern.compile(".*file:///.*").asMatchPredicate(),
          // SQL Server
          // -- server-specific values
          Pattern.compile(".*ServerName.*").asMatchPredicate(),
          // DB2
          // -- unnamed objects
          Pattern.compile("SQL\\d{15}.*").asMatchPredicate(),
          // -- indexes
          Pattern.compile("[\"0-9A-Z]{28,30}.*").asMatchPredicate(),
          // constraints
          // -- server-specific values
          Pattern.compile(".*HOST_NAME.*").asMatchPredicate(),
          Pattern.compile(".*TOTAL_MEMORY.*").asMatchPredicate(),
          Pattern.compile(".*TOTAL_CPUS.*").asMatchPredicate(),
          Pattern.compile(".*OS_NAME.*").asMatchPredicate(),
          // Apache Derby
          // -- unnamed objects
          Pattern.compile("SQL\\d+\\s+\\[primary key]").asMatchPredicate(),
          Pattern.compile("SQL\\d+\\s+\\[foreign key, with no action]").asMatchPredicate(),
          // MySQL
          // -- server-specific values
          Pattern.compile("server_uuid\\s+.*").asMatchPredicate(),
          Pattern.compile("hostname\\s+.*").asMatchPredicate(),
          Pattern.compile("  value\\s+\\d+\\s+").asMatchPredicate(),
          // Oracle
          // -- server-specific values
          Pattern.compile("\\s+value\\s+localhost:\\d+:xe\\s+").asMatchPredicate(),
          Pattern.compile("\\s+value\\s+localhost:\\d+\\/xepdb1\\s+").asMatchPredicate(),
          Pattern.compile("\\s+value\\s+localhost:\\d+\\/freepdb1\\s+").asMatchPredicate(),
          Pattern.compile("BOOKS\\.\\\"ISEQ\\$\\$_\\d+\\\"\\s+\\[sequence\\]").asMatchPredicate(),
          Pattern.compile("Version .*").asMatchPredicate(),
          // PostgreSQL
          // -- unnamed objects
          Pattern.compile(".*pg_temp_.*").asMatchPredicate());

  /** Should we keep the line - that is, not ignore it? */
  @Override
  public boolean test(final String line) {
    for (final Predicate<String> neuter : neuters) {
      if (neuter.test(line)) {
        return false;
      }
    }
    return true;
  }
}
