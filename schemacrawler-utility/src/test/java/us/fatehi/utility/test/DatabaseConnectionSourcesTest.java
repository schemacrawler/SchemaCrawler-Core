/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import us.fatehi.test.utility.DataSourceTestUtility;
import us.fatehi.utility.datasource.DatabaseConnectionSource;
import us.fatehi.utility.datasource.DatabaseConnectionSources;
import us.fatehi.utility.datasource.MultiUseUserCredentials;

public class DatabaseConnectionSourcesTest {

  @Test
  public void fromConnection() throws SQLException {

    final DataSource db = DataSourceTestUtility.newEmbeddedDatabase("/testdb.sql");
    final Connection wrappedConnection = db.getConnection();

    final DatabaseConnectionSource connectionSource =
        DatabaseConnectionSources.fromConnection(wrappedConnection);

    assertConnection(connectionSource);
  }

  @Test
  public void fromDataSource() throws SQLException {

    final DataSource db = DataSourceTestUtility.newEmbeddedDatabase("/testdb.sql");

    final DatabaseConnectionSource connectionSource = DatabaseConnectionSources.fromDataSource(db);

    assertConnection(connectionSource);
  }

  @Test
  public void newDatabaseConnectionSource() throws SQLException {

    final DataSource db = DataSourceTestUtility.newEmbeddedDatabase("/testdb.sql");
    final Connection wrappedConnection = db.getConnection();
    final DatabaseMetaData metaData = wrappedConnection.getMetaData();
    final String connectionUrl = metaData.getURL();
    final String userName = metaData.getUserName();
    final String password = "";

    final DatabaseConnectionSource connectionSource =
        DatabaseConnectionSources.newDatabaseConnectionSource(
            connectionUrl, new MultiUseUserCredentials(userName, password));

    assertConnection(connectionSource);
  }

  private void assertConnection(final DatabaseConnectionSource connectionSource)
      throws SQLException {
    assertThat(connectionSource.get(), is(not(nullValue())));
    assertThat(connectionSource.get().isClosed(), is(false));
  }
}
