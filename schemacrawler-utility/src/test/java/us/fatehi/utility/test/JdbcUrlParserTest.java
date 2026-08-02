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
    assertThat(jdbcUrl.hostClassifier().getSanitizedHostName(), is("public_host"));
    assertThat(jdbcUrl.port(), is(1433));
    assertThat(jdbcUrl.databaseName(), is("Sales"));
  }

  @Test
  public void parseOracleStyleUrl() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:oracle:thin:@//oracledb:1521/ORCLPDB1");
    assertThat(jdbcUrl.databaseServerType(), is("oracle"));
    assertThat(jdbcUrl.hostClassifier().getSanitizedHostName(), is("public_host"));
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

  @Test
  public void parseNonJdbcUrl() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("mysql://db:3306/appdb");
    assertThat(jdbcUrl.databaseServerType(), is(""));
    assertThat(jdbcUrl.hostClassifier().getSanitizedHostName(), is(""));
    assertThat(jdbcUrl.port(), is((Integer) null));
    assertThat(jdbcUrl.databaseName(), is((String) null));
  }

  @Test
  public void parseJdbcWithoutDriverBody() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:mysql");
    assertThat(jdbcUrl.databaseServerType(), is("mysql"));
    assertThat(jdbcUrl.hostClassifier().getSanitizedHostName(), is(""));
    assertThat(jdbcUrl.port(), is((Integer) null));
    assertThat(jdbcUrl.databaseName(), is((String) null));
  }

  @Test
  public void parseAuthorityWithQueryAndFragment() {
    final JdbcUrl jdbcUrl =
        JdbcUrlParser.parse("jdbc:postgresql://pgserver:5432/appdb?ssl=true#connection");
    assertThat(jdbcUrl.databaseServerType(), is("postgresql"));
    assertThat(jdbcUrl.hostClassifier().getSanitizedHostName(), is("public_host"));
    assertThat(jdbcUrl.port(), is(5432));
    assertThat(jdbcUrl.databaseName(), is("appdb"));
  }

  @Test
  public void parseAuthorityWithDatabasePropertyFallback() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:sqlserver://sqlhost:1433;database=Sales");
    assertThat(jdbcUrl.databaseServerType(), is("sqlserver"));
    assertThat(jdbcUrl.hostClassifier().getSanitizedHostName(), is("public_host"));
    assertThat(jdbcUrl.port(), is(1433));
    assertThat(jdbcUrl.databaseName(), is("Sales"));
  }

  @Test
  public void parseAuthorityWithDbPropertyFallback() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:sqlserver://sqlhost:1433;db=Sales");
    assertThat(jdbcUrl.databaseServerType(), is("sqlserver"));
    assertThat(jdbcUrl.hostClassifier().getSanitizedHostName(), is("public_host"));
    assertThat(jdbcUrl.port(), is(1433));
    assertThat(jdbcUrl.databaseName(), is("Sales"));
  }

  @Test
  public void parseNonAuthoritySemicolonFormWithDatabaseName() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:sqlserver:sqlhost:1433;databaseName=Sales");
    assertThat(jdbcUrl.databaseServerType(), is("sqlserver"));
    assertThat(jdbcUrl.hostClassifier().getSanitizedHostName(), is("public_host"));
    assertThat(jdbcUrl.port(), is(1433));
    assertThat(jdbcUrl.databaseName(), is("Sales"));
  }

  @Test
  public void parseNonAuthoritySemicolonFormWithDatabase() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:sqlserver:sqlhost:1433;database=Sales");
    assertThat(jdbcUrl.databaseServerType(), is("sqlserver"));
    assertThat(jdbcUrl.hostClassifier().getSanitizedHostName(), is("public_host"));
    assertThat(jdbcUrl.port(), is(1433));
    assertThat(jdbcUrl.databaseName(), is("Sales"));
  }

  @Test
  public void parseNonAuthoritySemicolonFormFallbackToHeadToken() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:sqlserver:sqlhost:1433;encrypt=true");
    assertThat(jdbcUrl.databaseServerType(), is("sqlserver"));
    assertThat(jdbcUrl.hostClassifier().getSanitizedHostName(), is("public_host"));
    assertThat(jdbcUrl.port(), is(1433));
    assertThat(jdbcUrl.databaseName(), is("sqlhost:1433"));
  }

  @Test
  public void parseBracketedIpv6HostPort() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:postgresql://[2001:db8::10]:5432/mydb");
    assertThat(jdbcUrl.databaseServerType(), is("postgresql"));
    assertThat(jdbcUrl.hostClassifier().isIpV6(), is(true));
    assertThat(jdbcUrl.hostClassifier().getSanitizedHostName(), is(""));
    assertThat(jdbcUrl.port(), is(5432));
    assertThat(jdbcUrl.databaseName(), is("mydb"));
  }

  @Test
  public void parseHostListUsesFirstHost() {
    final JdbcUrl jdbcUrl =
        JdbcUrlParser.parse("jdbc:postgresql://host1:5432,host2:5433,host3:5434/mydb");
    assertThat(jdbcUrl.databaseServerType(), is("postgresql"));
    assertThat(jdbcUrl.hostClassifier().getSanitizedHostName(), is("public_host"));
    assertThat(jdbcUrl.port(), is(5432));
    assertThat(jdbcUrl.databaseName(), is("mydb"));
  }

  @Test
  public void parseInvalidPortAsNull() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:mysql://dbhost:notaport/appdb");
    assertThat(jdbcUrl.databaseServerType(), is("mysql"));
    assertThat(jdbcUrl.hostClassifier().getSanitizedHostName(), is("public_host"));
    assertThat(jdbcUrl.port(), is((Integer) null));
    assertThat(jdbcUrl.databaseName(), is("appdb"));
  }

  @Test
  public void parseOracleAtHostSyntaxWithoutSlashes() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:oracle:thin:@oracledb:1521/ORCL");
    assertThat(jdbcUrl.databaseServerType(), is("oracle"));
    assertThat(jdbcUrl.hostClassifier().getSanitizedHostName(), is(""));
    assertThat(jdbcUrl.port(), is((Integer) null));
    assertThat(jdbcUrl.databaseName(), is("thin:@oracledb:1521"));
  }
}
