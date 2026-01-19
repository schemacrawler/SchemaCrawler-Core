/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.databaseconnector;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.test.utility.DisableLogging;
import schemacrawler.test.utility.TestDatabaseConnector;
import schemacrawler.tools.executable.commandline.PluginCommand;
import us.fatehi.test.utility.TestConnection;
import us.fatehi.utility.datasource.DatabaseConnectionSource;
import us.fatehi.utility.datasource.MultiUseUserCredentials;

@DisableLogging
public class DatabaseConnectorTest {

  /**
   * NOTE: This test does not test production code, but rather test utility code. However, it covers
   * basic logic in the database connector class.
   */
  @Test
  @DisplayName("Test for DatabaseConnector operations")
  public void databaseConnector() throws Exception {

    final DatabaseConnector databaseConnector = new TestDatabaseConnector();
    assertThat(
        databaseConnector.getDatabaseServerType().getDatabaseSystemIdentifier(), is("test-db"));
    assertThat(databaseConnector.getHelpCommand().getName(), is("server:test-db"));

    final DatabaseConnectionOptions connectionOptions =
        new DatabaseServerHostConnectionOptions(
            "test-db", "some-host", 2121, "some-database", null);
    final DatabaseConnectionSource connectionSource =
        databaseConnector.newDatabaseConnectionSource(
            connectionOptions, new MultiUseUserCredentials());

    assertThat(
        databaseConnector.getSchemaRetrievalOptionsBuilder(connectionSource.get()),
        is(not(nullValue())));

    final SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions();
    assertThat(
        databaseConnector.withSchemaCrawlerOptionsDefaults(schemaCrawlerOptions),
        is(not(nullValue())));

    assertThat(databaseConnector.supportsUrl("jdbc:test-db:somevalue"), is(true));
    assertThat(databaseConnector.supportsUrl("jdbc:newdb:somevalue"), is(false));
    assertThat(databaseConnector.supportsUrl(null), is(false));

    assertThat(databaseConnector.toString(), is("Database connector for test-db - Test Database"));
  }

  @Test
  @DisplayName("Unknown DatabaseConnector")
  public void unknownDatabaseConnector() {
    final DatabaseConnector databaseConnector = UnknownDatabaseConnector.UNKNOWN;

    final PluginCommand helpCommand = databaseConnector.getHelpCommand();
    assertThat(helpCommand, is(notNullValue()));
    assertThat(helpCommand.getName(), is(""));

    assertThat(
        databaseConnector.getDatabaseServerType().getDatabaseSystemIdentifier(), is(nullValue()));

    assertThat(databaseConnector.supportsUrl("jdbc:newdb:somevalue"), is(false));
    assertThat(databaseConnector.supportsUrl(null), is(false));

    assertThat(
        databaseConnector.toString(), is("Database connector for unknown database system type"));
  }

  @Test
  @DisplayName("DatabaseConnector with additional properties")
  public void urlxAdditional() throws Exception {
    // See schemacrawler.test.utility.TestDatabaseConnector
    urlxTest(
        Map.of("unpublishedJdbcDriverProperty", "value", "publishedJdbcDriverProperty", "value"),
        false);
  }

  @Test
  @DisplayName("DatabaseConnector with known properties")
  public void urlxKnown() throws Exception {
    // See us.fatehi.test.utility.TestDatabaseDriver
    urlxTest(Map.of("publishedJdbcDriverProperty", "value"), false);
  }

  @Test
  @DisplayName("DatabaseConnector with no additional properties")
  public void urlxNull() throws Exception {
    urlxTest(null, true);
  }

  @Test
  @DisplayName("DatabaseConnector with unknown properties")
  public void urlxUnknown() throws Exception {
    urlxTest(Map.of("unknownJdbcDriverProperty", "value"), true);
  }

  private void urlxTest(final Map<String, String> connectionProperties, final boolean returnsEmpty)
      throws Exception {

    final DatabaseConnector databaseConnector = new TestDatabaseConnector();

    final DatabaseConnectionOptions connectionOptions =
        new DatabaseServerHostConnectionOptions(
            "test-db", "some-host", 2121, "some-database", connectionProperties);
    final DatabaseConnectionSource connectionSource =
        databaseConnector.newDatabaseConnectionSource(
            connectionOptions, new MultiUseUserCredentials());

    final TestConnection connection = connectionSource.get().unwrap(TestConnection.class);
    if (returnsEmpty) {
      assertThat(connection.getConnectionProperties().size(), is(0));
    } else {
      final Properties expectedProperties = new Properties();
      if (connectionProperties != null) {
        expectedProperties.putAll(connectionProperties);
      }
      assertThat(connection.getConnectionProperties(), is(expectedProperties));
    }
  }
}
