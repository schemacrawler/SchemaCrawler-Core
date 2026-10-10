/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.utility;

import schemacrawler.schema.Table;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public final class TableRowCountsUtility {

  private static final long UNKNOWN_TABLE_ROW_COUNT = -1L;
  public static final String TABLE_ROW_COUNT_KEY = "schemacrawler.table.row_count";

  public static long getRowCount(final Table table) {
    if (table == null) {
      return UNKNOWN_TABLE_ROW_COUNT;
    }

    return table.getAttribute(TABLE_ROW_COUNT_KEY, UNKNOWN_TABLE_ROW_COUNT);
  }

  public static boolean hasRowCount(final Table table) {
    return table != null && table.hasAttribute(TABLE_ROW_COUNT_KEY);
  }

  private TableRowCountsUtility() {
    // Prevent instantiation
  }
}
