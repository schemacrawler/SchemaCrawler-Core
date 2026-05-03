/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.catalog.summary;

import schemacrawler.schema.CrawlInfo;
import schemacrawler.schema.Schema;

/** Produces a valid, parseable YAML summary of a catalog. */
final class YamlCatalogSummaryHandler implements CatalogSummaryHandler {

  private static String quoted(final Object value) {
    final String s = value == null ? "" : value.toString();
    if (s.isEmpty()
        || s.contains(":")
        || s.contains("#")
        || s.contains("'")
        || s.startsWith("-")
        || s.startsWith("?")
        || s.startsWith("&")) {
      return "'" + s.replace("'", "''") + "'";
    }
    return s;
  }

  private final StringBuilder sb = new StringBuilder();

  @Override
  public void begin() {
    // Nothing to do
  }

  @Override
  public void end() {
    // Nothing to do
  }

  @Override
  public void handleHeader(
      final String catalogName, final CrawlInfo crawlInfo, final CatalogCounts counts) {
    sb.append("catalog: ").append(catalogName).append("\n");
    sb.append("crawl-info:\n");
    entry("  ", "generated-by", crawlInfo.getSchemaCrawlerVersion());
    entry("  ", "generated-on", crawlInfo.getCrawlTimestamp());
    entry("  ", "database", crawlInfo.getDatabaseVersion());
    entry("  ", "jdbc-driver", crawlInfo.getJdbcDriverVersion());
    entry("  ", "os", crawlInfo.getOperatingSystemVersion());
    entry("  ", "jvm", crawlInfo.getJvmVersion());
    sb.append("counts:\n");
    count("  ", "schemas", counts.schemas());
    count("  ", "data-types", counts.dataTypes());
    count("  ", "tables", counts.tables());
    count("  ", "columns", counts.columns());
    count("  ", "routines", counts.routines());
    count("  ", "synonyms", counts.synonyms());
    count("  ", "sequences", counts.sequences());
    sb.append("schemas:\n");
  }

  @Override
  public void handleSchema(final Schema schema, final SchemaCounts c) {
    sb.append("  - id: ").append(schema.key().slug()).append("\n");
    entry("    ", "name", schema.getFullName());
    sb.append("    data-types:\n");
    count("      ", "count", c.dataTypes().count());
    appendTableCounts(c.tables());
    appendRoutineCounts(c.routines());
    sb.append("    synonyms:\n");
    count("      ", "count", c.synonyms().count());
    sb.append("    sequences:\n");
    count("      ", "count", c.sequences().count());
  }

  String getYaml() {
    return sb.toString();
  }

  private void appendRoutineCounts(final RoutineCounts routines) {
    sb.append("    routines:\n");
    count("      ", "count", routines.count());
    if (routines.count() > 0) {
      count("      ", "procedures", routines.procedures());
      count("      ", "functions", routines.functions());
      count("      ", "parameters", routines.parameters());
    }
  }

  private void appendTableCounts(final TableCounts tables) {
    sb.append("    tables:\n");
    count("      ", "count", tables.count());
    if (tables.count() > 0) {
      count("      ", "columns", tables.columns());
      count("      ", "primary-keys", tables.primaryKeys());
      count("      ", "foreign-keys", tables.foreignKeys());
      count("      ", "indexes", tables.indexes());
      count("      ", "triggers", tables.triggers());
    }
  }

  private void count(final String indent, final String key, final int n) {
    sb.append(indent).append(key).append(": ").append(n).append("\n");
  }

  private void entry(final String indent, final String key, final Object value) {
    sb.append(indent).append(key).append(": ").append(quoted(value)).append("\n");
  }
}
