/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.datasource;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.isBlank;
import static us.fatehi.utility.Utility.requireNotBlank;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import us.fatehi.utility.database.DatabaseUtility;
import us.fatehi.utility.string.StringFormat;

final class SimpleDatabaseConnectionSource extends AbstractDatabaseConnectionSource {

  private static final Logger LOGGER =
      Logger.getLogger(SimpleDatabaseConnectionSource.class.getName());

  private final String connectionUrl;
  private final Properties jdbcConnectionProperties;
  private final Deque<Connection> connectionPool;
  private final Deque<Connection> usedConnections;
  private volatile boolean isClosed;

  SimpleDatabaseConnectionSource(
      final String connectionUrl,
      final Set<String> additionalDriverProperties,
      final Map<String, String> connectionProperties,
      final UserCredentials userCredentials) {

    this.connectionUrl = requireNotBlank(connectionUrl, "No database connection URL provided");
    requireNonNull(userCredentials, "No user credentials provided");

    final String user = userCredentials.user();
    final String password = userCredentials.password();
    if (isBlank(user)) {
      LOGGER.log(Level.WARNING, "Database user is not provided");
    }
    if (isBlank(password)) {
      LOGGER.log(Level.WARNING, "Database password is not provided");
    }

    jdbcConnectionProperties =
        createConnectionProperties(
            connectionUrl, additionalDriverProperties, connectionProperties, user, password);

    connectionPool = new LinkedBlockingDeque<>();
    usedConnections = new LinkedBlockingDeque<>();

    // Explicitly set closed flag
    isClosed = false;
  }

  @Override
  public synchronized void close() throws Exception {

    if (isClosed) {
      LOGGER.log(Level.INFO, "Database connection source is already closed");
      return;
    }

    final List<Connection> connections = new ArrayList<>(connectionPool);
    connections.addAll(usedConnections);

    for (final Connection connection : connections) {
      try {
        connection.close();
        LOGGER.log(Level.INFO, new StringFormat("Closed database connection <%s>", connection));
      } catch (final Exception e) {
        LOGGER.log(Level.WARNING, "Cannot close connection", e);
      }
    }

    if (!usedConnections.isEmpty()) {
      LOGGER.log(Level.SEVERE, "Abnormal termination - not all database connections are closed");
    }

    connectionPool.clear();
    usedConnections.clear();

    isClosed = true;
  }

  @Override
  public synchronized Connection get() {
    if (isClosed) {
      throw new IllegalStateException("Database connection source is already closed");
    }

    // Create a connection if needed
    if (connectionPool.isEmpty()) {
      final Connection connection = getConnection(connectionUrl, jdbcConnectionProperties);
      connectionPool.add(connection);
    }

    // Mark connection as in-use
    final Connection connection = connectionPool.removeFirst();
    usedConnections.add(connection);

    // (Connection is checked during the initialization process)
    initializeConnection(connection);
    return PooledConnectionUtility.newPooledConnection(connection, this);
  }

  @Override
  public synchronized boolean releaseConnection(final Connection connection) {
    if (isClosed) {
      throw new IllegalStateException("Database connection source is already closed");
    }

    final boolean removed = usedConnections.remove(connection);

    try {
      final Connection unwrappedConnection = connection.unwrap(Connection.class);
      DatabaseUtility.checkConnection(unwrappedConnection);
    } catch (final SQLException e) {
      LOGGER.log(
          Level.WARNING,
          "Cannot check connection before returning to the pool - " + e.getMessage());
      LOGGER.log(Level.FINE, "Cannot check connection before returning to the pool - ", e);
      // Do not add an invalid connection back to the pool
      return removed;
    }

    if (removed) {
      connectionPool.add(connection);
    }

    return removed;
  }
}
