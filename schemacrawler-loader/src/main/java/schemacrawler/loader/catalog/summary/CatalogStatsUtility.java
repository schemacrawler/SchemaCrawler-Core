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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Routine;
import schemacrawler.schema.RoutineType;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.View;
import us.fatehi.utility.UtilityMarker;

/** Utility methods for building {@link CatalogStats}. */
@UtilityMarker
public final class CatalogStatsUtility {

  public static CatalogStats from(final Catalog catalog) {
    requireNonNull(catalog, "No catalog provided");

    final Collection<Schema> schemaCollection = catalog.getSchemas();
    final List<Schema> schemaList = new ArrayList<>(schemaCollection);
    final List<CatalogStats.SchemaStats> schemaStats = new ArrayList<>(schemaList.size());
    final List<CatalogStats.SchemaCounts> schemaCounts = new ArrayList<>(schemaList.size());
    for (final Schema schema : schemaList) {
      final CatalogStats.SchemaCounts counts = schemaCounts(catalog, schema);
      schemaStats.add(new CatalogStats.SchemaStats(schema, counts));
      schemaCounts.add(counts);
    }

    return new CatalogStats(
        catalog.getName(),
        catalog.getCrawlInfo(),
        catalogCounts(schemaCounts, catalog.getTables()),
        List.copyOf(schemaStats));
  }

  private static boolean isView(final Table table) {
    return table instanceof View || table.getTableType().isView();
  }

  private static CatalogStats.CatalogCounts catalogCounts(
      final List<CatalogStats.SchemaCounts> all) {
    int dataTypes = 0;
    int tables = 0;
    int columns = 0;
    int routines = 0;
    int synonyms = 0;
    int sequences = 0;
    for (final CatalogStats.SchemaCounts counts : all) {
      dataTypes += counts.dataTypes().count();
      tables += counts.tables().count();
      columns += counts.tables().columns();
      routines += counts.routines().count();
      synonyms += counts.synonyms().count();
      sequences += counts.sequences().count();
    }
    return new CatalogStats.CatalogCounts(
        all.size(), dataTypes, tables, columns, routines, synonyms, sequences, 0, 0, 0);
  }

  private static CatalogStats.CatalogCounts catalogCounts(
      final List<CatalogStats.SchemaCounts> all, final Collection<Table> allTables) {
    final CatalogStats.CatalogCounts counts = catalogCounts(all);

    int tableCount = 0;
    int viewCount = 0;
    final Map<NamedObjectKey, ForeignKey> foreignKeys = new LinkedHashMap<>();
    for (final Table table : allTables) {
      if (isView(table)) {
        viewCount++;
      } else {
        tableCount++;
      }
      for (final ForeignKey foreignKey : table.getImportedForeignKeys()) {
        foreignKeys.putIfAbsent(foreignKey.key(), foreignKey);
      }
    }

    return new CatalogStats.CatalogCounts(
        counts.schemas(),
        counts.dataTypes(),
        counts.tables(),
        counts.columns(),
        counts.routines(),
        counts.synonyms(),
        counts.sequences(),
        tableCount,
        viewCount,
        foreignKeys.size());
  }

  private static CatalogStats.RoutineCounts routineCounts(final Collection<Routine> routines) {
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
    return new CatalogStats.RoutineCounts(routines.size(), procedures, functions, parameters);
  }

  private static CatalogStats.SchemaCounts schemaCounts(
      final Catalog catalog, final Schema schema) {
    return new CatalogStats.SchemaCounts(
        new CatalogStats.DataTypeCounts(catalog.getColumnDataTypes(schema).size()),
        tableCounts(catalog.getTables(schema)),
        routineCounts(catalog.getRoutines(schema)),
        new CatalogStats.SynonymCounts(catalog.getSynonyms(schema).size()),
        new CatalogStats.SequenceCounts(catalog.getSequences(schema).size()));
  }

  private static CatalogStats.TableCounts tableCounts(final Collection<Table> tables) {
    int columns = 0;
    int primaryKeys = 0;
    int foreignKeys = 0;
    int indexes = 0;
    int triggers = 0;
    for (final Table table : tables) {
      columns += table.getColumns().size();
      if (table.hasPrimaryKey()) {
        primaryKeys++;
      }
      foreignKeys += table.getImportedForeignKeys().size();
      indexes += table.getIndexes().size();
      triggers += table.getTriggers().size();
    }
    return new CatalogStats.TableCounts(
        tables.size(), columns, primaryKeys, foreignKeys, indexes, triggers);
  }

  private CatalogStatsUtility() {
    // Prevent instantiation
  }
}
