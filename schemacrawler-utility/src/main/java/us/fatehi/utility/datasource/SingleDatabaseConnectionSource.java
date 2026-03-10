/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.datasource;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.requireNotBlank;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import us.fatehi.utility.SQLRuntimeException;
import us.fatehi.utility.database.DatabaseUtility;

final class SingleDatabaseConnectionSource extends AbstractDatabaseConnectionSource {

  private final Connection connection;

  public SingleDatabaseConnectionSource(final Connection connection) {
    try {
      this.connection = DatabaseUtility.checkConnection(connection);
    } catch (final SQLException e) {
      throw new SQLRuntimeException(e);
    }
  }

  SingleDatabaseConnectionSource(
      final String connectionUrl,
      final Set<String> additionalDriverProperties,
      final Map<String, String> connectionProperties,
      final UserCredentials userCredentials) {
    requireNotBlank(connectionUrl, "No database connection URL provided");
    requireNonNull(userCredentials, "No user credentials provided");

    final String user = userCredentials.user();
    final String password = userCredentials.password();
    final Properties jdbcConnectionProperties =
        createConnectionProperties(
            connectionUrl, additionalDriverProperties, connectionProperties, user, password);
    connection = getConnection(connectionUrl, jdbcConnectionProperties);
  }

  @Override
  public void close() throws Exception {
    connection.close();
  }

  @Override
  public Connection get() {
    initializeConnection(connection);
    return PooledConnectionUtility.newPooledConnection(connection, this);
  }

  @Override
  public boolean releaseConnection(final Connection connection) {
    // Do nothing, since connections are not closed
    return true;
  }
}
