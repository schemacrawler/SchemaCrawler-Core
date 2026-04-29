/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.database;

import static java.sql.Types.BLOB;
import static java.sql.Types.CLOB;
import static java.sql.Types.LONGNVARCHAR;
import static java.sql.Types.LONGVARBINARY;
import static java.sql.Types.LONGVARCHAR;
import static java.sql.Types.NCLOB;
import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.IOUtility.readFully;

import java.io.Reader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import us.fatehi.utility.UtilityLogger;
import us.fatehi.utility.string.StringFormat;

public abstract class AbstractDataResultSet implements AutoCloseable {

  private static final Logger LOGGER = Logger.getLogger(AbstractDataResultSet.class.getName());

  private final ResultSet results;
  private final List<String> columnNames;
  private int rowCount;
  private boolean readLargeData;

  /**
   * Wraps a JDBC {@link ResultSet} for row-by-row iteration.
   *
   * @param results the result set to wrap; must not be {@code null}
   * @throws SQLException if reading the result-set metadata fails
   */
  public AbstractDataResultSet(final ResultSet results) throws SQLException {
    this.results = requireNonNull(results, "Cannot use null results");

    final ResultSetMetaData metaData = results.getMetaData();
    final int columnCount = metaData.getColumnCount();
    final String[] columnNamesArray = new String[columnCount];
    for (int i = 0; i < columnCount; i++) {
      columnNamesArray[i] = metaData.getColumnLabel(i + 1);
    }
    columnNames = List.of(columnNamesArray);

    rowCount = 0;
  }

  /**
   * Closes the underlying {@link ResultSet}.
   *
   * @throws SQLException if a database access error occurs
   */
  @Override
  public final void close() throws SQLException {
    results.close();
    LOGGER.log(Level.FINE, new StringFormat("Processed %d rows", rowCount));
  }

  /**
   * Returns the column labels for this result set in ordinal order.
   *
   * @return array of column label strings
   */
  public final List<String> getColumnNames() {
    return columnNames;
  }

  public int getRowCount() {
    return rowCount;
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
  public List<Object> row() throws SQLException {
    final int columnCount = columnNames.size();
    final List<Object> currentRow = new ArrayList<>(columnCount);
    for (int i = 1; i <= columnCount; i++) {
      currentRow.add(readColumnData(i));
    }
    return currentRow;
  }

  public void setReadLargeData(final boolean readLargeData) {
    this.readLargeData = readLargeData;
  }

  /**
   * Advances the cursor to the next row.
   *
   * @return {@code true} if the new current row is valid; {@code false} if there are no more rows
   * @throws SQLException if a database access error occurs
   */
  protected final boolean advanceNext() throws SQLException {
    final boolean hasNext = results.next();
    new UtilityLogger(LOGGER).logSQLWarnings(results);
    if (hasNext) {
      rowCount = rowCount + 1;
    }
    return hasNext;
  }

  protected final Object readColumnData(final int columnIndex) {
    try {
      final int jdbcType = results.getMetaData().getColumnType(columnIndex);
      return switch (jdbcType) {
        case BLOB, LONGVARBINARY -> readBinaryColumnData(columnIndex);
        case CLOB, NCLOB, LONGNVARCHAR, LONGVARCHAR -> readCharacterColumnData(columnIndex);
        default -> readObjectColumnData(columnIndex);
      };
    } catch (final SQLException e) {
      LOGGER.log(
          Level.WARNING, e, new StringFormat("Could not read column at index %d", columnIndex));
      return ColumnDataIndicator.ERROR_DATA;
    }
  }

  public ResultSet getResults() {
    return results;
  }

  private Object readBinaryColumnData(final int columnIndex) throws SQLException {
    // The data has to be read in order to determine if it was null
    final Object value = readObjectColumnData(columnIndex);
    if (value == null) {
      return null;
    }
    return ColumnDataIndicator.BINARY_DATA;
  }

  private Object readCharacterColumnData(final int columnIndex) throws SQLException {
    // The data has to be read in order to determine if it was null
    final Reader reader = results.getCharacterStream(columnIndex);
    if (results.wasNull() || reader == null) {
      return null;
    }
    if (readLargeData) {
      return readFully(reader);
    }
    return ColumnDataIndicator.BINARY_DATA;
  }

  private Object readObjectColumnData(final int columnIndex) throws SQLException {
    // The data has to be read in order to determine if it was null
    final Object value = results.getObject(columnIndex);
    if (results.wasNull() || value == null) {
      return null;
    }
    return value;
  }
}
