/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.datasource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

class SingleDatabaseConnectionSourceCloseStateTest {

  @Test
  void closeFailureKeepsStateClosed() throws Exception {
    final Connection connection = mock(Connection.class);
    when(connection.isValid(5)).thenReturn(true);
    doThrow(new SQLException("close failure")).doNothing().when(connection).close();

    final SingleDatabaseConnectionSource source = new SingleDatabaseConnectionSource(connection);

    assertThrows(SQLException.class, source::close);
    assertThrows(IllegalStateException.class, source::get);
    assertThrows(IllegalStateException.class, () -> source.releaseConnection(null));

    source.close();

    verify(connection, times(1)).close();
  }

  @Test
  void closeTwiceClosesUnderlyingConnectionOnlyOnce() throws Exception {
    final Connection connection = mock(Connection.class);
    when(connection.isValid(5)).thenReturn(true);
    doNothing().when(connection).close();

    final SingleDatabaseConnectionSource source = new SingleDatabaseConnectionSource(connection);

    source.close();
    source.close();

    verify(connection, times(1)).close();
  }
}
