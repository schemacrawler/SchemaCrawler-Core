/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility;

import static org.junit.jupiter.api.Assertions.fail;
import static us.fatehi.test.utility.TestUtility.failTestSetup;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;
import schemacrawler.testdb.TestSchemaCreator;
import us.fatehi.test.utility.DataSourceTestUtility;
import us.fatehi.utility.database.SqlScript;
import us.fatehi.utility.datasource.DatabaseConnectionSource;
import us.fatehi.utility.datasource.DatabaseConnectionSources;

public abstract class BaseAdditionalDatabaseTest {

  private DatabaseConnectionSource connectionSource;

  protected void closeDataSource() {
    try {
      connectionSource.close();
    } catch (final Exception e) {
      failTestSetup("Could not close data source", e);
    }
  }

  protected final void createConnectionSource(final Connection connection) {
    createConnectionSource(DatabaseConnectionSources.fromConnection(connection));
  }

  protected final void createConnectionSource(final DatabaseConnectionSource connectionSource) {
    this.connectionSource = connectionSource;
  }

  protected void createDatabase(final String scriptsResource) {
    try (final Connection connection = getConnection()) {
      final TestSchemaCreator schemaCreator =
          new TestSchemaCreator(connection, scriptsResource, false);
      schemaCreator.run();
    } catch (final SQLException e) {
      failTestSetup("Could not create database", e);
    }
  }

  protected void createDataSource(
      final String connectionUrl, final String user, final String password) {
    createDataSource(connectionUrl, user, password, null);
  }

  protected void createDataSource(
      final String connectionUrl,
      final String user,
      final String password,
      final Map<String, String> connectionProperties) {

    final DataSource dataSource =
        DataSourceTestUtility.createDataSource(connectionUrl, user, password, connectionProperties);
    createConnectionSource(DatabaseConnectionSources.fromDataSource(dataSource));
  }

  protected final Connection getConnection() {
    try {
      return connectionSource.get();
    } catch (final RuntimeException e) {
      fail("Could not get database connection", e);
      return null; // Appeasing the compiler - this line will never be executed.
    }
  }

  protected final DatabaseConnectionSource getConnectionSource() {
    return connectionSource;
  }

  protected void runScript(final String databaseSqlResource) throws Exception {
    try (final Connection connection = getConnection()) {
      SqlScript.executeScriptFromResource(databaseSqlResource, connection);
    }
  }
}
