/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.catalog.summary;

import java.util.Collection;
import java.util.List;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.CrawlInfo;
import schemacrawler.schema.Routine;
import schemacrawler.schema.RoutineType;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;

/**
 * Handler interface for catalog summary traversal. Implementations receive structured count data
 * for each schema and catalog-level aggregates.
 */
public interface CatalogSummaryHandler {

  record DataTypeCounts(int count) {}

  record SynonymCounts(int count) {}

  record SequenceCounts(int count) {}

  record TableCounts(
      int count, int columns, int primaryKeys, int foreignKeys, int indexes, int triggers) {

    static TableCounts from(final Collection<Table> tables) {
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
      return new TableCounts(tables.size(), columns, primaryKeys, foreignKeys, indexes, triggers);
    }
  }

  record RoutineCounts(int count, int procedures, int functions, int parameters) {

    static RoutineCounts from(final Collection<Routine> routines) {
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
      return new RoutineCounts(routines.size(), procedures, functions, parameters);
    }
  }

  record SchemaCounts(
      DataTypeCounts dataTypes,
      TableCounts tables,
      RoutineCounts routines,
      SynonymCounts synonyms,
      SequenceCounts sequences) {

    static SchemaCounts from(final Catalog catalog, final Schema schema) {
      return new SchemaCounts(
          new DataTypeCounts(catalog.getColumnDataTypes(schema).size()),
          TableCounts.from(catalog.getTables(schema)),
          RoutineCounts.from(catalog.getRoutines(schema)),
          new SynonymCounts(catalog.getSynonyms(schema).size()),
          new SequenceCounts(catalog.getSequences(schema).size()));
    }
  }

  /** Catalog-level aggregate counts, derived from the list of per-schema counts. */
  record CatalogCounts(
      int schemas,
      int dataTypes,
      int tables,
      int columns,
      int routines,
      int synonyms,
      int sequences) {

    static CatalogCounts from(final List<SchemaCounts> all) {
      int dataTypes = 0;
      int tables = 0;
      int columns = 0;
      int routines = 0;
      int synonyms = 0;
      int sequences = 0;
      for (final SchemaCounts c : all) {
        dataTypes += c.dataTypes().count();
        tables += c.tables().count();
        columns += c.tables().columns();
        routines += c.routines().count();
        synonyms += c.synonyms().count();
        sequences += c.sequences().count();
      }
      return new CatalogCounts(
          all.size(), dataTypes, tables, columns, routines, synonyms, sequences);
    }
  }

  void begin();

  void end();

  void handleHeader(String catalogName, CrawlInfo crawlInfo, CatalogCounts catalogCounts);

  void handleSchema(Schema schema, SchemaCounts schemaCounts);
}
