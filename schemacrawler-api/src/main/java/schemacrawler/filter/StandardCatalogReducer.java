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
import schemacrawler.schema.CatalogReducer;
import schemacrawler.schema.Reducible;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.CrawlOptions;
import schemacrawler.schemacrawler.LimitOptions;

final class StandardCatalogReducer implements CatalogReducer {

  private static Predicate<Routine> routineFilter(final CrawlOptions options) {
    final LimitOptions limitOptions = options.limitOptions();
    return new RoutineTypesFilter(limitOptions)
        .and(new DatabaseObjectFilter<>(limitOptions, ruleForRoutineInclusion));
  }

  private static Predicate<Schema> schemaFilter(final CrawlOptions options) {
    return NamedObjectFilters.fullName(options.limitOptions().get(ruleForSchemaInclusion));
  }

  private static Predicate<Sequence> sequenceFilter(final CrawlOptions options) {
    return new DatabaseObjectFilter<>(options.limitOptions(), ruleForSequenceInclusion);
  }

  private static Predicate<Synonym> synonymFilter(final CrawlOptions options) {
    return new DatabaseObjectFilter<>(options.limitOptions(), ruleForSynonymInclusion);
  }

  private static Predicate<Table> tableFilter(final CrawlOptions options) {
    final LimitOptions limitOptions = options.limitOptions();
    return new TableTypesFilter(limitOptions)
        .and(new DatabaseObjectFilter<>(limitOptions, ruleForTableInclusion));
  }

  private final CrawlOptions options;

  StandardCatalogReducer(final CrawlOptions options) {
    this.options = requireNonNull(options, "No crawl options provided");
  }

  @Override
  public void reduce(final Reducible catalog) {
    requireNonNull(catalog, "No catalog provided");

    catalog.reduce(Schema.class, new FilteringReducer<>(schemaFilter(options)));
    catalog.reduce(Table.class, new TablesReducer(tableFilter(options)));
    catalog.reduce(Routine.class, new FilteringReducer<>(routineFilter(options)));
    catalog.reduce(Synonym.class, new FilteringReducer<>(synonymFilter(options)));
    catalog.reduce(Sequence.class, new FilteringReducer<>(sequenceFilter(options)));
  }

  @Override
  public void undo(final Reducible catalog) {
    requireNonNull(catalog, "No catalog provided");

    catalog.undo(Schema.class, new FilteringReducer<>(schemaFilter(options)));
    catalog.undo(Table.class, new TablesReducer(tableFilter(options)));
    catalog.undo(Routine.class, new FilteringReducer<>(routineFilter(options)));
    catalog.undo(Synonym.class, new FilteringReducer<>(synonymFilter(options)));
    catalog.undo(Sequence.class, new FilteringReducer<>(sequenceFilter(options)));
  }
}
