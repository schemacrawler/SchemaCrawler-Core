/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import us.fatehi.test.utility.TestDatabaseDriver;
import us.fatehi.utility.database.JdbcDriverMetadata;
import us.fatehi.utility.database.JdbcDriverRegistry;
import us.fatehi.utility.property.PropertyName;

public class JdbcDriverRegistryTest {

  @Test
  public void createConnection() throws Exception {
    final JdbcDriverRegistry jdbcDriverRegistry = JdbcDriverRegistry.getRegistry();

    final Connection connection =
        jdbcDriverRegistry.createConnection("jdbc:test-db:test", new Properties());
    assertThat(connection, is(notNullValue()));
    assertThat(connection.isValid(1), is(true));
  }

  @Test
  public void discoverAvailableDrivers() throws Exception {
    final Collection<PropertyName> jdbcDrivers =
        JdbcDriverRegistry.getRegistry().getRegisteredPlugins();
    final Collection<String> jdbcDriverClassNames =
        jdbcDrivers.stream().map(PropertyName::getName).collect(Collectors.toList());
    assertThat(jdbcDriverClassNames, hasItem(TestDatabaseDriver.class.getName()));
  }

  @Test
  public void discoverAvailableDriversCache() throws Exception {
    final JdbcDriverRegistry firstRegistry = JdbcDriverRegistry.getRegistry();
    final JdbcDriverRegistry secondRegistry = JdbcDriverRegistry.getRegistry();
    assertThat(secondRegistry, is(firstRegistry));

    final Collection<PropertyName> firstRead = firstRegistry.getRegisteredPlugins();
    final Collection<PropertyName> secondRead = secondRegistry.getRegisteredPlugins();
    assertThat(secondRead, is(firstRead));
  }

  @Test
  public void inspectMetadata() throws Exception {
    final JdbcDriverRegistry jdbcDriverRegistry = JdbcDriverRegistry.getRegistry();
    final JdbcDriverMetadata metadata = jdbcDriverRegistry.inspectMetadata("jdbc:test-db:test");
    assertThat(metadata.jdbcDriver().driverClassName(), is(TestDatabaseDriver.class.getName()));
    final List<String> propertyNames =
        metadata.properties().stream()
            .map(us.fatehi.utility.database.JdbcDriverProperty::name)
            .collect(Collectors.toList());
    assertThat(propertyNames, hasItem("publishedJdbcDriverProperty"));
    assertThat(metadata.properties(), is(notNullValue()));
  }
}
