/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.ServerIdentity;
import us.fatehi.utility.CloudProvider;

public class SafeServerIdentityExtractorTest {

  @Test
  @DisplayName("Extracts AWS provider and region from JDBC URL")
  public void extractsAwsRegion() throws Exception {
    final Connection connection =
        mockConnection("jdbc:mysql://mydb.us-east-1.rds.amazonaws.com:3306/appdb");

    final ServerIdentity serverIdentity = new SafeServerIdentityExtractor().extract(connection);

    assertThat(serverIdentity, is(notNullValue()));
    assertThat(serverIdentity.instanceName(), is("appdb"));
    assertThat(serverIdentity.cloudProvider(), is(CloudProvider.AWS));
    assertThat(serverIdentity.region(), is("us-east-1"));
  }

  @Test
  @DisplayName("Falls back to unknown identity for null connection")
  public void nullConnectionFallsBack() {
    final ServerIdentity serverIdentity = new SafeServerIdentityExtractor().extract(null);
    assertThat(serverIdentity, is(ServerIdentity.unknown()));
  }

  @Test
  @DisplayName("Supports connector-provided subclass behavior")
  public void connectorSubclassExtractor() throws Exception {
    final Connection connection = mockConnection("jdbc:sqlite:C:\\temp\\sample.db");

    final SafeServerIdentityExtractor extractor =
        new SafeServerIdentityExtractor() {
          @Override
          public ServerIdentity extract(final Connection connection) {
            return new ServerIdentity("sqlite-local", CloudProvider.LOCAL, "local");
          }
        };
    final ServerIdentity serverIdentity = extractor.extract(connection);

    assertThat(serverIdentity.instanceName(), is("sqlite-local"));
    assertThat(serverIdentity.cloudProvider(), is(CloudProvider.LOCAL));
    assertThat(serverIdentity.region(), is("local"));
  }

  @Test
  @DisplayName("Identifies localhost hosts as local server identity")
  public void localhostIdentity() throws Exception {
    final Connection connection = mockConnection("jdbc:postgresql://127.0.0.1:5432/postgres");

    final ServerIdentity serverIdentity = new SafeServerIdentityExtractor().extract(connection);

    assertThat(serverIdentity.instanceName(), is("postgres"));
    assertThat(serverIdentity.cloudProvider(), is(CloudProvider.LOCAL));
    assertThat(serverIdentity.region(), is("local"));
  }

  @Test
  @DisplayName("Uses localhost as instance when host is local and database is absent")
  public void localhostAsInstanceFallback() throws Exception {
    final Connection connection = mockConnection("jdbc:mysql://localhost:3306");

    final ServerIdentity serverIdentity = new SafeServerIdentityExtractor().extract(connection);

    assertThat(serverIdentity.instanceName(), is("localhost"));
    assertThat(serverIdentity.cloudProvider(), is(CloudProvider.LOCAL));
    assertThat(serverIdentity.region(), is("local"));
  }

  private Connection mockConnection(final String jdbcUrl) throws Exception {
    final Connection connection = mock(Connection.class);
    final DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
    when(connection.getMetaData()).thenReturn(databaseMetaData);
    when(databaseMetaData.getURL()).thenReturn(jdbcUrl);
    return connection;
  }
}
