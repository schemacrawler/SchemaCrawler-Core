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
import java.util.logging.Level;
import java.util.logging.Logger;
import us.fatehi.utility.SQLRuntimeException;
import us.fatehi.utility.database.DatabaseUtility;

/**
 * Wraps a single connection as a connection source. It does not account for connections to be
 * closed after unwrapping.
 *
 * <p><b>IMPORTANT: This connection source is not thread-safe.</b>
 */
final class SingleDatabaseConnectionSource extends AbstractDatabaseConnectionSource {

  private static final Logger LOGGER =
      Logger.getLogger(SingleDatabaseConnectionSource.class.getName());

  private static Connection connect(
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
    final Connection connection = getConnection(connectionUrl, jdbcConnectionProperties);
    return connection;
  }

  private final Connection connection;
  private volatile boolean isClosed;

  SingleDatabaseConnectionSource(final Connection connection) {
    this.connection = requireNonNull(connection, "No connection provided");
    checkConnection();

    // Explicitly set closed flag
    isClosed = false;
  }

  SingleDatabaseConnectionSource(
      final String connectionUrl,
      final Set<String> additionalDriverProperties,
      final Map<String, String> connectionProperties,
      final UserCredentials userCredentials) {
    this(connect(connectionUrl, additionalDriverProperties, connectionProperties, userCredentials));
  }

  @Override
  public synchronized void close() throws Exception {
    if (isClosed) {
      LOGGER.log(Level.INFO, "Database connection source is already closed");
      return;
    }

    connection.close();

    isClosed = true;
  }

  @Override
  public synchronized Connection get() {
    if (isClosed) {
      throw new IllegalStateException("Database connection source is already closed");
    }

    // Need to re-initialize, since the initializer may be set after instantiation
    // (Connection is checked during the initialization process)
    initializeConnection(connection);
    return PooledConnectionUtility.newPooledConnection(connection, this);
  }

  @Override
  public synchronized boolean releaseConnection(final Connection connection) {
    if (isClosed) {
      throw new IllegalStateException("Database connection source is already closed");
    }

    checkConnection();

    // Do nothing, since connections are not closed
    return true;
  }

  private void checkConnection() {
    try {
      DatabaseUtility.checkConnection(connection);
    } catch (final SQLException e) {
      throw new SQLRuntimeException(e);
    }
  }
}
