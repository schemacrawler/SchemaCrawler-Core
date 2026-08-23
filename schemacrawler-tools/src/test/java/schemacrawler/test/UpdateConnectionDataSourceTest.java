/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.tools.utility.DatabaseConnectorUtility;
import us.fatehi.test.utility.TestObjectUtility;
import us.fatehi.test.utility.extensions.WithSystemProperty;
import us.fatehi.utility.datasource.DatabaseConnectionSource;
import us.fatehi.utility.datasource.DatabaseConnectionSources;
import us.fatehi.utility.datasource.DatabaseServerType;

public class UpdateConnectionDataSourceTest {

  @DisplayName("MySQL + MariaDB: MariaDB JDBC URL = treated as MySQL connector")
  @Test
  @WithSystemProperty(key = "SC_WITHOUT_DATABASE_PLUGIN", value = "mariadb")
  public void mariadb_jdbc_url_resolved_as_mysql() throws Exception {
    final DatabaseConnectionSource connectionSource = mockMariaDBConnectionSource();

    final SchemaRetrievalOptions schemaRetrievalOptions =
        DatabaseConnectorUtility.matchSchemaRetrievalOptions(connectionSource);
    final DatabaseServerType databaseServerType = schemaRetrievalOptions.getDatabaseServerType();

    assertThat(databaseServerType.isUnknownDatabaseSystem(), is(true));
  }

  @DisplayName("updateConnectionDataSource: with null connectionSource = handle gracefully")
  @Test
  public void updateConnectionDataSource_nullConnectionSource() throws Exception {
    final SchemaRetrievalOptions schemaRetrievalOptions =
        DatabaseConnectorUtility.matchSchemaRetrievalOptions(mockUncontrolledConnectionSource());

    DatabaseConnectorUtility.updateConnectionDataSource(null, schemaRetrievalOptions);
  }

  @DisplayName("updateConnectionDataSource: with null schemaRetrievalOptions = handle gracefully")
  @Test
  public void updateConnectionDataSource_nullSchemaRetrievalOptions() throws Exception {
    final DatabaseConnectionSource connectionSource = mockUncontrolledConnectionSource();

    DatabaseConnectorUtility.updateConnectionDataSource(connectionSource, null);
  }

  @DisplayName("updateConnectionDataSource: with valid inputs = set connection initializer")
  @Test
  public void updateConnectionDataSource_validInputs() throws Exception {
    final DatabaseConnectionSource connectionSource = mockUncontrolledConnectionSource();
    final SchemaRetrievalOptions schemaRetrievalOptions =
        DatabaseConnectorUtility.matchSchemaRetrievalOptions(connectionSource);

    DatabaseConnectorUtility.updateConnectionDataSource(connectionSource, schemaRetrievalOptions);

    assertThat(connectionSource, notNullValue());
  }

  private DatabaseConnectionSource mockConnectionSourceForUrl(
      final String connectionUrl, final String toString) throws SQLException {
    final DatabaseMetaData databaseMetaData = TestObjectUtility.mockDatabaseMetaData();
    when(databaseMetaData.getURL()).thenReturn(connectionUrl);
    when(databaseMetaData.toString()).thenReturn(toString);
    final Connection connection = TestObjectUtility.mockConnection();
    when(connection.getMetaData()).thenReturn(databaseMetaData);
    when(connection.toString()).thenReturn(toString);
    final DatabaseConnectionSource connectionSource =
        DatabaseConnectionSources.fromConnection(connection);
    return connectionSource;
  }

  private DatabaseConnectionSource mockMariaDBConnectionSource() throws SQLException {
    return mockConnectionSourceForUrl(
        "jdbc:mariadb://localhost:3306/test", "Mock MariaDB connection");
  }

  private DatabaseConnectionSource mockUncontrolledConnectionSource() throws SQLException {
    return mockConnectionSourceForUrl("jdbc:newdb:foo", "Mock NewDB connection");
  }
}
