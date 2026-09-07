/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.tools.offline.jdbc.OfflineConnectionUtility;
import schemacrawler.tools.utility.DatabaseConnectorUtility;
import us.fatehi.utility.datasource.DatabaseConnectionSource;
import us.fatehi.utility.datasource.DatabaseConnectionSources;
import us.fatehi.utility.datasource.DatabaseServerType;

public class OfflineConnectionTest {

  private Connection connection;

  @BeforeEach
  public void createTempFile() throws IOException {
    final Path offlineDatabasePath =
        Files.createTempFile(OfflineConnectionTest.class.getCanonicalName() + ".", ".ser");
    Files.write(offlineDatabasePath, "some offline database metadata ...".getBytes());

    connection = OfflineConnectionUtility.newOfflineConnection(offlineDatabasePath);
  }

  @Test
  public void testMetaData() throws SQLException {
    // This is an important test to make sure that we do not over-engineer the offline connection
    assertThrows(SQLFeatureNotSupportedException.class, () -> connection.getMetaData());
  }

  @Test
  public void testSchemaRetrievalOptions() {
    final DatabaseConnectionSource connectionSource =
        DatabaseConnectionSources.fromConnection(connection);
    final SchemaRetrievalOptions schemaRetrievalOptions =
        DatabaseConnectorUtility.matchSchemaRetrievalOptions(connectionSource);

    // Database server type is unknown, because the offline database connector is
    // not on the classpath
    assertThat(schemaRetrievalOptions.getDatabaseServerType(), is(DatabaseServerType.UNKNOWN));
  }
}
