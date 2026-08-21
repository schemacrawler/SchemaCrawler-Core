/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */
package schemacrawler.loader.catalog.summary;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import schemacrawler.loader.catalog.summary.CatalogStats.CatalogCounts;
import schemacrawler.loader.catalog.summary.CatalogStats.DataTypesCounts;
import schemacrawler.loader.catalog.summary.CatalogStats.RoutinesCounts;
import schemacrawler.loader.catalog.summary.CatalogStats.SchemaCounts;
import schemacrawler.loader.catalog.summary.CatalogStats.SchemaStats;
import schemacrawler.loader.catalog.summary.CatalogStats.SequencesCounts;
import schemacrawler.loader.catalog.summary.CatalogStats.SynonymsCounts;
import schemacrawler.loader.catalog.summary.CatalogStats.TablesCounts;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Routine;
import schemacrawler.schema.RoutineType;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import us.fatehi.utility.UtilityMarker;

/** Utility methods for building {@link CatalogStats}. */
@UtilityMarker
public final class CatalogStatsUtility {

  /**
   * Running sums derived from per-schema {@link TablesCounts}.
   *
   * <p>Catalog-level counts for columns, foreign keys, and views are computed by summing
   * schema-level table counts (rather than re-reading all tables) so that {@link #from(Catalog)}
   * and {@link #schemaStatsFrom(Catalog)} stay consistent.
   */
  private record SchemaSums(int columns, int foreignKeys, int views) {

    SchemaSums() {
      this(0, 0, 0);
    }

    SchemaSums accumulate(final TablesCounts tablesCounts) {
      if (tablesCounts == null) {
        return this;
      }
      return new SchemaSums(
          columns + tablesCounts.columns(),
          foreignKeys + tablesCounts.foreignKeys(),
          views + tablesCounts.views());
    }
  }

  /** Running sums read directly from the catalog across all schemas. */
  private record CatalogSums(
      int schemas, int dataTypes, int tables, int routines, int synonyms, int sequences) {}

  public static CatalogStats from(final Catalog catalog) {
    requireNonNull(catalog, "No catalog provided");

    final Collection<Schema> schemaCollection = catalog.getSchemas();
    final List<Schema> schemaList = new ArrayList<>(schemaCollection);
    final List<SchemaStats> schemaStats = new ArrayList<>(schemaList.size());
    final List<SchemaCounts> schemaCounts = new ArrayList<>(schemaList.size());
    for (final Schema schema : schemaList) {
      final SchemaCounts counts = schemaCounts(catalog, schema);
      schemaStats.add(new SchemaStats(schema, counts));
      schemaCounts.add(counts);
    }

    return new CatalogStats(
        catalog.getCrawlInfo().getTitle(),
        catalog.getCrawlInfo(),
        catalogCounts(catalogSums(catalog), schemaCounts),
        List.copyOf(schemaStats));
  }

  public static List<SchemaStats> schemaStatsFrom(final Catalog catalog) {
    requireNonNull(catalog, "No catalog provided");

    final Collection<Schema> schemaCollection = catalog.getSchemas();
    final List<Schema> schemaList = new ArrayList<>(schemaCollection);
    final List<SchemaStats> schemaStats = new ArrayList<>(schemaList.size());
    final List<SchemaCounts> schemaCounts = new ArrayList<>(schemaList.size());
    for (final Schema schema : schemaList) {
      final SchemaCounts counts =
          new SchemaCounts(
              new DataTypesCounts(catalog.getColumnDataTypes(schema).size()),
              new TablesCounts(catalog.getTables(schema).size()),
              new RoutinesCounts(catalog.getRoutines(schema).size()),
              new SynonymsCounts(catalog.getSynonyms(schema).size()),
              new SequencesCounts(catalog.getSequences(schema).size()));
      schemaStats.add(new SchemaStats(schema, counts));
      schemaCounts.add(counts);
    }

    return schemaStats;
  }

  private static CatalogCounts catalogCounts(
      final CatalogSums catalogSums, final List<SchemaCounts> all) {
    final SchemaSums schemaSums = schemaSums(all);

    return new CatalogCounts(
        catalogSums.schemas(),
        catalogSums.dataTypes(),
        catalogSums.tables(),
        schemaSums.views(),
        schemaSums.foreignKeys(),
        schemaSums.columns(),
        catalogSums.routines(),
        catalogSums.synonyms(),
        catalogSums.sequences());
  }

  private static CatalogSums catalogSums(final Catalog catalog) {
    final CatalogSums catalogSums =
        new CatalogSums(
            catalog.getSchemas().size(),
            catalog.getColumnDataTypes().size(),
            catalog.getTables().size(),
            catalog.getRoutines().size(),
            catalog.getSynonyms().size(),
            catalog.getSequences().size());
    return catalogSums;
  }

  private static RoutinesCounts routineCounts(final Collection<Routine> routines) {
    int procedures = 0;
    int functions = 0;
    int parameters = 0;
    for (final Routine routine : routines) {
      final RoutineType routineType = routine.getType();
      switch (routineType) {
        case procedure -> procedures++;
        case function -> functions++;
        default -> {
          continue;
        }
      }
      parameters += routine.getParameters().size();
    }
    return new RoutinesCounts(routines.size(), procedures, functions, parameters);
  }

  private static SchemaCounts schemaCounts(final Catalog catalog, final Schema schema) {
    return new SchemaCounts(
        new DataTypesCounts(catalog.getColumnDataTypes(schema).size()),
        tableCounts(catalog.getTables(schema)),
        routineCounts(catalog.getRoutines(schema)),
        new SynonymsCounts(catalog.getSynonyms(schema).size()),
        new SequencesCounts(catalog.getSequences(schema).size()));
  }

  private static SchemaSums schemaSums(final List<SchemaCounts> all) {

    // Aggregate only the fields that are derived from table-level detail.
    SchemaSums schemaSums = new SchemaSums();
    for (final SchemaCounts counts : all) {
      schemaSums = schemaSums.accumulate(counts.tables());
    }

    return schemaSums;
  }

  private static TablesCounts tableCounts(final Collection<Table> tables) {
    int columns = 0;
    int primaryKeys = 0;
    int foreignKeys = 0;
    int indexes = 0;
    int triggers = 0;
    int views = 0;
    for (final Table table : tables) {
      columns += table.getColumns().size();
      if (table.hasPrimaryKey()) {
        primaryKeys++;
      }
      foreignKeys += table.getImportedForeignKeys().size();
      indexes += table.getIndexes().size();
      triggers += table.getTriggers().size();
      if (table.getTableType().isView()) {
        views++;
      }
    }
    return new TablesCounts(
        tables.size(), columns, primaryKeys, foreignKeys, indexes, triggers, views);
  }

  private CatalogStatsUtility() {
    // Prevent instantiation
  }
}
