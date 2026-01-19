/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.databaseconnector;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.isBlank;

import java.sql.Connection;
import java.util.Map;
import schemacrawler.schemacrawler.InformationSchemaViews;
import schemacrawler.schemacrawler.InformationSchemaViewsBuilder;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;
import schemacrawler.schemacrawler.exceptions.ConfigurationException;
import schemacrawler.tools.executable.commandline.PluginCommand;
import us.fatehi.utility.datasource.DatabaseConnectionSource;
import us.fatehi.utility.datasource.DatabaseConnectionSourceBuilder;
import us.fatehi.utility.datasource.DatabaseServerType;
import us.fatehi.utility.datasource.UserCredentials;

public abstract class DatabaseConnector {

  private final DatabaseConnectorOptions options;

  protected DatabaseConnector(final DatabaseConnectorOptions options) {
    this.options = requireNonNull(options, "No database connector options provided");
  }

  public final DatabaseServerType getDatabaseServerType() {
    return options.dbServerType();
  }

  public final PluginCommand getHelpCommand() {
    return options.helpCommand();
  }

  /**
   * Gets the complete bundled database specific configuration set, including the SQL for
   * information schema views.
   *
   * @param connection Database connection
   */
  public final SchemaRetrievalOptionsBuilder getSchemaRetrievalOptionsBuilder(
      final Connection connection) {

    final DatabaseConnectionSourceBuilder dbConnectionSourceBuilder =
        options.dbConnectionSourceBuildProcess().get();
    final InformationSchemaViews informationSchemaViews =
        InformationSchemaViewsBuilder.builder()
            .withFunction(options.informationSchemaViewsBuildProcess(), connection)
            .toOptions();
    final SchemaRetrievalOptionsBuilder schemaRetrievalOptionsBuilder =
        SchemaRetrievalOptionsBuilder.builder()
            .withDatabaseServerType(options.dbServerType())
            .withInformationSchemaViews(informationSchemaViews)
            .withConnectionInitializer(dbConnectionSourceBuilder.getConnectionInitializer())
            .fromConnnection(connection);

    // Allow database plugins to intercept and do further customization
    options.schemaRetrievalOptionsBuildProcess().accept(schemaRetrievalOptionsBuilder, connection);

    return schemaRetrievalOptionsBuilder;
  }

  /**
   * Creates a datasource for connecting to a database.
   *
   * @param connectionOptions Connection options
   * @param userCredentials Username and password
   * @return Database connection source
   */
  public final DatabaseConnectionSource newDatabaseConnectionSource(
      final DatabaseConnectionOptions connectionOptions, final UserCredentials userCredentials) {
    requireNonNull(connectionOptions, "No database connection options provided");

    // Connect using connection options provided from the command-line,
    // provided configuration, and bundled configuration
    final DatabaseConnectionSourceBuilder dbConnectionSourceBuilder;
    if (connectionOptions
        instanceof final DatabaseUrlConnectionOptions databaseUrlConnectionOptions) {

      final String connectionUrl = databaseUrlConnectionOptions.connectionUrl();

      dbConnectionSourceBuilder = DatabaseConnectionSourceBuilder.builder(connectionUrl);
    } else if (connectionOptions
        instanceof final DatabaseServerHostConnectionOptions serverHostConnectionOptions) {

      final String host = serverHostConnectionOptions.host();
      final Integer port = serverHostConnectionOptions.port();
      final String database = serverHostConnectionOptions.database();
      final Map<String, String> urlx = serverHostConnectionOptions.urlx();

      dbConnectionSourceBuilder = databaseConnectionSourceBuilder();
      dbConnectionSourceBuilder.withHost(host);
      dbConnectionSourceBuilder.withPort(port);
      dbConnectionSourceBuilder.withDatabase(database);
      dbConnectionSourceBuilder.withUrlx(urlx);

    } else {
      throw new ConfigurationException("Could not create new database connection source");
    }

    dbConnectionSourceBuilder.withUserCredentials(userCredentials);
    final DatabaseConnectionSource databaseConnectionSource = dbConnectionSourceBuilder.build();
    return databaseConnectionSource;
  }

  public final boolean supportsUrl(final String url) {
    if (isBlank(url)) {
      return false;
    }
    return options.supportsUrl().test(url);
  }

  @Override
  public final String toString() {
    if (options.dbServerType().isUnknownDatabaseSystem()) {
      return "Database connector for unknown database system type";
    }
    return "Database connector for " + options.dbServerType();
  }

  public final SchemaCrawlerOptions withSchemaCrawlerOptionsDefaults(
      final SchemaCrawlerOptions schemaCrawlerOptions) {
    final LimitOptionsBuilder limitOptionsBuilder =
        LimitOptionsBuilder.builder().fromOptions(schemaCrawlerOptions.limitOptions());
    options.limitOptionsBuildProcess().accept(limitOptionsBuilder);

    return schemaCrawlerOptions.withLimitOptions(limitOptionsBuilder.toOptions());
  }

  protected DatabaseConnectionSourceBuilder databaseConnectionSourceBuilder() {
    return options.dbConnectionSourceBuildProcess().get();
  }
}
