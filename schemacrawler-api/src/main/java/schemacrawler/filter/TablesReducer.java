/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;
import schemacrawler.schema.Reducer;
import schemacrawler.schema.ReducibleCollection;
import schemacrawler.schema.Table;

final class TablesReducer implements Reducer<Table> {

  private final Predicate<Table> tableFilter;

  TablesReducer(final Predicate<Table> tableFilter) {
    this.tableFilter = requireNonNull(tableFilter, "No table filter provided");
  }

  @Override
  public void reduce(final ReducibleCollection<? extends Table> allTables) {
    if (allTables == null) {
      return;
    }
    allTables.filter(tableFilter);
  }

  @Override
  public void undo(final ReducibleCollection<? extends Table> allTables) {
    if (allTables == null) {
      return;
    }
    allTables.resetFilter();
  }
}
