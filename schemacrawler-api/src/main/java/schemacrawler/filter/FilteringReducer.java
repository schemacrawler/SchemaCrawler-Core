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
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Reducer;
import schemacrawler.schema.ReducibleCollection;

// Applies a predicate filter to any named-object collection.
final class FilteringReducer<N extends NamedObject> implements Reducer<N> {

  private final Predicate<N> filter;

  FilteringReducer(final Predicate<N> filter) {
    this.filter = requireNonNull(filter, "No filter provided");
  }

  @Override
  public void reduce(final ReducibleCollection<? extends N> allNamedObjects) {
    requireNonNull(allNamedObjects, "No named objects provided");
    allNamedObjects.filter(filter);
  }

  @Override
  public void undo(final ReducibleCollection<? extends N> allNamedObjects) {
    requireNonNull(allNamedObjects, "No named objects provided");
    allNamedObjects.resetFilter();
  }
}
