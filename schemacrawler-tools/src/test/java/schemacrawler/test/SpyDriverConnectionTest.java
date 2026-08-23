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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.test.utility.DisableLogging;
import schemacrawler.tools.utility.DatabaseConnectorUtility;
import us.fatehi.utility.datasource.DatabaseConnectionSource;
import us.fatehi.utility.datasource.DatabaseConnectionSources;
import us.fatehi.utility.datasource.MultiUseUserCredentials;

/**
 * Tests that a spy JDBC driver (named "p6spy") wrapping a {@code jdbc:p6spy:test-db:...} URL:
 *
 * <ul>
 *   <li>Is used by {@code JdbcDriverRegistry.createConnection()} to obtain a valid connection.
 *   <li>Results in {@code DatabaseConnectorUtility.matchSchemaRetrievalOptions()} identifying the
 *       correct underlying database connector ("test-db").
 * </ul>
 *
 * <p>These tests are expected to fail until production code is updated to unwrap spy URL prefixes.
 */
@DisableLogging
@ExtendWith(MockitoExtension.class)
public class SpyDriverConnectionTest {

  private static final String SPY_URL = "jdbc:p6spy:test-db:test";

  /** Minimal spy driver implementation that delegates connection creation to the inner driver. */
  private static final class p6spyDriver implements Driver {

    private static Connection newSpyConnection(final String url, final Properties info)
        throws SQLException {
      // Delegate the actual connection to the registered inner driver via DriverManager
      final Driver innerDriver = DriverManager.getDriver("jdbc:test-db:test");
      final Connection innerConnection = innerDriver.connect("jdbc:test-db:test", info);

      final DatabaseMetaData metaData = mock(DatabaseMetaData.class);
      lenient().when(metaData.getURL()).thenReturn(url);
      lenient().when(metaData.getDatabaseProductName()).thenReturn("p6spy");
      lenient().when(metaData.getDatabaseProductVersion()).thenReturn("0.0");

      final Connection spyConnection = mock(Connection.class);
      lenient().when(spyConnection.isValid(any(Integer.class))).thenReturn(true);
      lenient().when(spyConnection.isClosed()).thenReturn(false);
      lenient().when(spyConnection.getMetaData()).thenReturn(metaData);
      lenient().when(spyConnection.unwrap(Connection.class)).thenReturn(innerConnection);
      lenient().when(spyConnection.isWrapperFor(Connection.class)).thenReturn(true);

      return spyConnection;
    }

    @Override
    public boolean acceptsURL(final String url) {
      return url != null && url.startsWith("jdbc:p6spy:");
    }

    @Override
    public Connection connect(final String url, final Properties info) throws SQLException {
      if (!acceptsURL(url)) {
        return null;
      }
      return newSpyConnection(url, info);
    }

    @Override
    public int getMajorVersion() {
      return 0;
    }

    @Override
    public int getMinorVersion() {
      return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
      throw new SQLFeatureNotSupportedException("Not supported", "HYC00");
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) {
      return new DriverPropertyInfo[0];
    }

    @Override
    public boolean jdbcCompliant() {
      return false;
    }
  }

  private p6spyDriver p6spyDriver;

  @BeforeEach
  public void registerp6spyDriver() throws SQLException {
    p6spyDriver = new p6spyDriver();
    DriverManager.registerDriver(p6spyDriver);
  }

  @AfterEach
  public void deregisterp6spyDriver() throws SQLException {
    DriverManager.deregisterDriver(p6spyDriver);
  }

  @Test
  @DisplayName("p6spy driver creates a valid connection via SimpleDatabaseConnectionSource")
  public void spyDriverCreatesValidConnection() {
    final DatabaseConnectionSource connectionSource =
        DatabaseConnectionSources.newDatabaseConnectionSource(
            SPY_URL, new MultiUseUserCredentials());

    final Connection connection = connectionSource.get();
    assertThat(connection, is(not(nullValue())));
  }

  @Test
  @DisplayName("matchSchemaRetrievalOptions identifies test-db connector for p6spy-wrapped URL")
  public void spyDriverMatchesTestDbConnector() {
    final DatabaseConnectionSource connectionSource =
        DatabaseConnectionSources.newDatabaseConnectionSource(
            SPY_URL, new MultiUseUserCredentials());

    final SchemaRetrievalOptions schemaRetrievalOptions =
        DatabaseConnectorUtility.matchSchemaRetrievalOptions(connectionSource);

    assertThat(schemaRetrievalOptions, is(not(nullValue())));
    assertThat(
        schemaRetrievalOptions.getDatabaseServerType().getDatabaseSystemIdentifier(),
        is("test-db"));
  }
}
