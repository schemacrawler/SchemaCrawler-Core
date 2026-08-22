/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.database;

import static java.util.Objects.requireNonNull;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import us.fatehi.utility.property.VersionNumber;

public final class ConnectionInfoBuilder {

  private static final Logger LOGGER = Logger.getLogger(ConnectionInfoBuilder.class.getName());

  public static ConnectionInfoBuilder builder(final Connection connection) throws SQLException {
    return new ConnectionInfoBuilder(connection);
  }

  private static <T> T getConnectionInfoProperty(
      final Callable<T> propertyFunction, final T defaultValue) {
    if (propertyFunction == null) {
      return defaultValue;
    }
    try {
      return propertyFunction.call();
    } catch (final Exception e) {
      LOGGER.log(Level.FINE, "Could not get connection info property", e);
      return defaultValue;
    }
  }

  /**
   * Get database connection URL.
   *
   * <p>NOTE: Some databases such as Hive may throw an exception. See issue #910.
   *
   * @param dbMetaData Database metadata.
   * @return Database connection URL
   */
  private static String getConnectionUrl(final DatabaseMetaData dbMetaData) {
    if (dbMetaData == null) {
      return "";
    }
    try {
      return dbMetaData.getURL();
    } catch (final SQLException e) {
      LOGGER.log(Level.WARNING, "Could not obtain the database connection URL", e);
      return "";
    }
  }

  private final DatabaseMetaData dbMetaData;

  private ConnectionInfoBuilder(final Connection connection) throws SQLException {
    requireNonNull(connection, "No connection provided");
    dbMetaData = connection.getMetaData();
    requireNonNull(dbMetaData, "No database metadata available");
  }

  public DatabaseInformation buildDatabaseInformation() throws SQLException {
    return new DatabaseInformation(
        getConnectionInfoProperty(() -> dbMetaData.getDatabaseProductName(), ""),
        getConnectionInfoProperty(() -> dbMetaData.getDatabaseProductVersion(), ""),
        getConnectionInfoProperty(() -> dbMetaData.getUserName(), ""));
  }

  public JdbcDriverInformation buildJdbcDriverInformation() throws SQLException {
    final String connectionUrl = getConnectionUrl(dbMetaData);
    final JdbcDriverMetadata jdbcDriverMetadata =
        JdbcDriverRegistry.getRegistry().inspectMetadata(connectionUrl);
    final JdbcDriver jdbcDriver;
    if (jdbcDriverMetadata != null) {
      jdbcDriver = jdbcDriverMetadata.jdbcDriver();
    } else {
      jdbcDriver = new JdbcDriver();
    }

    return new JdbcDriverInformation(
        getConnectionInfoProperty(() -> dbMetaData.getDriverName(), ""),
        jdbcDriver.driverClassName(),
        getConnectionInfoProperty(() -> dbMetaData.getDriverVersion(), ""),
        jdbcDriver.driverVersionNumber(),
        new VersionNumber(
            getConnectionInfoProperty(() -> dbMetaData.getJDBCMajorVersion(), 0),
            getConnectionInfoProperty(() -> dbMetaData.getJDBCMinorVersion(), 0)),
        jdbcDriver.jdbcCompliant(),
        connectionUrl);
  }
}
