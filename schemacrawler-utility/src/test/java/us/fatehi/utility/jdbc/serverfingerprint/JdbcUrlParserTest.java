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
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static us.fatehi.test.utility.TestUtility.NOT_BLANK;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

public class JdbcUrlParserTest {

  @Test
  public void parseAuthorityWithDatabasePropertyFallback() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:sqlserver://sqlhost:1433;database=Sales");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is("sqlserver"));
    assertThat(jdbcUrl.hostHash(), matchesPattern(NOT_BLANK));
    assertThat(jdbcUrl.hostClassification(), is(HostClassification.PUBLIC));
    assertThat(jdbcUrl.databaseName(), is("sales"));
  }

  @Test
  public void parseAuthorityWithDbPropertyFallback() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:sqlserver://sqlhost:1433;db=Sales");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is("sqlserver"));
    assertThat(jdbcUrl.hostHash(), matchesPattern(NOT_BLANK));
    assertThat(jdbcUrl.hostClassification(), is(HostClassification.PUBLIC));
    assertThat(jdbcUrl.databaseName(), is("sales"));
  }

  @Test
  public void parseAuthorityWithQueryAndFragment() {
    final JdbcUrl jdbcUrl =
        JdbcUrlParser.parse("jdbc:postgresql://pgserver:5432/appdb?ssl=true#connection");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is("postgresql"));
    assertThat(jdbcUrl.hostHash(), matchesPattern(NOT_BLANK));
    assertThat(jdbcUrl.hostClassification(), is(HostClassification.PUBLIC));
    assertThat(jdbcUrl.databaseName(), is("appdb"));
  }

  @Test
  public void parseBlankUrl() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse(null);
    assertThat(jdbcUrl.databaseSystemIdentifier(), is(""));
    assertThat(jdbcUrl.hostHash(), is(""));
    assertThat(jdbcUrl.databaseName(), is(""));
  }

  @Test
  public void parseBracketedIpv6HostPort() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:postgresql://[2001:db8::10]:5432/mydb");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is("postgresql"));
    assertThat(jdbcUrl.hostHash(), matchesPattern(NOT_BLANK));
    assertThat(jdbcUrl.hostClassification(), is(HostClassification.PUBLIC));
    assertThat(jdbcUrl.databaseName(), is("mydb"));
  }

  @Test
  public void parseHostListUsesFirstHost() {
    final JdbcUrl jdbcUrl =
        JdbcUrlParser.parse("jdbc:postgresql://host1:5432,host2:5433,host3:5434/mydb");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is("postgresql"));
    assertThat(jdbcUrl.hostHash(), matchesPattern(NOT_BLANK));
    assertThat(jdbcUrl.hostClassification(), is(HostClassification.PUBLIC));
    assertThat(jdbcUrl.databaseName(), is("mydb"));
  }

  @Test
  public void parseHostPortDatabaseUrl() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:mysql://db.example.com:3306/appdb");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is("mysql"));
    assertThat(jdbcUrl.hostHash(), matchesPattern(NOT_BLANK));
    assertThat(jdbcUrl.hostClassification(), is(HostClassification.PUBLIC));
    assertThat(jdbcUrl.databaseName(), is("appdb"));
  }

  @Test
  public void parseInvalidPortAsNull() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:mysql://dbhost:notaport/appdb");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is("mysql"));
    assertThat(jdbcUrl.hostHash(), matchesPattern(NOT_BLANK));
    assertThat(jdbcUrl.hostClassification(), is(HostClassification.PUBLIC));
    assertThat(jdbcUrl.databaseName(), is("appdb"));
  }

  @Test
  public void parseJdbcWithoutDriverBody() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:mysql");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is("mysql"));
    assertThat(jdbcUrl.hostHash(), is(""));
    assertThat(jdbcUrl.databaseName(), is(""));
  }

  @Test
  public void parseNestedAuthorityUrl() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:hsqldb:hsql://localhost:9001/schemacrawler");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is("hsqldb"));
    assertThat(jdbcUrl.hostHash(), is("<localhost>"));
    assertThat(jdbcUrl.hostClassification(), is(HostClassification.LOCALHOST));
    assertThat(jdbcUrl.databaseName(), is("schemacrawler"));
  }

  @Test
  public void parseNonAuthoritySemicolonFormFallbackToHeadToken() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:sqlserver:sqlhost:1433;encrypt=true");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is("sqlserver"));
    assertThat(jdbcUrl.hostHash(), matchesPattern(NOT_BLANK));
    assertThat(jdbcUrl.hostClassification(), is(HostClassification.PUBLIC));
    assertThat(jdbcUrl.databaseName(), is("sqlhost:1433"));
  }

  @Test
  public void parseNonAuthoritySemicolonFormWithDatabase() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:sqlserver:sqlhost:1433;database=Sales");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is("sqlserver"));
    assertThat(jdbcUrl.hostHash(), matchesPattern(NOT_BLANK));
    assertThat(jdbcUrl.hostClassification(), is(HostClassification.PUBLIC));
    assertThat(jdbcUrl.databaseName(), is("sales"));
  }

  @Test
  public void parseNonAuthoritySemicolonFormWithDatabaseName() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:sqlserver:sqlhost:1433;databaseName=Sales");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is("sqlserver"));
    assertThat(jdbcUrl.hostHash(), matchesPattern(NOT_BLANK));
    assertThat(jdbcUrl.hostClassification(), is(HostClassification.PUBLIC));
    assertThat(jdbcUrl.databaseName(), is("sales"));
  }

  @Test
  public void parseNonJdbcUrl() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("mysql://db:3306/appdb");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is(""));
    assertThat(jdbcUrl.hostHash(), is(""));
    assertThat(jdbcUrl.databaseName(), is(""));
  }

  @Test
  public void parseOfflineFileUrl() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:offline:C:\\temp\\snapshot.db");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is("offline"));
    assertThat(jdbcUrl.hostHash(), is(""));
    assertThat(jdbcUrl.databaseName(), is("c:\\temp\\snapshot.db"));
  }

  @Test
  public void parseOracleAtHostSyntaxWithoutSlashes() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:oracle:thin:@oracledb:1521/ORCL");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is("oracle"));
    assertThat(jdbcUrl.hostHash(), is(""));
    assertThat(jdbcUrl.databaseName(), is("thin:@oracledb:1521"));
  }

  @Test
  public void parseOracleStyleUrl() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:oracle:thin:@//oracledb:1521/ORCLPDB1");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is("oracle"));
    assertThat(jdbcUrl.hostHash(), matchesPattern(NOT_BLANK));
    assertThat(jdbcUrl.hostClassification(), is(HostClassification.PUBLIC));
    assertThat(jdbcUrl.databaseName(), is("orclpdb1"));
  }

  @Test
  public void parseSqliteMemoryUrl() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:sqlite::memory:");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is("sqlite"));
    assertThat(jdbcUrl.hostHash(), is(""));
    assertThat(jdbcUrl.databaseName(), is(":memory:"));
  }

  @Test
  public void parseSqlServerStyleUrl() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:sqlserver://sqlhost:1433;databaseName=Sales");
    assertThat(jdbcUrl.databaseSystemIdentifier(), is("sqlserver"));
    assertThat(jdbcUrl.hostHash(), matchesPattern(NOT_BLANK));
    assertThat(jdbcUrl.hostClassification(), is(HostClassification.PUBLIC));
    assertThat(jdbcUrl.databaseName(), is("sales"));
  }

  @RepeatedTest(5)
  @DisplayName("Random database connection URL protocols test")
  public void parseSpyWrappedMysqlUrl() {
    final int randomLength = RandomUtils.insecure().randomInt(1, 10);
    final String spySubProtcol = RandomStringUtils.insecure().nextAlphabetic(randomLength);
    final JdbcUrl jdbcUrl =
        JdbcUrlParser.parse("jdbc:%s://dbhost:9999/appdb".formatted(spySubProtcol));
    assertThat(
        "For spy sub-procol <%s>".formatted(spySubProtcol),
        jdbcUrl.databaseSystemIdentifier(),
        is(spySubProtcol.toLowerCase()));
  }
}
