/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.databaseconnector;

import static java.util.Comparator.naturalOrder;
import static schemacrawler.tools.databaseconnector.UnknownDatabaseConnector.UNKNOWN;
import static us.fatehi.utility.Utility.isBlank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schemacrawler.exceptions.InternalRuntimeException;
import schemacrawler.tools.command.PluginCommandRegistry;
import schemacrawler.tools.executable.commandline.PluginCommand;
import schemacrawler.tools.registry.BasePluginRegistry;
import us.fatehi.utility.datasource.DatabaseServerType;
import us.fatehi.utility.property.PropertyName;
import us.fatehi.utility.string.StringFormat;

/** Registry for database plugins. */
public final class DatabaseConnectorRegistry extends BasePluginRegistry
    implements PluginCommandRegistry {

  private static final Logger LOGGER = Logger.getLogger(DatabaseConnectorRegistry.class.getName());

  private static DatabaseConnectorRegistry registrySingleton;

  public static DatabaseConnectorRegistry getRegistry() {
    if (registrySingleton == null) {
      registrySingleton = new DatabaseConnectorRegistry();
      registrySingleton.log();
    }
    return registrySingleton;
  }

  private static void addDatabaseConnector(
      final DatabaseConnector databaseConnector,
      final Map<String, DatabaseConnector> databaseConnectorRegistry) {
    final String databaseSystemIdentifier =
        databaseConnector.getDatabaseServerType().getDatabaseSystemIdentifier();
    if (databaseConnectorRegistry.containsKey(databaseSystemIdentifier)) {
      LOGGER.log(
          Level.WARNING,
          new StringFormat(
              "Skipping database connector, %s=%s (already registered)",
              databaseSystemIdentifier, databaseConnector.getClass().getName()));
      return;
    }
    LOGGER.log(
        Level.CONFIG,
        new StringFormat(
            "Loading database connector, %s=%s",
            databaseSystemIdentifier, databaseConnector.getClass().getName()));
    databaseConnectorRegistry.put(databaseSystemIdentifier, databaseConnector);

    // Special case: MariaDB is handled by the MySQL plugin
    if ("mysql".equals(databaseSystemIdentifier)) {
      databaseConnectorRegistry.put("mariadb", databaseConnector);
    }
  }

  private static Map<String, DatabaseConnector> loadDatabaseConnectorRegistry() {

    // Use thread-safe map
    final Map<String, DatabaseConnector> databaseConnectorRegistry = new ConcurrentHashMap<>();
    final List<DatabaseConnectorBundle> databaseConnectorBundles = new ArrayList<>();

    try {
      final ServiceLoader<DatabaseConnector> serviceLoader =
          ServiceLoader.load(
              DatabaseConnector.class, DatabaseConnectorRegistry.class.getClassLoader());
      for (final DatabaseConnector databaseConnector : serviceLoader) {
        if (databaseConnector instanceof final DatabaseConnectorBundle databaseConnectorBundle) {
          // Save the bundle for later processing
          // We do not want lightweight bundles to override connectors that are implemented with
          // code
          databaseConnectorBundles.add(databaseConnectorBundle);
        } else {
          addDatabaseConnector(databaseConnector, databaseConnectorRegistry);
        }
      }
    } catch (final Exception | ServiceConfigurationError | LinkageError e) {
      // Catch errors for missing third-party jars;
      // other errors (e.g. OutOfMemoryError) are intentionally not caught here
      throw new InternalRuntimeException("Could not load database connector registry", e);
    }

    for (final DatabaseConnectorBundle databaseConnectorBundle : databaseConnectorBundles) {
      for (final DatabaseConnector databaseConnector :
          databaseConnectorBundle.getDatabaseConnectors()) {
        addDatabaseConnector(databaseConnector, databaseConnectorRegistry);
      }
    }

    LOGGER.log(
        Level.CONFIG,
        new StringFormat("Loaded %d database connectors", databaseConnectorRegistry.size()));

    return databaseConnectorRegistry;
  }

  private final Map<String, DatabaseConnector> databaseConnectorRegistry;

  private DatabaseConnectorRegistry() {
    super("SchemaCrawler Database Server Plugins");
    databaseConnectorRegistry = loadDatabaseConnectorRegistry();
  }

  @Override
  public Collection<PluginCommand> getCommandLineCommands() {
    return List.of();
  }

  public DatabaseConnector getDatabaseConnector(final String databaseSystemIdentifier) {
    if (isBlank(databaseSystemIdentifier)) {
      return UNKNOWN;
    }
    if (hasDatabaseSystemIdentifier(databaseSystemIdentifier)) {
      return databaseConnectorRegistry.get(databaseSystemIdentifier);
    }
    return UNKNOWN;
  }

  public List<DatabaseServerType> getDatabaseServerTypes() {
    final List<DatabaseServerType> databaseServerTypes = new ArrayList<>();
    for (final DatabaseConnector databaseConnector : databaseConnectorRegistry.values()) {
      databaseServerTypes.add(databaseConnector.getDatabaseServerType());
    }
    databaseServerTypes.sort(naturalOrder());
    return databaseServerTypes;
  }

  @Override
  public Collection<PluginCommand> getHelpCommands() {
    final Collection<PluginCommand> commandLineHelpCommands = new ArrayList<>();
    for (final DatabaseConnector databaseConnector : databaseConnectorRegistry.values()) {
      commandLineHelpCommands.add(databaseConnector.getHelpCommand());
    }
    return commandLineHelpCommands;
  }

  @Override
  public Collection<PropertyName> getRegisteredPlugins() {
    final List<PropertyName> availableServers = new ArrayList<>();
    for (final DatabaseServerType serverType : getDatabaseServerTypes()) {
      final PropertyName serverDescription =
          new PropertyName(
              serverType.getDatabaseSystemIdentifier(), serverType.getDatabaseSystemName());
      availableServers.add(serverDescription);
    }
    Collections.sort(availableServers);
    return availableServers;
  }

  public boolean hasDatabaseSystemIdentifier(final String databaseSystemIdentifier) {
    if (isBlank(databaseSystemIdentifier)) {
      return false;
    }
    return databaseConnectorRegistry.containsKey(databaseSystemIdentifier);
  }
}
