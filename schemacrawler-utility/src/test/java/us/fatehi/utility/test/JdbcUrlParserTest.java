/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import us.fatehi.utility.datasource.JdbcUrl;
import us.fatehi.utility.datasource.JdbcUrlParser;

public class JdbcUrlParserTest {

  @Test
  public void parseHostPortDatabaseUrl() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:mysql://db.example.com:3306/appdb");
    assertThat(jdbcUrl.databaseServerType(), is("mysql"));
    assertThat(jdbcUrl.hostClassifier().getSanitizedHostName(), is(""));
    assertThat(jdbcUrl.port(), is(3306));
    assertThat(jdbcUrl.databaseName(), is("appdb"));
  }

  @Test
  public void parseSqlServerStyleUrl() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:sqlserver://sqlhost:1433;databaseName=Sales");
    assertThat(jdbcUrl.databaseServerType(), is("sqlserver"));
    assertThat(jdbcUrl.hostClassifier().getSanitizedHostName(), is("sqlhost"));
    assertThat(jdbcUrl.port(), is(1433));
    assertThat(jdbcUrl.databaseName(), is("Sales"));
  }

  @Test
  public void parseOracleStyleUrl() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:oracle:thin:@//oracledb:1521/ORCLPDB1");
    assertThat(jdbcUrl.databaseServerType(), is("oracle"));
    assertThat(jdbcUrl.hostClassifier().getSanitizedHostName(), is("oracledb"));
    assertThat(jdbcUrl.port(), is(1521));
    assertThat(jdbcUrl.databaseName(), is("ORCLPDB1"));
  }

  @Test
  public void parseSqliteMemoryUrl() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:sqlite::memory:");
    assertThat(jdbcUrl.databaseServerType(), is("sqlite"));
    assertThat(jdbcUrl.hostClassifier().getSanitizedHostName(), is(""));
    assertThat(jdbcUrl.port(), is((Integer) null));
    assertThat(jdbcUrl.databaseName(), is(":memory:"));
  }

  @Test
  public void parseOfflineFileUrl() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:offline:C:\\temp\\snapshot.db");
    assertThat(jdbcUrl.databaseServerType(), is("offline"));
    assertThat(jdbcUrl.hostClassifier().getSanitizedHostName(), is(""));
    assertThat(jdbcUrl.port(), is((Integer) null));
    assertThat(jdbcUrl.databaseName(), is("C:\\temp\\snapshot.db"));
  }

  @Test
  public void parseBlankUrl() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse(null);
    assertThat(jdbcUrl.databaseServerType(), is(""));
    assertThat(jdbcUrl.hostClassifier().getSanitizedHostName(), is(""));
    assertThat(jdbcUrl.port(), is((Integer) null));
    assertThat(jdbcUrl.databaseName(), is((String) null));
  }
}
