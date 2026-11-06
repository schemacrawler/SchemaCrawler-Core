/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

import java.util.Map;
import org.junit.jupiter.api.Test;
import schemacrawler.tools.databaseconnector.EnvironmentalDatabaseConnectionSourceBuilder;
import us.fatehi.test.utility.TestConnection;
import us.fatehi.utility.datasource.DatabaseConnectionSource;
import us.fatehi.utility.readconfig.EnvironmentVariableConfig;

public class EnvironmentalDatabaseConnectionSourceBuilderTest {

  @Test
  public void urlWinsFieldsAndPreservesSeparateCredentials() throws Exception {
    final Map<String, String> values =
        Map.of(
            "SCHCRWLR_JDBC_URL", "jdbc:test-db:chosen:1234/catalog",
            "SCHCRWLR_SERVER", "ignored-server",
            "SCHCRWLR_HOST", "ignored-host",
            "SCHCRWLR_PORT", "9999",
            "SCHCRWLR_DATABASE", "ignored-database",
            "SCHCRWLR_DATABASE_USER", "  test-user  ",
            "SCHCRWLR_DATABASE_PASSWORD", "  test-password  ");
    try (final DatabaseConnectionSource source =
        EnvironmentalDatabaseConnectionSourceBuilder.builder(
                (EnvironmentVariableConfig) () -> values)
            .build()) {
      final TestConnection connection = source.get().unwrap(TestConnection.class);
      assertThat(connection.getUrl(), is("jdbc:test-db:chosen:1234/catalog"));
      assertThat(connection.getConnectionProperties(), hasEntry("user", "test-user"));
      assertThat(connection.getConnectionProperties(), hasEntry("password", "  test-password  "));
    }
  }
}
