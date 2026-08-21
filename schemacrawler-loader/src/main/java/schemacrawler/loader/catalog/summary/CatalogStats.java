/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */
package schemacrawler.loader.catalog.summary;

import static java.util.Objects.requireNonNull;

import java.util.List;
import schemacrawler.schema.CrawlInfo;
import schemacrawler.schema.Schema;

/** Immutable catalog summary statistics. */
public record CatalogStats(
    String title, CrawlInfo crawlInfo, CatalogCounts counts, List<SchemaStats> schemas) {

  /** Totals across all data types that are considered in a schema. */
  public record DataTypesCounts(Integer count) {
    public DataTypesCounts {
      count = StatsUtility.removeNegativeInteger.apply(count);
    }
  }

  /** Totals across all synonyms that are considered in a schema. */
  public record SynonymsCounts(Integer count) {
    public SynonymsCounts {
      count = StatsUtility.removeNegativeInteger.apply(count);
    }
  }

  /** Totals across all sequences that are considered in a schema. */
  public record SequencesCounts(Integer count) {
    public SequencesCounts {
      count = StatsUtility.removeNegativeInteger.apply(count);
    }
  }

  /** Totals across all tables that are considered in a schema. */
  public record TablesCounts(
      Integer count,
      Integer columns,
      Integer primaryKeys,
      Integer foreignKeys,
      Integer indexes,
      Integer triggers,
      Integer views) {

    public TablesCounts {
      count = StatsUtility.removeNegativeInteger.apply(count);
      columns = StatsUtility.removeNegativeInteger.apply(columns);
      primaryKeys = StatsUtility.removeNegativeInteger.apply(primaryKeys);
      foreignKeys = StatsUtility.removeNegativeInteger.apply(foreignKeys);
      indexes = StatsUtility.removeNegativeInteger.apply(indexes);
      triggers = StatsUtility.removeNegativeInteger.apply(triggers);
      views = StatsUtility.removeNegativeInteger.apply(views);
    }

    public TablesCounts(final Integer count) {
      this(count, null, null, null, null, null, null);
    }
  }

  /** Totals across all routines that are considered in a schema. */
  public record RoutinesCounts(
      Integer count, Integer procedures, Integer functions, Integer parameters) {

    public RoutinesCounts {
      count = StatsUtility.removeNegativeInteger.apply(count);
      procedures = StatsUtility.removeNegativeInteger.apply(procedures);
      functions = StatsUtility.removeNegativeInteger.apply(functions);
      parameters = StatsUtility.removeNegativeInteger.apply(parameters);
    }

    public RoutinesCounts(final Integer count) {
      this(count, null, null, null);
    }
  }

  /** Totals across all objects that are considered in a schema. */
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
   * @param views number of views
   * @param foreignKeys deduplicated number of foreign keys across all tables
   */
  public record CatalogCounts(
      Integer schemas,
      Integer dataTypes,
      Integer tables,
      Integer views,
      Integer foreignKeys,
      Integer columns,
      Integer routines,
      Integer synonyms,
      Integer sequences) {

    public CatalogCounts {
      schemas = StatsUtility.removeNegativeInteger.apply(schemas);
      dataTypes = StatsUtility.removeNegativeInteger.apply(dataTypes);
      tables = StatsUtility.removeNegativeInteger.apply(tables);
      views = StatsUtility.removeNegativeInteger.apply(views);
      columns = StatsUtility.removeNegativeInteger.apply(columns);
      foreignKeys = StatsUtility.removeNegativeInteger.apply(foreignKeys);
      routines = StatsUtility.removeNegativeInteger.apply(routines);
      synonyms = StatsUtility.removeNegativeInteger.apply(synonyms);
      sequences = StatsUtility.removeNegativeInteger.apply(sequences);
    }
  }

  public record SchemaStats(Schema schema, SchemaCounts counts) {}

  public CatalogStats {
    title = requireNonNull(title, "No title provided");
    crawlInfo = requireNonNull(crawlInfo, "No crawl info provided");
    counts = requireNonNull(counts, "No catalog counts provided");
    schemas = List.copyOf(requireNonNull(schemas, "No schema stats provided"));
  }
}
