/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import us.fatehi.utility.database.JdbcConnectionUtility;

public class JdbcConnectionUtilityTest {

  @Test
  public void connectionReturnsEmptyStringWhenConnectionIsNull() {
    assertThat(JdbcConnectionUtility.getConnectionUrl((Connection) null), is(emptyString()));
  }

  @Test
  public void connectionReturnsEmptyStringWhenGetMetaDataReturnsNull() throws SQLException {
    final Connection connection = mock(Connection.class);
    when(connection.getMetaData()).thenReturn(null);

    assertThat(JdbcConnectionUtility.getConnectionUrl(connection), is(emptyString()));
  }

  @Test
  public void connectionReturnsEmptyStringWhenGetMetaDataThrows() throws SQLException {
    final Connection connection = mock(Connection.class);
    when(connection.getMetaData()).thenThrow(new SQLException("Could not get metadata"));

    assertThat(JdbcConnectionUtility.getConnectionUrl(connection), is(emptyString()));
  }

  @Test
  public void connectionReturnsEmptyStringWhenGetUrlThrows() throws SQLException {
    final DatabaseMetaData dbMetaData = mock(DatabaseMetaData.class);
    when(dbMetaData.getURL()).thenThrow(new SQLException("Could not get URL"));
    final Connection connection = mock(Connection.class);
    when(connection.getMetaData()).thenReturn(dbMetaData);

    assertThat(JdbcConnectionUtility.getConnectionUrl(connection), is(emptyString()));
  }

  @Test
  public void connectionReturnsUrlFromMetaData() throws SQLException {
    final DatabaseMetaData dbMetaData = mock(DatabaseMetaData.class);
    when(dbMetaData.getURL()).thenReturn("jdbc:mocked://mockedconnection");
    final Connection connection = mock(Connection.class);
    when(connection.getMetaData()).thenReturn(dbMetaData);

    assertThat(
        JdbcConnectionUtility.getConnectionUrl(connection), is("jdbc:mocked://mockedconnection"));

    verify(connection).getMetaData();
    verify(dbMetaData).getURL();
    verifyNoMoreInteractions(connection, dbMetaData);
  }

  @Test
  public void metaDataReturnsEmptyStringWhenDatabaseMetaDataIsNull() {
    assertThat(JdbcConnectionUtility.getConnectionUrl((DatabaseMetaData) null), is(emptyString()));
  }

  @Test
  public void metaDataReturnsEmptyStringWhenGetUrlThrows() throws SQLException {
    final DatabaseMetaData dbMetaData = mock(DatabaseMetaData.class);
    // See issue #910
    when(dbMetaData.getURL()).thenThrow(new SQLException("Method not supported"));

    assertThat(JdbcConnectionUtility.getConnectionUrl(dbMetaData), is(emptyString()));
  }

  @Test
  public void metaDataReturnsNullWhenDriverReturnsNullUrl() throws SQLException {
    final DatabaseMetaData dbMetaData = mock(DatabaseMetaData.class);
    when(dbMetaData.getURL()).thenReturn(null);

    assertThat(JdbcConnectionUtility.getConnectionUrl(dbMetaData), is(nullValue()));
  }

  @Test
  public void metaDataReturnsUrl() throws SQLException {
    final DatabaseMetaData dbMetaData = mock(DatabaseMetaData.class);
    when(dbMetaData.getURL()).thenReturn("jdbc:hsqldb:hsql://localhost:9001/schemacrawler");

    assertThat(
        JdbcConnectionUtility.getConnectionUrl(dbMetaData),
        is("jdbc:hsqldb:hsql://localhost:9001/schemacrawler"));
  }
}
