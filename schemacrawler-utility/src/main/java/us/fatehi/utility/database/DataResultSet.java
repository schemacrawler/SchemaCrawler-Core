/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A lightweight wrapper around a JDBC {@link ResultSet} that iterates over rows and returns each
 * row's data as a list of objects. Column names are available via {@link #getColumnNames()}.
 *
 * <p>Three kinds of column values are possible:
 *
 * <ul>
 *   <li>{@code null} — the database column contained a SQL NULL value
 *   <li>{@link ColumnDataIndicator#BINARY_DATA} — the column holds binary or LOB data that was not
 *       read (either because it is a BLOB/LONGVARBINARY type, or because {@code showLobs} is {@code
 *       false})
 *   <li>{@link ColumnDataIndicator#ERROR_DATA} — reading the column failed with a JDBC exception
 *       (the exception is logged)
 *   <li>Any other object — the column value as returned by the JDBC driver
 * </ul>
 */
public final class DataResultSet extends AbstractDataResultSet {

  private int maxRows;

  /**
   * Wraps a JDBC {@link ResultSet} for row-by-row iteration.
   *
   * @param results the result set to wrap; must not be {@code null}
   * @throws SQLException if reading the result-set metadata fails
   */
  public DataResultSet(final ResultSet results) throws SQLException {
    super(results);

    maxRows = Integer.MAX_VALUE;
  }

  /**
   * Advances the cursor to the next row.
   *
   * @return {@code true} if the new current row is valid; {@code false} if there are no more rows
   *     or the {@code maxRows} limit has been reached
   * @throws SQLException if a database access error occurs
   */
  public boolean next() throws SQLException {
    if (getRowCount() == maxRows) {
      return false;
    }
    return advanceNext();
  }

  /**
   * Reads all columns of the current row and returns data as a list.
   *
   * @return a list whose entries are {@code null} (SQL NULL), {@link
   *     ColumnDataIndicator.BINARY_DATA} (unread binary/LOB), {@link
   *     ColumnDataIndicator.ERROR_DATA} (read error), or the column value
   * @throws SQLException if advancing within the result set fails (individual column errors are
   *     caught and represented as {@link ErrorDataIndicator#INSTANCE})
   */
  @Override
  public List<Object> row() throws SQLException {
    final int columnCount = getColumnNames().size();
    final List<Object> currentRow = new ArrayList<>(columnCount);
    for (int i = 1; i <= columnCount; i++) {
      currentRow.add(readColumnData(i));
    }
    return currentRow;
  }

  public void setMaxRows(final int maxRows) {
    if (maxRows < 0) {
      return;
    }
    this.maxRows = maxRows;
  }
}
