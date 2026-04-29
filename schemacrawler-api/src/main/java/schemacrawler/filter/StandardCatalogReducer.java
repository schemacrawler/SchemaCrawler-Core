/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static java.util.Objects.requireNonNull;
import static schemacrawler.schemacrawler.DatabaseObjectRuleForInclusion.ruleForRoutineInclusion;
import static schemacrawler.schemacrawler.DatabaseObjectRuleForInclusion.ruleForSchemaInclusion;
import static schemacrawler.schemacrawler.DatabaseObjectRuleForInclusion.ruleForSequenceInclusion;
import static schemacrawler.schemacrawler.DatabaseObjectRuleForInclusion.ruleForSynonymInclusion;
import static schemacrawler.schemacrawler.DatabaseObjectRuleForInclusion.ruleForTableInclusion;

import java.util.function.Predicate;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.CatalogReducer;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.LimitOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;

final class StandardCatalogReducer implements CatalogReducer {

  private static Predicate<Routine> routineFilter(final SchemaCrawlerOptions options) {
    final LimitOptions limitOptions = options.limitOptions();
    return new RoutineTypesFilter(limitOptions)
        .and(new DatabaseObjectFilter<>(limitOptions, ruleForRoutineInclusion))
        .and(new RoutineGrepFilter(options.grepOptions()));
  }

  private static Predicate<Schema> schemaFilter(final SchemaCrawlerOptions options) {
    return new InclusionRuleFilter<>(options.limitOptions().get(ruleForSchemaInclusion), true);
  }

  private static Predicate<Sequence> sequenceFilter(final SchemaCrawlerOptions options) {
    return new DatabaseObjectFilter<>(options.limitOptions(), ruleForSequenceInclusion);
  }

  private static Predicate<Synonym> synonymFilter(final SchemaCrawlerOptions options) {
    return new DatabaseObjectFilter<>(options.limitOptions(), ruleForSynonymInclusion);
  }

  private static Predicate<Table> tableFilter(final SchemaCrawlerOptions options) {
    final LimitOptions limitOptions = options.limitOptions();
    return new TableTypesFilter(limitOptions)
        .and(new DatabaseObjectFilter<>(limitOptions, ruleForTableInclusion))
        .and(new TableGrepFilter(options.grepOptions()));
  }

  private final SchemaCrawlerOptions options;

  StandardCatalogReducer(final SchemaCrawlerOptions options) {
    this.options = requireNonNull(options, "No SchemaCrawler options provided");
  }

  @Override
  public void reduce(final Catalog catalog) {
    requireNonNull(catalog, "No catalog provided");

    catalog.reduce(Schema.class, new FilteringReducer<>(schemaFilter(options)));
    catalog.reduce(Table.class, new TablesReducer(options, tableFilter(options)));
    catalog.reduce(Routine.class, new FilteringReducer<>(routineFilter(options)));
    catalog.reduce(Synonym.class, new FilteringReducer<>(synonymFilter(options)));
    catalog.reduce(Sequence.class, new FilteringReducer<>(sequenceFilter(options)));
  }

  @Override
  public void undo(final Catalog catalog) {
    requireNonNull(catalog, "No catalog provided");

    catalog.undo(Schema.class, new FilteringReducer<>(schemaFilter(options)));
    catalog.undo(Table.class, new TablesReducer(options, tableFilter(options)));
    catalog.undo(Routine.class, new FilteringReducer<>(routineFilter(options)));
    catalog.undo(Synonym.class, new FilteringReducer<>(synonymFilter(options)));
    catalog.undo(Sequence.class, new FilteringReducer<>(sequenceFilter(options)));
  }
}
