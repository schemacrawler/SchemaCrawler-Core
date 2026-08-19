/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.implementation;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableReference;

/** Thread-safe singleton cache for TableEntityModelInferrer values keyed by NamedObjectKey. */
public final class TableEntityModelInferrerCache {

  private final ConcurrentMap<NamedObjectKey, TableEntityModelInferrer> cache;

  public TableEntityModelInferrerCache() {
    cache = new ConcurrentHashMap<>();
  }

  public void clear() {
    cache.clear();
  }

  /**
   * Returns a cached value when present; otherwise obtains one from {@code valueSupplier}, stores
   * it atomically, and returns the cached result.
   *
   * <p>The supplier may be invoked more than once under contention, although only one computed
   * result is retained in the map. Keep the supplier side-effect free where possible.
   */
  public TableEntityModelInferrer fromTable(final Table table) {
    requireNonNull(table, "Table not provided");

    final NamedObjectKey key = table.key();
    final Supplier<TableEntityModelInferrer> valueSupplier =
        () -> {
          final TableEntityModelInferrer modelInferrer = new TableEntityModelInferrer(table);
          return modelInferrer;
        };

    return cache.computeIfAbsent(
        key, value -> requireNonNull(valueSupplier.get(), "Value supplier must not return null"));
  }

  /**
   * Returns a cached value when present; otherwise obtains one from {@code valueSupplier}, stores
   * it atomically, and returns the cached result.
   *
   * <p>The supplier may be invoked more than once under contention, although only one computed
   * result is retained in the map. Keep the supplier side-effect free where possible.
   */
  public TableEntityModelInferrer fromTableReference(final TableReference fk) {
    requireNonNull(fk, "Table reference not provided");

    return fromTable(fk.getForeignKeyTable());
  }
}
