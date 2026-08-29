/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.jdbc.serverfingerprint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@link JdbcUrlParser} correctly identifies the underlying database system when a spy
 * JDBC driver wraps the original URL (P6Spy, log4jdbc).
 *
 * <p>These tests are expected to fail until production code is updated to unwrap spy URL prefixes.
 */
public class JdbcUrlParserSpyTest {

  @Test
  @DisplayName("log4jdbc wrapping PostgreSQL URL - databaseSystemIdentifier should be postgresql")
  public void parseLog4jdbcWrappedPostgresqlUrl() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:log4jdbc:postgresql://pghost:5432/mydb");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is("postgresql"));
  }

  @Test
  @DisplayName("P6Spy wrapping MySQL URL - databaseSystemIdentifier should be mysql")
  public void parseP6SpyWrappedMysqlUrl() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:p6spy:mysql://dbhost:3306/appdb");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is("mysql"));
  }

  @Test
  @DisplayName("HSQLDB supporting MySQL URL - databaseSystemIdentifier should be mysql")
  public void parseHsqldbSupportHsqlUrl() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:hsqldb:mysql://localhost:9001/schemacrawler");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is("hsqldb"));
  }
}
