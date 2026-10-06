/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schema;

/**
 * Reduces a catalog by applying inclusion/exclusion filter options to each schema object type. Call
 * {@link #reduce(Reducible)} to apply the reduction, and {@link #undo(Reducible)} to restore the
 * catalog to its full, unfiltered state. Instances are created via {@code
 * ReducerFactory.getCatalogReducer(SchemaCrawlerOptions)}.
 */
public interface CatalogReducer {

  /**
   * Applies the configured filter options to each schema object type in the catalog, removing
   * objects that do not match the options.
   *
   * @param catalog Reducible catalog to reduce; must not be null.
   */
  void reduce(Reducible catalog);

  /**
   * Resets the catalog to its full, unfiltered state, reversing any prior {@link
   * #reduce(Reducible)} call.
   *
   * @param catalog Reducible catalog to reset; must not be null.
   */
  void undo(Reducible catalog);
}
