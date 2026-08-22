/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.database;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.isBlank;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import us.fatehi.utility.UtilityMarker;
import us.fatehi.utility.property.VersionNumber;
import us.fatehi.utility.string.StringFormat;

@UtilityMarker
public final class JdbcDriverRegistry {

  private static final Logger LOGGER = Logger.getLogger(JdbcDriverRegistry.class.getName());

  private static final Object LOCK = new Object();

  private static volatile Collection<JdbcDriver> cachedDrivers;

  public static Connection createConnection(
      final String connectionUrl, final java.util.Properties connectionProperties)
      throws SQLException {
    requireConnectionUrl(connectionUrl);
    final java.util.Properties properties =
        connectionProperties == null ? new java.util.Properties() : connectionProperties;
    final Driver jdbcDriver = getJdbcDriver(connectionUrl);
    return jdbcDriver.connect(connectionUrl, properties);
  }

  public static Collection<JdbcDriver> discoverAvailableDrivers() {
    if (cachedDrivers == null) {
      synchronized (LOCK) {
        if (cachedDrivers == null) {
          try {
            cachedDrivers = performDiscovery();
          } catch (final SQLException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            cachedDrivers = List.of();
          }
        }
      }
    }
    return cachedDrivers;
  }

  public static JdbcDriverMetadata inspectMetadata(final String connectionUrl) throws SQLException {
    requireConnectionUrl(connectionUrl);
    final Driver jdbcDriver = getJdbcDriver(connectionUrl);
    final DriverPropertyInfo[] properties =
        jdbcDriver.getPropertyInfo(connectionUrl, new java.util.Properties());
    final List<JdbcDriverProperty> jdbcDriverProperties = new ArrayList<>();
    for (final DriverPropertyInfo propertyInfo : properties) {
      jdbcDriverProperties.add(toJdbcDriverProperty(propertyInfo));
    }
    return new JdbcDriverMetadata(toJdbcDriver(jdbcDriver), jdbcDriverProperties);
  }

  public static JdbcDriver resolveDriverForUrl(final String connectionUrl) throws SQLException {
    requireConnectionUrl(connectionUrl);
    return toJdbcDriver(getJdbcDriver(connectionUrl));
  }

  private static Driver getJdbcDriver(final String connectionUrl) throws SQLException {
    try {
      final Driver jdbcDriver = DriverManager.getDriver(connectionUrl);
      if (jdbcDriver == null) {
        throw new SQLException(
            "Could not find a suitable JDBC driver for database connection URL <%s>"
                .formatted(connectionUrl));
      }
      return jdbcDriver;
    } catch (final SQLException e) {
      throw new SQLException(
          "Could not find a suitable JDBC driver for database connection URL <%s>"
              .formatted(connectionUrl),
          e);
    }
  }

  private static Collection<JdbcDriver> performDiscovery() throws SQLException {
    final List<JdbcDriver> jdbcDrivers = new ArrayList<>();
    final ServiceLoader<Driver> serviceLoader = ServiceLoader.load(Driver.class);
    serviceLoader.stream()
        .forEach(
            driverProvider -> {
              try {
                final Driver driver = driverProvider.get();
                jdbcDrivers.add(toJdbcDriver(driver));
                LOGGER.log(
                    Level.FINE,
                    new StringFormat("Found JDBC driver <%s>", driver.getClass().getName()));
              } catch (final Exception | ServiceConfigurationError | LinkageError e) {
                LOGGER.log(
                    Level.FINE,
                    e,
                    new StringFormat("Could not load JDBC driver provider <%s>", driverProvider));
              }
            });

    if (jdbcDrivers.isEmpty()) {
      throw new SQLException("No database drivers are available");
    }

    LOGGER.log(
        Level.CONFIG,
        new StringFormat(
            "Registered JDBC Drivers: %s",
            jdbcDrivers.stream()
                .map(JdbcDriver::driverClassName)
                .sorted()
                .collect(Collectors.joining(", "))));
    return List.copyOf(jdbcDrivers);
  }

  private static void requireConnectionUrl(final String connectionUrl) throws SQLException {
    if (isBlank(connectionUrl)) {
      throw new SQLException("No database connection URL provided");
    }
  }

  private static JdbcDriver toJdbcDriver(final Driver driver) {
    requireNonNull(driver, "No JDBC driver provided");
    final int driverMajorVersion = driver.getMajorVersion();
    final int driverMinorVersion = driver.getMinorVersion();
    return new JdbcDriver(
        driver.getClass().getName(),
        new VersionNumber(driverMajorVersion, driverMinorVersion),
        new VersionNumber(0, 0),
        driver.jdbcCompliant());
  }

  private static JdbcDriverProperty toJdbcDriverProperty(final DriverPropertyInfo propertyInfo) {
    requireNonNull(propertyInfo, "No JDBC driver property info provided");
    final List<String> choices;
    if (propertyInfo.choices == null || propertyInfo.choices.length == 0) {
      choices = List.of();
    } else {
      choices = List.of(propertyInfo.choices);
    }
    return new JdbcDriverProperty(
        propertyInfo.name,
        propertyInfo.description,
        propertyInfo.required,
        propertyInfo.value,
        choices);
  }

  private JdbcDriverRegistry() {
    // Prevent instantiation
  }
}
