/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility;

import java.util.Set;
import schemacrawler.tools.databaseconnector.DatabaseConnector;
import schemacrawler.tools.databaseconnector.DatabaseConnectorOptions;
import schemacrawler.tools.databaseconnector.DatabaseConnectorOptionsBuilder;
import schemacrawler.tools.executable.commandline.PluginCommand;
import us.fatehi.utility.datasource.DatabaseConnectionSourceBuilder;
import us.fatehi.utility.datasource.DatabaseServerType;

/**
 * SchemaCrawler database support plug-in.
 *
 * <p>Plug-in to support a hypothetical RMDBS, "Test Database".
 *
 * @see <a href="https://www.schemacrawler.com">SchemaCrawler</a>
 */
public final class TestDatabaseConnector extends DatabaseConnector {

  private static DatabaseConnectorOptions databaseConnectorOptions() {
    final DatabaseServerType dbServerType = new DatabaseServerType("test-db", "Test Database");

    final DatabaseConnectionSourceBuilder connectionSourceBuilder =
        DatabaseConnectionSourceBuilder.builder("jdbc:test-db:${database}")
            .withAdditionalDriverProperties(Set.of("unpublishedJdbcDriverProperty"));

    final PluginCommand pluginCommand = PluginCommand.newDatabasePluginCommand(dbServerType);
    pluginCommand.addOption(
        "server",
        String.class,
        "--server=test-db%n" + "Loads SchemaCrawler plug-in for Test Database");

    return DatabaseConnectorOptionsBuilder.builder(dbServerType)
        .withHelpCommand(pluginCommand)
        .withUrlSupportPredicate(url -> url != null && url.startsWith("jdbc:test-db:"))
        .withInformationSchemaViewsBuilder(
            (informationSchemaViewsBuilder, connection) ->
                informationSchemaViewsBuilder.fromResourceFolder("/test-db.information_schema"))
        .withDatabaseConnectionSourceBuilder(() -> connectionSourceBuilder)
        .build();
  }

  public TestDatabaseConnector() throws Exception {
    super(databaseConnectorOptions());
    forceInstantiationFailureIfConfigured();
  }

  private void forceInstantiationFailureIfConfigured() {
    final String propertyValue =
        System.getProperty(this.getClass().getName() + ".force-instantiation-failure");
    if (propertyValue != null) {
      throw new RuntimeException("Forced instantiation error");
    }
  }
}
