/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility;

import static java.sql.Types.BLOB;
import static java.sql.Types.CLOB;
import static java.sql.Types.LONGVARBINARY;
import static java.sql.Types.LONGVARCHAR;
import static java.sql.Types.VARCHAR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;
import us.fatehi.utility.database.ColumnDataIndicator;
import us.fatehi.utility.database.DataResultSet;

public class DataResultSetTest {

  private static ResultSet mockResultSet(final int[] types) throws SQLException {
    final int columnCount = types.length;
    final ResultSet rs = mock(ResultSet.class);
    final ResultSetMetaData meta = mock(ResultSetMetaData.class);

    when(rs.getMetaData()).thenReturn(meta);
    when(meta.getColumnCount()).thenReturn(columnCount);
    for (int i = 1; i <= columnCount; i++) {
      when(meta.getColumnLabel(i)).thenReturn("COL" + i);
      when(meta.getColumnType(i)).thenReturn(types[i - 1]);
    }
    when(rs.getWarnings()).thenReturn(null);
    return rs;
  }

  @Test
  public void blobColumnReturnsBinaryData() throws Exception {
    final ResultSet rs = mockResultSet(new int[] {BLOB});
    when(rs.next()).thenReturn(true, false);
    when(rs.getObject(1)).thenReturn(mock(java.sql.Blob.class));
    when(rs.wasNull()).thenReturn(false);

    try (final DataResultSet dataResults = new DataResultSet(rs)) {
      dataResults.setMaxRows(100);
      dataResults.next();
      assertThat(dataResults.row().get(0), is(ColumnDataIndicator.BINARY_DATA));
    }
  }

  @Test
  public void clobWithShowLobsFalseReturnsBinaryData() throws Exception {
    final ResultSet rs = mockResultSet(new int[] {CLOB});
    when(rs.next()).thenReturn(true, false);
    when(rs.getCharacterStream(1)).thenReturn(new StringReader("lob content"));
    when(rs.wasNull()).thenReturn(false);

    try (final DataResultSet dataResults = new DataResultSet(rs)) {
      dataResults.setMaxRows(100);
      dataResults.setReadLargeData(false);
      dataResults.next();
      assertThat(dataResults.row().get(0), is(ColumnDataIndicator.BINARY_DATA));
    }
  }

  @Test
  public void clobWithShowLobsReturnsString() throws Exception {
    final ResultSet rs = mockResultSet(new int[] {CLOB});
    when(rs.next()).thenReturn(true, false);
    when(rs.getCharacterStream(1)).thenReturn(new StringReader("lob content"));
    when(rs.wasNull()).thenReturn(false);

    try (final DataResultSet dataResults = new DataResultSet(rs)) {
      dataResults.setMaxRows(100);
      dataResults.setReadLargeData(true);
      dataResults.next();
      assertThat(dataResults.row().get(0), is("lob content"));
    }
  }

  @Test
  public void closeClosesUnderlyingResultSet() throws Exception {
    final ResultSet rs = mockResultSet(new int[] {VARCHAR});

    final DataResultSet dataResults = new DataResultSet(rs);
    dataResults.close();

    verify(rs).close();
  }

  @Test
  public void columnNamesReturnedFromMetaData() throws Exception {
    final ResultSet rs = mockResultSet(new int[] {VARCHAR, VARCHAR});
    when(rs.getMetaData().getColumnLabel(1)).thenReturn("ID");
    when(rs.getMetaData().getColumnLabel(2)).thenReturn("NAME");

    try (final DataResultSet dataResults = new DataResultSet(rs)) {
      assertThat(dataResults.getColumnNames(), contains("ID", "NAME"));
    }
  }

  @Test
  public void columnReadExceptionReturnsErrorDataIndicator() throws Exception {
    final ResultSet rs = mockResultSet(new int[] {VARCHAR});
    when(rs.next()).thenReturn(true, false);
    when(rs.getObject(1)).thenThrow(new SQLException("read failure"));

    try (final DataResultSet dataResults = new DataResultSet(rs)) {
      dataResults.setMaxRows(100);
      dataResults.next();
      assertThat(dataResults.row().get(0), is(ColumnDataIndicator.ERROR_DATA));
    }
  }

  @Test
  public void errorDataIsSpecialColumnData() {
    assertThat(ColumnDataIndicator.ERROR_DATA, instanceOf(ColumnDataIndicator.class));
  }

  @Test
  public void errorDataToStringIsDescriptive() {
    assertThat(ColumnDataIndicator.ERROR_DATA.toString(), containsString("error"));
  }

  @Test
  public void longVarBinaryColumnReturnsBinaryData() throws Exception {
    final ResultSet rs = mockResultSet(new int[] {LONGVARBINARY});
    when(rs.next()).thenReturn(true, false);
    when(rs.getObject(1)).thenReturn(new byte[] {1, 2, 3});
    when(rs.wasNull()).thenReturn(false);

    try (final DataResultSet dataResults = new DataResultSet(rs)) {
      dataResults.setMaxRows(100);
      dataResults.next();
      assertThat(dataResults.row().get(0), is(ColumnDataIndicator.BINARY_DATA));
    }
  }

  @Test
  public void longVarCharWithShowLobsReturnsString() throws Exception {
    final ResultSet rs = mockResultSet(new int[] {LONGVARCHAR});
    when(rs.next()).thenReturn(true, false);
    when(rs.getCharacterStream(1)).thenReturn(new StringReader("long text"));
    when(rs.wasNull()).thenReturn(false);

    try (final DataResultSet dataResults = new DataResultSet(rs)) {
      dataResults.setMaxRows(100);
      dataResults.setReadLargeData(true);
      dataResults.next();
      assertThat(dataResults.row().get(0), is("long text"));
    }
  }

  @Test
  public void maxRowsLimitsIteration() throws Exception {
    final ResultSet rs = mockResultSet(new int[] {VARCHAR});
    when(rs.next()).thenReturn(true);

    try (final DataResultSet dataResults = new DataResultSet(rs)) {
      dataResults.setMaxRows(2);
      assertTrue(dataResults.next());
      assertTrue(dataResults.next());
      assertFalse(dataResults.next()); // maxRows reached
    }
  }

  @Test
  public void metaDataExceptionPropagates() throws Exception {
    final ResultSet rs = mock(ResultSet.class);
    when(rs.getMetaData()).thenThrow(new SQLException("meta error"));
    assertThrows(SQLException.class, () -> new DataResultSet(rs));
  }

  @Test
  public void negativeMaxRowsIsIgnored() throws Exception {
    final ResultSet rs = mockResultSet(new int[] {VARCHAR});
    when(rs.next()).thenReturn(true, false);

    try (final DataResultSet dataResults = new DataResultSet(rs)) {
      dataResults.setMaxRows(1);
      dataResults.setMaxRows(-5); // ignored; maxRows stays 1
      assertTrue(dataResults.next());
      assertFalse(dataResults.next());
    }
  }

  @Test
  public void nextReturnsTrueWhenRowsAvailable() throws Exception {
    final ResultSet rs = mockResultSet(new int[] {VARCHAR});
    when(rs.next()).thenReturn(true, false);

    try (final DataResultSet dataResults = new DataResultSet(rs)) {
      dataResults.setMaxRows(100);
      assertTrue(dataResults.next());
      assertFalse(dataResults.next());
    }
  }

  @Test
  public void nullBlobReturnsJavaNull() throws Exception {
    final ResultSet rs = mockResultSet(new int[] {BLOB});
    when(rs.next()).thenReturn(true, false);
    when(rs.getObject(1)).thenReturn(null);
    when(rs.wasNull()).thenReturn(true);

    try (final DataResultSet dataResults = new DataResultSet(rs)) {
      dataResults.setMaxRows(100);
      dataResults.next();
      assertThat(dataResults.row().get(0), is(nullValue()));
    }
  }

  @Test
  public void nullClobWithShowLobsFalseReturnsJavaNull() throws Exception {
    final ResultSet rs = mockResultSet(new int[] {CLOB});
    when(rs.next()).thenReturn(true, false);
    when(rs.getObject(1)).thenReturn(null);
    when(rs.wasNull()).thenReturn(true);

    try (final DataResultSet dataResults = new DataResultSet(rs)) {
      dataResults.setMaxRows(100);
      dataResults.setReadLargeData(false);
      dataResults.next();
      assertThat(dataResults.row().get(0), is(nullValue()));
    }
  }

  @Test
  public void nullClobWithShowLobsReturnsJavaNull() throws Exception {
    final ResultSet rs = mockResultSet(new int[] {CLOB});
    when(rs.next()).thenReturn(true, false);
    when(rs.getCharacterStream(1)).thenReturn(null);
    when(rs.wasNull()).thenReturn(true);

    try (final DataResultSet dataResults = new DataResultSet(rs)) {
      dataResults.setMaxRows(100);
      dataResults.next();
      assertThat(dataResults.row().get(0), is(nullValue()));
    }
  }

  @Test
  public void nullResultSetThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new DataResultSet(null));
  }

  @Test
  public void rowReturnsScalarValues() throws Exception {
    final ResultSet rs = mockResultSet(new int[] {VARCHAR, VARCHAR});
    when(rs.next()).thenReturn(true, false);
    when(rs.getObject(1)).thenReturn("hello");
    when(rs.wasNull()).thenReturn(false);
    when(rs.getObject(2)).thenReturn(42);

    try (final DataResultSet dataResults = new DataResultSet(rs)) {
      dataResults.setMaxRows(100);
      dataResults.next();
      final List<Object> row = dataResults.row();
      assertThat(row, hasSize(2));
      assertThat(row.get(0), is("hello"));
      assertThat(row.get(1), is(42));
    }
  }

  @Test
  public void sqlNullReturnsJavaNull() throws Exception {
    final ResultSet rs = mockResultSet(new int[] {VARCHAR});
    when(rs.next()).thenReturn(true, false);
    when(rs.getObject(1)).thenReturn(null);
    when(rs.wasNull()).thenReturn(true);

    try (final DataResultSet dataResults = new DataResultSet(rs)) {
      dataResults.setMaxRows(100);
      dataResults.next();
      assertThat(dataResults.row().get(0), is(nullValue()));
    }
  }

  @Test
  public void zeroMaxRowsReturnsNoRows() throws Exception {
    final ResultSet rs = mockResultSet(new int[] {VARCHAR});
    when(rs.next()).thenReturn(true);

    try (final DataResultSet dataResults = new DataResultSet(rs)) {
      dataResults.setMaxRows(0);
      assertFalse(dataResults.next());
    }
  }
}
