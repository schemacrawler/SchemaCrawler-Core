/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static java.util.Objects.requireNonNull;
import static schemacrawler.filter.ReducerFactory.getRoutineReducer;
import static schemacrawler.filter.ReducerFactory.getSchemaReducer;
import static schemacrawler.filter.ReducerFactory.getSequenceReducer;
import static schemacrawler.filter.ReducerFactory.getSynonymReducer;
import static schemacrawler.filter.ReducerFactory.getTableReducer;

import schemacrawler.schema.Catalog;
import schemacrawler.schema.CatalogReducer;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;

final class StandardCatalogReducer implements CatalogReducer {

  private final SchemaCrawlerOptions options;

  StandardCatalogReducer(final SchemaCrawlerOptions options) {
    this.options = requireNonNull(options, "No SchemaCrawler options provided");
  }

  @Override
  public void reduce(final Catalog catalog) {
    requireNonNull(catalog, "No catalog provided");

    catalog.reduce(Schema.class, getSchemaReducer(options));
    catalog.reduce(Table.class, getTableReducer(options));
    catalog.reduce(Routine.class, getRoutineReducer(options));
    catalog.reduce(Synonym.class, getSynonymReducer(options));
    catalog.reduce(Sequence.class, getSequenceReducer(options));
  }

  @Override
  public void undo(final Catalog catalog) {
    requireNonNull(catalog, "No catalog provided");

    final SchemaCrawlerOptions options = SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions();

    catalog.undo(Schema.class, getSchemaReducer(options));
    catalog.undo(Table.class, getTableReducer(options));
    catalog.undo(Routine.class, getRoutineReducer(options));
    catalog.undo(Synonym.class, getSynonymReducer(options));
    catalog.undo(Sequence.class, getSequenceReducer(options));
  }
}
