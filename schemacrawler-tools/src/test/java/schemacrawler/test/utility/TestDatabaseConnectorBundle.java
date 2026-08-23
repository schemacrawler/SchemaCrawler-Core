/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility;

import java.util.Collection;
import java.util.List;
import schemacrawler.tools.databaseconnector.DatabaseConnector;
import schemacrawler.tools.databaseconnector.DatabaseConnectorBundle;
import schemacrawler.tools.databaseconnector.DatabaseConnectorOptionsBuilder;
import schemacrawler.tools.executable.commandline.PluginCommand;
import us.fatehi.utility.datasource.DatabaseConnectionSourceBuilder;
import us.fatehi.utility.datasource.DatabaseServerType;

public final class TestDatabaseConnectorBundle extends DatabaseConnector
    implements DatabaseConnectorBundle {

  private static DatabaseConnectorOptionsBuilder databaseConnectorOptionsBuilder(
      final DatabaseServerType dbServerType) {
    final PluginCommand pluginCommand = PluginCommand.newDatabasePluginCommand(dbServerType);
    return DatabaseConnectorOptionsBuilder.builder(dbServerType)
        .withHelpCommand(pluginCommand)
        .withDatabaseConnectionSourceBuilder(
            () ->
                DatabaseConnectionSourceBuilder.builder(
                    "jdbc:%s:${database}".formatted(dbServerType.getDatabaseSystemIdentifier())));
  }

  private static DatabaseConnectorOptionsBuilder bundleConnectorOptionsBuilder() {
    return databaseConnectorOptionsBuilder(new DatabaseServerType("bundle-db", "Bundle Database"));
  }

  private static DatabaseConnectorOptionsBuilder childConnectorOptionsBuilder() {
    return databaseConnectorOptionsBuilder(
        new DatabaseServerType("test-bundle-db", "Test Bundle Database"));
  }

  private static DatabaseConnector newBundleChildConnector() {
    return new BundleChildDatabaseConnector();
  }

  private static final class BundleChildDatabaseConnector extends DatabaseConnector {

    private BundleChildDatabaseConnector() {
      super(childConnectorOptionsBuilder().build());
    }
  }

  public TestDatabaseConnectorBundle() {
    super(bundleConnectorOptionsBuilder().build());
  }

  @Override
  public Collection<DatabaseConnector> getDatabaseConnectors() {
    return List.of(new TestDatabaseConnector(), newBundleChildConnector());
  }
}
