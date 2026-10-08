/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import java.util.function.Predicate;
import schemacrawler.schema.Table;

/** Selects tables whose row count is not known to be zero. */
public final class NoEmptyTablesFilter implements Predicate<Table> {

  public static final String TABLE_ROW_COUNT_KEY = "schemacrawler.table.row_count";

  private static final long UNKNOWN_TABLE_ROW_COUNT = -1L;

  /** {@inheritDoc} */
  @Override
  public boolean test(final Table table) {
    return table != null && table.getAttribute(TABLE_ROW_COUNT_KEY, UNKNOWN_TABLE_ROW_COUNT) != 0L;
  }
}
