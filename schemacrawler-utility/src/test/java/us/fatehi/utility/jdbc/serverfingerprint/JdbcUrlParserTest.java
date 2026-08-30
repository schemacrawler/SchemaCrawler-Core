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
import static org.junit.jupiter.api.Assertions.assertAll;
import static us.fatehi.test.utility.TestUtility.NOT_BLANK;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

public class JdbcUrlParserTest {

  @Test
  public void parseAuthorityWithDatabasePropertyFallback() {
    final JdbcUrlTokens jdbcUrl =
        JdbcUrlTokenizer.tokenize("jdbc:sqlserver://sqlhost:1433;database=Sales");
    assertAll(
        () ->
            assertThat(
                "database system identifier", jdbcUrl.databaseSystemIdentifier(), is("sqlserver")),
        () -> assertThat("host", jdbcUrl.host(), matchesPattern(NOT_BLANK)),
        () ->
            assertThat(
                "host classification", jdbcUrl.hostClassification(), is(HostClassification.PUBLIC)),
        () -> assertThat("database name", jdbcUrl.databaseName(), is("sales")));
  }

  @Test
  public void parseAuthorityWithDbPropertyFallback() {
    final JdbcUrlTokens jdbcUrl =
        JdbcUrlTokenizer.tokenize("jdbc:sqlserver://sqlhost:1433;db=Sales");
    assertAll(
        () ->
            assertThat(
                "database system identifier", jdbcUrl.databaseSystemIdentifier(), is("sqlserver")),
        () -> assertThat("host", jdbcUrl.host(), matchesPattern(NOT_BLANK)),
        () ->
            assertThat(
                "host classification", jdbcUrl.hostClassification(), is(HostClassification.PUBLIC)),
        () -> assertThat("database name", jdbcUrl.databaseName(), is("sales")));
  }

  @Test
  public void parseAuthorityWithQueryAndFragment() {
    final JdbcUrlTokens jdbcUrl =
        JdbcUrlTokenizer.tokenize("jdbc:postgresql://pgserver:5432/appdb?ssl=true#connection");
    assertAll(
        () ->
            assertThat(
                "database system identifier", jdbcUrl.databaseSystemIdentifier(), is("postgresql")),
        () -> assertThat("host", jdbcUrl.host(), matchesPattern(NOT_BLANK)),
        () ->
            assertThat(
                "host classification", jdbcUrl.hostClassification(), is(HostClassification.PUBLIC)),
        () -> assertThat("database name", jdbcUrl.databaseName(), is("appdb")));
  }

  @Test
  public void parseBlankUrl() {
    final JdbcUrlTokens jdbcUrl = JdbcUrlTokenizer.tokenize(null);
    assertAll(
        () -> assertThat("database system identifier", jdbcUrl.databaseSystemIdentifier(), is("")),
        () -> assertThat("host", jdbcUrl.host(), is("")),
        () -> assertThat("database name", jdbcUrl.databaseName(), is("")));
  }

  @Test
  public void parseBracketedIpv6HostPort() {
    final JdbcUrlTokens jdbcUrl =
        JdbcUrlTokenizer.tokenize("jdbc:postgresql://[2001:db8::10]:5432/mydb");
    assertAll(
        () ->
            assertThat(
                "database system identifier", jdbcUrl.databaseSystemIdentifier(), is("postgresql")),
        () -> assertThat("host", jdbcUrl.host(), matchesPattern(NOT_BLANK)),
        () ->
            assertThat(
                "host classification", jdbcUrl.hostClassification(), is(HostClassification.PUBLIC)),
        () -> assertThat("database name", jdbcUrl.databaseName(), is("mydb")));
  }

  @Test
  public void parseHostListUsesFirstHost() {
    final JdbcUrlTokens jdbcUrl =
        JdbcUrlTokenizer.tokenize("jdbc:postgresql://host1:5432,host2:5433,host3:5434/mydb");
    assertAll(
        () ->
            assertThat(
                "database system identifier", jdbcUrl.databaseSystemIdentifier(), is("postgresql")),
        () -> assertThat("host", jdbcUrl.host(), matchesPattern(NOT_BLANK)),
        () ->
            assertThat(
                "host classification", jdbcUrl.hostClassification(), is(HostClassification.PUBLIC)),
        () -> assertThat("database name", jdbcUrl.databaseName(), is("mydb")));
  }

  @Test
  public void parseHostPortDatabaseUrl() {
    final JdbcUrlTokens jdbcUrl =
        JdbcUrlTokenizer.tokenize("jdbc:mysql://db.example.com:3306/appdb");
    assertAll(
        () ->
            assertThat(
                "database system identifier", jdbcUrl.databaseSystemIdentifier(), is("mysql")),
        () -> assertThat("host", jdbcUrl.host(), matchesPattern(NOT_BLANK)),
        () ->
            assertThat(
                "host classification", jdbcUrl.hostClassification(), is(HostClassification.PUBLIC)),
        () -> assertThat("database name", jdbcUrl.databaseName(), is("appdb")));
  }

  @Test
  public void parseInvalidPortAsNull() {
    final JdbcUrlTokens jdbcUrl = JdbcUrlTokenizer.tokenize("jdbc:mysql://dbhost:notaport/appdb");
    assertAll(
        () ->
            assertThat(
                "database system identifier", jdbcUrl.databaseSystemIdentifier(), is("mysql")),
        () -> assertThat("host", jdbcUrl.host(), matchesPattern(NOT_BLANK)),
        () ->
            assertThat(
                "host classification", jdbcUrl.hostClassification(), is(HostClassification.PUBLIC)),
        () -> assertThat("database name", jdbcUrl.databaseName(), is("appdb")));
  }

  @Test
  public void parseJdbcWithoutDriverBody() {
    final JdbcUrlTokens jdbcUrl = JdbcUrlTokenizer.tokenize("jdbc:mysql");
    assertAll(
        () ->
            assertThat(
                "database system identifier", jdbcUrl.databaseSystemIdentifier(), is("mysql")),
        () -> assertThat("host", jdbcUrl.host(), is("")),
        () -> assertThat("database name", jdbcUrl.databaseName(), is("")));
  }

  @Test
  public void parseNestedAuthorityUrl() {
    final JdbcUrlTokens jdbcUrl =
        JdbcUrlTokenizer.tokenize("jdbc:hsqldb:hsql://localhost:9001/schemacrawler");
    assertAll(
        () ->
            assertThat(
                "database system identifier", jdbcUrl.databaseSystemIdentifier(), is("hsqldb")),
        () -> assertThat("host", jdbcUrl.host(), is("<localhost>")),
        () ->
            assertThat(
                "host classification",
                jdbcUrl.hostClassification(),
                is(HostClassification.LOCALHOST)),
        () -> assertThat("database name", jdbcUrl.databaseName(), is("schemacrawler")));
  }

  @Test
  public void parseNonAuthoritySemicolonFormFallbackToHeadToken() {
    final JdbcUrlTokens jdbcUrl =
        JdbcUrlTokenizer.tokenize("jdbc:sqlserver:sqlhost:1433;encrypt=true");
    assertAll(
        () ->
            assertThat(
                "database system identifier", jdbcUrl.databaseSystemIdentifier(), is("sqlserver")),
        () -> assertThat("host", jdbcUrl.host(), matchesPattern(NOT_BLANK)),
        () ->
            assertThat(
                "host classification", jdbcUrl.hostClassification(), is(HostClassification.PUBLIC)),
        () -> assertThat("database name", jdbcUrl.databaseName(), is("sqlhost:1433")));
  }

  @Test
  public void parseNonAuthoritySemicolonFormWithDatabase() {
    final JdbcUrlTokens jdbcUrl =
        JdbcUrlTokenizer.tokenize("jdbc:sqlserver:sqlhost:1433;database=Sales");
    assertAll(
        () ->
            assertThat(
                "database system identifier", jdbcUrl.databaseSystemIdentifier(), is("sqlserver")),
        () -> assertThat("host", jdbcUrl.host(), matchesPattern(NOT_BLANK)),
        () ->
            assertThat(
                "host classification", jdbcUrl.hostClassification(), is(HostClassification.PUBLIC)),
        () -> assertThat("database name", jdbcUrl.databaseName(), is("sales")));
  }

  @Test
  public void parseNonAuthoritySemicolonFormWithDatabaseName() {
    final JdbcUrlTokens jdbcUrl =
        JdbcUrlTokenizer.tokenize("jdbc:sqlserver:sqlhost:1433;databaseName=Sales");
    assertAll(
        () ->
            assertThat(
                "database system identifier", jdbcUrl.databaseSystemIdentifier(), is("sqlserver")),
        () -> assertThat("host", jdbcUrl.host(), matchesPattern(NOT_BLANK)),
        () ->
            assertThat(
                "host classification", jdbcUrl.hostClassification(), is(HostClassification.PUBLIC)),
        () -> assertThat("database name", jdbcUrl.databaseName(), is("sales")));
  }

  @Test
  public void parseNonJdbcUrl() {
    final JdbcUrlTokens jdbcUrl = JdbcUrlTokenizer.tokenize("mysql://db:3306/appdb");
    assertAll(
        () -> assertThat("database system identifier", jdbcUrl.databaseSystemIdentifier(), is("")),
        () -> assertThat("host", jdbcUrl.host(), is("")),
        () -> assertThat("database name", jdbcUrl.databaseName(), is("")));
  }

  @Test
  public void parseOfflineFileUrl() {
    final JdbcUrlTokens jdbcUrl = JdbcUrlTokenizer.tokenize("jdbc:offline:C:\\temp\\snapshot.db");
    assertAll(
        () ->
            assertThat(
                "database system identifier", jdbcUrl.databaseSystemIdentifier(), is("offline")),
        () -> assertThat("host", jdbcUrl.host(), is("")),
        () -> assertThat("database name", jdbcUrl.databaseName(), is("c:\\temp\\snapshot.db")));
  }

  @Test
  public void parseOracleAtHostSyntaxWithoutSlashes() {
    final JdbcUrlTokens jdbcUrl = JdbcUrlTokenizer.tokenize("jdbc:oracle:thin:@oracledb:1521/ORCL");
    assertAll(
        () ->
            assertThat(
                "database system identifier", jdbcUrl.databaseSystemIdentifier(), is("oracle")),
        () -> assertThat("host", jdbcUrl.host(), is("")),
        () -> assertThat("database name", jdbcUrl.databaseName(), is("thin:@oracledb:1521")));
  }

  @Test
  public void parseOracleStyleUrl() {
    final JdbcUrlTokens jdbcUrl =
        JdbcUrlTokenizer.tokenize("jdbc:oracle:thin:@//oracledb:1521/ORCLPDB1");
    assertAll(
        () ->
            assertThat(
                "database system identifier", jdbcUrl.databaseSystemIdentifier(), is("oracle")),
        () -> assertThat("host", jdbcUrl.host(), matchesPattern(NOT_BLANK)),
        () ->
            assertThat(
                "host classification", jdbcUrl.hostClassification(), is(HostClassification.PUBLIC)),
        () -> assertThat("database name", jdbcUrl.databaseName(), is("orclpdb1")));
  }

  @RepeatedTest(5)
  @DisplayName("Random database connection URL protocols test")
  public void parseSpyWrappedMysqlUrl() {
    final int randomLength = RandomUtils.insecure().randomInt(1, 10);
    final String spySubProtcol = RandomStringUtils.insecure().nextAlphabetic(randomLength);
    final JdbcUrlTokens jdbcUrl =
        JdbcUrlTokenizer.tokenize("jdbc:%s://dbhost:9999/appdb".formatted(spySubProtcol));
    assertThat(
        "For spy sub-procol <%s>".formatted(spySubProtcol),
        jdbcUrl.databaseSystemIdentifier(),
        is(spySubProtcol.toLowerCase()));
  }

  @Test
  public void parseSqlServerStyleUrl() {
    final JdbcUrlTokens jdbcUrl =
        JdbcUrlTokenizer.tokenize("jdbc:sqlserver://sqlhost:1433;databaseName=Sales");
    assertAll(
        () ->
            assertThat(
                "database system identifier", jdbcUrl.databaseSystemIdentifier(), is("sqlserver")),
        () -> assertThat("host", jdbcUrl.host(), matchesPattern(NOT_BLANK)),
        () ->
            assertThat(
                "host classification", jdbcUrl.hostClassification(), is(HostClassification.PUBLIC)),
        () -> assertThat("database name", jdbcUrl.databaseName(), is("sales")));
  }
}
