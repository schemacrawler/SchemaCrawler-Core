/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */
package schemacrawler.loader.utility;

import static java.util.Objects.requireNonNull;

import java.util.List;
import schemacrawler.schema.CrawlInfo;
import schemacrawler.schema.Schema;

/** Immutable catalog summary statistics. */
public record CatalogStats(
    String catalogName, CrawlInfo crawlInfo, CatalogCounts counts, List<SchemaStats> schemas) {

  public record DataTypesCounts(int count) {}

  public record SynonymsCounts(int count) {}

  public record SequencesCounts(int count) {}

  public record TablesCounts(
      int count, int columns, int primaryKeys, int foreignKeys, int indexes, int triggers) {}

  public record RoutinesCounts(int count, int procedures, int functions, int parameters) {}

  public record SchemaCounts(
      DataTypesCounts dataTypes,
      TablesCounts tables,
      RoutinesCounts routines,
      SynonymsCounts synonyms,
      SequencesCounts sequences) {}

  /**
   * Aggregate counts across all schemas in the catalog.
   *
   * @param schemas total number of schemas
   * @param dataTypes total number of data types across all schemas
   * @param tables total number of table objects (including views) across all schemas
   * @param columns total number of columns across all tables and views
   * @param routines total number of routines across all schemas
   * @param synonyms total number of synonyms across all schemas
   * @param sequences total number of sequences across all schemas
   * @param tableCount number of non-view tables (excludes views)
   * @param viewCount number of views
   * @param foreignKeyCount deduplicated number of foreign keys across all tables
   */
  public record CatalogCounts(
      int schemas,
      int dataTypes,
      int tables,
      int columns,
      int routines,
      int synonyms,
      int sequences,
      int tableCount,
      int viewCount,
      int foreignKeyCount) {}

  public record SchemaStats(Schema schema, SchemaCounts counts) {}

  public CatalogStats {
    catalogName = requireNonNull(catalogName, "No catalog name provided");
    crawlInfo = requireNonNull(crawlInfo, "No crawl info provided");
    counts = requireNonNull(counts, "No catalog counts provided");
    schemas = List.copyOf(requireNonNull(schemas, "No schema stats provided"));
  }
}
