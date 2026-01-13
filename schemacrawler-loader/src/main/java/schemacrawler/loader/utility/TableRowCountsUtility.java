/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.utility;

import static java.util.Objects.requireNonNull;

import schemacrawler.schema.Table;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public final class TableRowCountsUtility {

  private static final int UNKNOWN_TABLE_ROW_COUNT = -1;
  public static final String TABLE_ROW_COUNT_KEY = "schemacrawler.table.row_count";

  public static long getRowCount(final Table table) {
    if (table == null) {
      return UNKNOWN_TABLE_ROW_COUNT;
    }

    return table.getAttribute(TABLE_ROW_COUNT_KEY, (long) UNKNOWN_TABLE_ROW_COUNT);
  }

  /**
   * Message format for the counts.
   *
   * @param number Number value in the message
   * @return Message format for the counts
   */
  public static String getRowCountMessage(final Number number) {
    requireNonNull(number, "No number provided");
    final long longValue = number.longValue();
    if (longValue <= 0) {
      return "empty";
    }
    return "%,d rows".formatted(longValue);
  }

  public static String getRowCountMessage(final Table table) {
    return getRowCountMessage(getRowCount(table));
  }

  public static boolean hasRowCount(final Table table) {
    return table != null && table.hasAttribute(TABLE_ROW_COUNT_KEY);
  }

  private TableRowCountsUtility() {
    // Prevent instantiation
  }
}
