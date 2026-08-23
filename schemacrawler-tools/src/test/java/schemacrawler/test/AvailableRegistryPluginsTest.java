/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static us.fatehi.test.utility.DataSourceTestUtility.JDBC_DRIVER_COUNT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import schemacrawler.tools.command.CommandRegistry;
import schemacrawler.tools.databaseconnector.DatabaseConnectorRegistry;
import schemacrawler.tools.registry.PluginRegistry;
import us.fatehi.utility.database.JdbcDriverRegistry;
import us.fatehi.utility.property.PropertyName;

public class AvailableRegistryPluginsTest {

  @Test
  public void availableCommands() {
    assertThat(
        getRegisteredPlugins(CommandRegistry.getRegistry()),
        arrayContainingInAnyOrder("test-command"));
  }

  @Test
  public void availableJDBCDrivers() {
    final Collection<PropertyName> availableDrivers =
        JdbcDriverRegistry.getRegistry().availableJDBCDrivers();
    assertThat(availableDrivers.size(), is(JDBC_DRIVER_COUNT));
  }

  @Test
  public void availableServers() {
    assertThat(
        getRegisteredPlugins(DatabaseConnectorRegistry.getRegistry()),
        arrayContainingInAnyOrder("test-db", "test-bundle-db"));
  }

  private String[] getRegisteredPlugins(final PluginRegistry registry) {
    final List<String> commands = new ArrayList<>();
    final Collection<PropertyName> registeredPlugins = registry.getRegisteredPlugins();
    for (final PropertyName registeredPlugin : registeredPlugins) {
      commands.add(registeredPlugin.getName());
    }
    return commands.toArray(new String[0]);
  }
}
