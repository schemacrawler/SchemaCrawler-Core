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
import static org.hamcrest.Matchers.sameInstance;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import us.fatehi.test.utility.TestDatabaseDriver;
import us.fatehi.utility.database.JdbcDriver;
import us.fatehi.utility.database.JdbcDriverMetadata;
import us.fatehi.utility.database.JdbcDriverRegistry;

public class JdbcDriverRegistryTest {

  @Test
  public void createConnection() throws Exception {
    JdbcDriverRegistry.discoverAvailableDrivers();

    final Connection connection =
        JdbcDriverRegistry.createConnection("jdbc:test-db:test", new Properties());
    assertThat(connection, is(notNullValue()));
    assertThat(connection.isValid(1), is(true));
  }

  @Test
  public void discoverAvailableDrivers() throws Exception {
    final Collection<JdbcDriver> jdbcDrivers = JdbcDriverRegistry.discoverAvailableDrivers();
    final Collection<String> jdbcDriverClassNames =
        jdbcDrivers.stream().map(JdbcDriver::driverClassName).collect(Collectors.toList());
    assertThat(jdbcDriverClassNames, hasItem(TestDatabaseDriver.class.getName()));
  }

  @Test
  public void discoverAvailableDriversCache() throws Exception {
    final Collection<JdbcDriver> firstRead = JdbcDriverRegistry.discoverAvailableDrivers();
    final Collection<JdbcDriver> secondRead = JdbcDriverRegistry.discoverAvailableDrivers();
    assertThat(secondRead, is(sameInstance(firstRead)));
  }

  @Test
  public void inspectMetadata() throws Exception {
    JdbcDriverRegistry.discoverAvailableDrivers();
    final JdbcDriverMetadata metadata = JdbcDriverRegistry.inspectMetadata("jdbc:test-db:test");
    assertThat(metadata.driver().driverClassName(), is(TestDatabaseDriver.class.getName()));
    final List<String> propertyNames =
        metadata.properties().stream()
            .map(us.fatehi.utility.database.JdbcDriverProperty::name)
            .collect(Collectors.toList());
    assertThat(propertyNames, hasItem("publishedJdbcDriverProperty"));
  }

  @Test
  public void resolveDriverForUrl() throws Exception {
    JdbcDriverRegistry.discoverAvailableDrivers();
    final JdbcDriver jdbcDriver = JdbcDriverRegistry.resolveDriverForUrl("jdbc:test-db:test");
    assertThat(jdbcDriver.driverClassName(), is(TestDatabaseDriver.class.getName()));
  }
}
