/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.datasource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import us.fatehi.utility.SQLRuntimeException;
import us.fatehi.utility.jdbc.driver.JdbcDriverRegistry;
import us.fatehi.utility.jdbc.serverfingerprint.DatabaseServerFingerprintBuilder;

public class ConnectionDiagnosticsTest {

  @Test
  public void unavailableDescriptionDoesNotPreventConnection() throws Exception {
    final String url = "jdbc:unknown:URL_SECRET";
    final Properties properties = new Properties();
    final Connection connection = mock(Connection.class);
    final JdbcDriverRegistry registry = mock(JdbcDriverRegistry.class);
    when(registry.createConnection(url, properties)).thenReturn(connection);
    try (final MockedStatic<JdbcDriverRegistry> registryAccess =
            mockStatic(JdbcDriverRegistry.class);
        final MockedStatic<DatabaseServerFingerprintBuilder> fingerprints =
            mockStatic(DatabaseServerFingerprintBuilder.class)) {
      registryAccess.when(JdbcDriverRegistry::getRegistry).thenReturn(registry);
      fingerprints
          .when(() -> DatabaseServerFingerprintBuilder.builder(url))
          .thenThrow(new IllegalArgumentException("DIAGNOSTIC_SECRET"));
      assertThat(
          AbstractDatabaseConnectionSource.getConnection(url, properties) == connection, is(true));
      verify(registry).createConnection(url, properties);
      final SQLException cause = new SQLException("DRIVER_SECRET");
      when(registry.createConnection(url, properties)).thenThrow(cause);
      final SQLRuntimeException failure =
          assertThrows(
              SQLRuntimeException.class,
              () -> AbstractDatabaseConnectionSource.getConnection(url, properties));
      assertThat(failure.getMessage().contains("description unavailable"), is(true));
      assertThat(failure.getMessage().contains("DIAGNOSTIC_SECRET"), is(false));
      assertThat(failure.getMessage().contains("URL_SECRET"), is(false));
      assertThat(failure.getCause() == cause, is(true));
    }
  }

  @Test
  public void connectionFailureUsesFingerprintAndPreservesDriverInputs() throws Exception {
    final String url = "jdbc:postgresql://private-host/books?password=URL_SECRET";
    final String internalUrl = url.replace("private-host", "db.internal");
    final Properties properties = new Properties();
    properties.setProperty("user", "USER_SECRET");
    properties.setProperty("password", " PASSWORD_SECRET ");
    final SQLException cause = new SQLException("DRIVER_SECRET");
    final JdbcDriverRegistry registry = mock(JdbcDriverRegistry.class);
    when(registry.createConnection(internalUrl, properties)).thenThrow(cause);
    final List<String> messages = new ArrayList<>();
    final Handler handler =
        new Handler() {
          @Override
          public void publish(final LogRecord record) {
            messages.add(record.getMessage());
          }

          @Override
          public void flush() {}

          @Override
          public void close() {}
        };
    final Logger logger = Logger.getLogger(AbstractDatabaseConnectionSource.class.getName());
    logger.addHandler(handler);
    try (final MockedStatic<JdbcDriverRegistry> registryAccess =
        mockStatic(JdbcDriverRegistry.class)) {
      registryAccess.when(JdbcDriverRegistry::getRegistry).thenReturn(registry);
      final SQLRuntimeException failure =
          assertThrows(
              SQLRuntimeException.class,
              () -> AbstractDatabaseConnectionSource.getConnection(internalUrl, properties));
      assertThat(failure.getCause() == cause, is(true));
      assertThat(failure.getMessage().contains("schemacrawler -T"), is(true));
      assertThat(properties.getProperty("password"), is(" PASSWORD_SECRET "));
      verify(registry).createConnection(internalUrl, properties);
      messages.add(failure.getMessage());
      final String hash =
          DatabaseServerFingerprintBuilder.builder(internalUrl).build().fingerprint();
      for (final String message : messages) {
        assertThat(message.contains(hash), is(true));
        assertThat(message.contains("type=postgresql"), is(true));
        assertThat(message.contains("host classification=INTERNAL"), is(true));
        for (final String secret :
            new String[] {
              internalUrl, "db.internal", "URL_SECRET", "USER_SECRET", "PASSWORD_SECRET"
            }) {
          assertThat(message.contains(secret), is(false));
        }
      }
    } finally {
      logger.removeHandler(handler);
    }
  }
}
