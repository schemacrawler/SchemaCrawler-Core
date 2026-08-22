/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.database;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.requireNotBlank;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import us.fatehi.utility.SQLRuntimeException;
import us.fatehi.utility.UtilityMarker;
import us.fatehi.utility.property.PropertyName;
import us.fatehi.utility.property.VersionNumber;
import us.fatehi.utility.string.StringFormat;

@UtilityMarker
public final class JdbcDriverRegistry {

  private static final Logger LOGGER = Logger.getLogger(JdbcDriverRegistry.class.getName());
  private static JdbcDriverRegistry registrySingleton;

  public static JdbcDriverRegistry getRegistry() {
    if (registrySingleton == null) {
      registrySingleton = new JdbcDriverRegistry();
    }
    return registrySingleton;
  }

  private static Map<String, JdbcDriver> loadJdbcDriverRegistry() {
    try {
      final Map<String, JdbcDriver> jdbcDrivers = new LinkedHashMap<>();
      final ServiceLoader<Driver> serviceLoader = ServiceLoader.load(Driver.class);
      serviceLoader.stream()
          .forEach(
              driverProvider -> {
                try {
                  final Driver driver = driverProvider.get();
                  final JdbcDriver jdbcDriver = toJdbcDriver(driver);
                  final String driverClassName = jdbcDriver.driverClassName();
                  if (!jdbcDrivers.containsKey(driverClassName)) {
                    jdbcDrivers.put(driverClassName, jdbcDriver);
                    LOGGER.log(
                        Level.FINE, new StringFormat("Found JDBC driver <%s>", driverClassName));
                  } else {
                    LOGGER.log(
                        Level.FINE,
                        new StringFormat("Skipping duplicate JDBC driver <%s>", driverClassName));
                  }
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
              jdbcDrivers.keySet().stream().sorted().collect(Collectors.joining(", "))));

      return Map.copyOf(jdbcDrivers);
    } catch (final SQLException e) {
      LOGGER.log(Level.WARNING, e.getMessage(), e);
      return Map.of();
    }
  }

  private static JdbcDriver toJdbcDriver(final Driver driver) {
    requireNonNull(driver, "No JDBC driver provided");
    final int driverMajorVersion = driver.getMajorVersion();
    final int driverMinorVersion = driver.getMinorVersion();
    return new JdbcDriver(
        driver.getClass().getName(),
        new VersionNumber(driverMajorVersion, driverMinorVersion),
        driver.jdbcCompliant());
  }

  private static Collection<JdbcDriverProperty> toJdbcDriverProperties(
      final Driver driver, final String connectionUrl) {
    requireNonNull(driver, "No JDBC driver provided");
    try {
      final DriverPropertyInfo[] propertyInfos =
          driver.getPropertyInfo(connectionUrl, new Properties());
      final List<JdbcDriverProperty> jdbcDriverProperties = new ArrayList<>();
      for (final DriverPropertyInfo propertyInfo : propertyInfos) {
        if (propertyInfo == null) {
          continue;
        }
        final List<String> choices;
        if (propertyInfo.choices == null) {
          choices = List.of();
        } else {
          choices = Arrays.asList(propertyInfo.choices);
        }
        final JdbcDriverProperty jdbcDriverPropertyInfo =
            new JdbcDriverProperty(
                propertyInfo.name,
                propertyInfo.description,
                propertyInfo.required,
                propertyInfo.value,
                choices);
        jdbcDriverProperties.add(jdbcDriverPropertyInfo);
      }
      return List.copyOf(jdbcDriverProperties);
    } catch (final SQLException | RuntimeException e) {
      LOGGER.log(
          Level.WARNING,
          e,
          new StringFormat(
              "Could not load JDBC driver properties for <%s> at <%s>",
              driver.getClass().getName(), connectionUrl));
      return List.of();
    }
  }

  private final Map<String, JdbcDriver> cachedDrivers;

  private JdbcDriverRegistry() {
    cachedDrivers = new LinkedHashMap<>(loadJdbcDriverRegistry());
  }

  public Collection<PropertyName> availableJDBCDrivers() {
    final Collection<PropertyName> availableJDBCDrivers = new ArrayList<>();
    for (final JdbcDriver jdbcDriver : cachedDrivers.values()) {
      availableJDBCDrivers.add(
          new PropertyName(
              jdbcDriver.driverClassName(), jdbcDriver.driverVersionNumber().toString()));
    }
    return List.copyOf(availableJDBCDrivers);
  }

  public Connection createConnection(
      final String connectionUrl, final Properties connectionProperties) throws SQLException {
    requireNotBlank(connectionUrl, "No JDBC connection URL provided");
    final Properties properties =
        connectionProperties == null ? new Properties() : connectionProperties;
    final Optional<Driver> jdbcDriverOptional = lookupDriver(connectionUrl);
    if (jdbcDriverOptional.isEmpty()) {
      throw new SQLException(
          "Could not find a suitable JDBC driver for database connection URL <%s>"
              .formatted(connectionUrl));
    }

    final Driver driver = jdbcDriverOptional.get();
    return driver.connect(connectionUrl, properties);
  }

  public JdbcDriverMetadata inspectMetadata(final String connectionUrl) {
    requireNotBlank(connectionUrl, "No JDBC connection URL provided");
    final Optional<Driver> jdbcDriverOptional = lookupDriver(connectionUrl);
    if (jdbcDriverOptional.isEmpty()) {
      return new JdbcDriverMetadata();
    }

    final Driver driver = jdbcDriverOptional.get();
    final String driverClassName = driver.getClass().getName();
    final JdbcDriver jdbcDriver;
    if (cachedDrivers.containsKey(driverClassName)) {
      jdbcDriver = cachedDrivers.get(driverClassName);
    } else {
      jdbcDriver = new JdbcDriver();
    }
    final JdbcDriverMetadata metadata =
        new JdbcDriverMetadata(jdbcDriver, toJdbcDriverProperties(driver, connectionUrl));
    return metadata;
  }

  private Optional<Driver> lookupDriver(final String connectionUrl) {
    try {
      final Driver jdbcDriver = DriverManager.getDriver(connectionUrl);
      return Optional.ofNullable(jdbcDriver);
    } catch (final SQLException e) {
      if ("08001".equals(e.getSQLState())) {
        return Optional.empty();
      }
      throw new SQLRuntimeException(
          "Could not find a suitable JDBC driver for database connection URL <%s>"
              .formatted(connectionUrl),
          e);
    }
  }
}
