/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.utility;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.isBlank;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;
import schemacrawler.schemacrawler.exceptions.InternalRuntimeException;
import schemacrawler.tools.databaseconnector.DatabaseConnector;
import schemacrawler.tools.databaseconnector.DatabaseConnectorRegistry;
import us.fatehi.utility.UtilityMarker;
import us.fatehi.utility.database.DatabaseUtility;
import us.fatehi.utility.datasource.DatabaseConnectionSource;
import us.fatehi.utility.datasource.DatabaseServerType;
import us.fatehi.utility.jdbc.serverfingerprint.JdbcUrlParser;
import us.fatehi.utility.readconfig.SystemPropertiesConfig;

/** SchemaCrawler utility methods. */
@UtilityMarker
public final class DatabaseConnectorUtility {

  private static final Logger LOGGER = Logger.getLogger(DatabaseConnectorUtility.class.getName());

  /**
   * Returns database specific options using an existing SchemaCrawler database plugin.
   *
   * @return SchemaRetrievalOptions
   */
  public static SchemaRetrievalOptions matchSchemaRetrievalOptions(
      final DatabaseConnectionSource connectionSource) {
    try (final Connection connection = connectionSource.get()) {
      // Validate and open a live connection first, because plugin-specific schema
      // retrieval options may depend on database metadata from that connection.
      DatabaseUtility.checkConnection(connection);
      final DatabaseConnector dbConnector = findDatabaseConnector(connection);
      final SchemaRetrievalOptionsBuilder schemaRetrievalOptionsBuilder =
          dbConnector.getSchemaRetrievalOptionsBuilder(connection);
      final SchemaRetrievalOptions schemaRetrievalOptions = schemaRetrievalOptionsBuilder.build();
      return schemaRetrievalOptions;
    } catch (final SQLException e) {
      throw new InternalRuntimeException("Could not obtain schema retrieval options", e);
    }
  }

  /**
   * Updates the connection data source by attaching a connection initializer.
   *
   * @param connectionSource Database connection source
   * @param schemaRetrievalOptions SchemaCrawler retrieval options to convey the connection
   *     initializer from the database plugin
   */
  public static void updateConnectionDataSource(
      final DatabaseConnectionSource connectionSource,
      final SchemaRetrievalOptions schemaRetrievalOptions) {

    // Gracefully skip wiring if either side of the handoff is missing.
    if (connectionSource == null) {
      LOGGER.log(Level.CONFIG, "No database connection source provided");
      return;
    }
    if (schemaRetrievalOptions == null) {
      LOGGER.log(Level.CONFIG, "No schema retrieval options provided");
      return;
    }

    // Plugin-provided initializer must run on first connection to set
    // server-specific
    // session state before metadata crawling starts.
    connectionSource.setFirstConnectionInitializer(
        schemaRetrievalOptions.getConnectionInitializer());
  }

  private static DatabaseConnector findDatabaseConnector(final Connection connection) {
    requireNonNull(connection, "No database connection provided");

    // Resolve database type from JDBC URL, then look up the matching connector.
    final String connectionUrl = getConnectionUrl(connection);
    final String databaseSystemIdentifier =
        normalizedIdentifier(JdbcUrlParser.parse(connectionUrl).databaseSystemIdentifier());

    final DatabaseConnectorRegistry registry = DatabaseConnectorRegistry.getRegistry();
    final DatabaseConnector dbConnector = registry.getDatabaseConnector(databaseSystemIdentifier);
    final DatabaseServerType databaseServerType = dbConnector.getDatabaseServerType();

    // Enforce plugin requirements for supported databases unless explicitly
    // bypassed.
    throwIfDatabaseConnectorRequired(databaseSystemIdentifier, databaseServerType);

    // Log SchemaCrawler database connector being used
    if (databaseServerType.isUnknownDatabaseSystem()) {
      LOGGER.log(Level.INFO, "Not using any SchemaCrawler database connector");
    } else {
      LOGGER.log(
          Level.INFO,
          "Using SchemaCrawler database connector for <%s>".formatted(databaseServerType));
    }

    return dbConnector;
  }

  private static String getConnectionUrl(final Connection connection) {
    requireNonNull(connection, "No connection provided");
    final String url;
    try {
      // JDBC metadata URL is the canonical source for connector resolution.
      url = connection.getMetaData().getURL();
    } catch (final SQLException e) {
      // Callers treat blank URL as "cannot infer connector from URL".
      LOGGER.log(Level.CONFIG, "Could not obtain the database connection URL");
      return "";
    }
    return url;
  }

  private static String normalizedIdentifier(final String databaseSystemIdentifier) {
    if ("mariadb".equalsIgnoreCase(databaseSystemIdentifier)) {
      return "mysql";
    }
    return databaseSystemIdentifier;
  }

  private static void throwIfDatabaseConnectorRequired(
      final String databaseSystemIdentifier, final DatabaseServerType dbServerType) {
    if (isBlank(databaseSystemIdentifier) || dbServerType == null) {
      return;
    }

    final List<String> connectorsRequired =
        List.of("db2", "hsqldb", "mariadb", "mysql", "oracle", "postgresql", "sqlite", "sqlserver");
    final String allowedDatabaseConnector =
        normalizedIdentifier(
            new SystemPropertiesConfig().getStringValue("SC_WITHOUT_DATABASE_PLUGIN"));
    final boolean isAllowed =
        databaseSystemIdentifier.equalsIgnoreCase(allowedDatabaseConnector)
            || "mariadb".equalsIgnoreCase(databaseSystemIdentifier)
                && "mysql".equalsIgnoreCase(allowedDatabaseConnector);

    // Fail fast when a known database should have a plugin but none is available,
    // unless that database is explicitly allow-listed via
    // SC_WITHOUT_DATABASE_PLUGIN.
    if (dbServerType.isUnknownDatabaseSystem()
        && connectorsRequired.contains(databaseSystemIdentifier)
        && !isAllowed) {
      final String pluginId;
      if ("mariadb".equalsIgnoreCase(databaseSystemIdentifier)) {
        pluginId = "mysql";
      } else {
        pluginId = databaseSystemIdentifier;
      }
      throw new InternalRuntimeException(
          """
          Add the SchemaCrawler database connector plugin for <%s> to the CLASSPATH
          or set
          SC_WITHOUT_DATABASE_PLUGIN=%s
          either as an environmental variable or as a Java system property
          """
              .formatted(pluginId, pluginId));
    }
  }

  private DatabaseConnectorUtility() {
    // Prevent instantiation
  }
}
