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
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;
import us.fatehi.utility.UtilityMarker;

/** Shared factory for memoized {@link TableEntityModelInferrer} instances. */
@UtilityMarker
public final class TableEntityModelInferrerFactory {

  private static final int MAX_CACHE_SIZE = 10;

  private static final ConcurrentMap<NamedObjectKey, TableEntityModelInferrer> inferrerMemo =
      new ConcurrentHashMap<>();

  public static TableEntityModelInferrer forTable(final Table table) {
    requireNonNull(table, "No table provided");

    final NamedObjectKey tableKey = table.key();
    if (tableKey == null) {
      return new TableEntityModelInferrer(table);
    }

    final TableEntityModelInferrer inferrer =
        inferrerMemo.computeIfAbsent(tableKey, key -> new TableEntityModelInferrer(table));
    if (inferrerMemo.size() > MAX_CACHE_SIZE) {
      inferrerMemo.clear();
    }
    return inferrer;
  }

  private TableEntityModelInferrerFactory() {
    // Prevent instantiation
  }
}
