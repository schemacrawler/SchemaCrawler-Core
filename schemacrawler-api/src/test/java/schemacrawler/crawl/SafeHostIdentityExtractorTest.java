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
import schemacrawler.schema.HostIdentity;
import us.fatehi.utility.CloudProvider;
import us.fatehi.utility.HostType;

public class SafeHostIdentityExtractorTest {

  @Test
  @DisplayName("Extracts AWS provider and region from JDBC URL")
  public void extractsAwsRegion() throws Exception {
    final Connection connection =
        mockConnection("jdbc:mysql://mydb.us-east-1.rds.amazonaws.com:3306/appdb");

    final HostIdentity hostIdentity = new SafeHostIdentityExtractor().extract(connection);

    assertThat(hostIdentity, is(notNullValue()));
    assertThat(hostIdentity.hostType(), is(HostType.public_host));
    assertThat(hostIdentity.cloudProvider(), is(CloudProvider.AWS));
    assertThat(hostIdentity.region(), is("us-east-1"));
  }

  @Test
  @DisplayName("Falls back to unknown identity for null connection")
  public void nullConnectionFallsBack() {
    final HostIdentity hostIdentity = new SafeHostIdentityExtractor().extract(null);
    assertThat(hostIdentity, is(HostIdentity.unknown()));
  }

  @Test
  @DisplayName("Supports connector-provided subclass behavior")
  public void connectorSubclassExtractor() throws Exception {
    final Connection connection = mockConnection("jdbc:sqlite:C:\\temp\\sample.db");

    final SafeHostIdentityExtractor extractor =
        new SafeHostIdentityExtractor() {
          @Override
          public HostIdentity extract(final Connection connection) {
            return new HostIdentity(HostType.public_host, CloudProvider.UNKNOWN, "unknown");
          }
        };
    final HostIdentity hostIdentity = extractor.extract(connection);

    assertThat(hostIdentity.hostType(), is(HostType.public_host));
    assertThat(hostIdentity.cloudProvider(), is(CloudProvider.UNKNOWN));
    assertThat(hostIdentity.region(), is("unknown"));
  }

  @Test
  @DisplayName("Identifies localhost hosts as local host identity")
  public void localhostIdentity() throws Exception {
    final Connection connection = mockConnection("jdbc:postgresql://127.0.0.1:5432/postgres");

    final HostIdentity hostIdentity = new SafeHostIdentityExtractor().extract(connection);

    assertThat(hostIdentity.hostType(), is(HostType.localhost));
    assertThat(hostIdentity.cloudProvider(), is(CloudProvider.UNKNOWN));
    assertThat(hostIdentity.region(), is("unknown"));
  }

  @Test
  @DisplayName("Uses localhost host type when host is local and database is absent")
  public void localhostAsInstanceFallback() throws Exception {
    final Connection connection = mockConnection("jdbc:mysql://localhost:3306");

    final HostIdentity hostIdentity = new SafeHostIdentityExtractor().extract(connection);

    assertThat(hostIdentity.hostType(), is(HostType.localhost));
    assertThat(hostIdentity.cloudProvider(), is(CloudProvider.UNKNOWN));
    assertThat(hostIdentity.region(), is("unknown"));
  }

  private Connection mockConnection(final String jdbcUrl) throws Exception {
    final Connection connection = mock(Connection.class);
    final DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
    when(connection.getMetaData()).thenReturn(databaseMetaData);
    when(databaseMetaData.getURL()).thenReturn(jdbcUrl);
    return connection;
  }
}
