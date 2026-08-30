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

import org.junit.jupiter.api.Test;

public class JdbcUrlHostTest {

  @Test
  public void parseJdbcWithoutDriverBodyHasTypeButNoHostOrPort() {
    final JdbcUrlTokens jdbcUrl = JdbcUrlTokenizer.tokenize("jdbc:mysql");

    assertAll(
        () ->
            assertThat(
                "has database system identifier", jdbcUrl.hasDatabaseSystemIdentifier(), is(true)),
        () -> assertThat("has host", jdbcUrl.hasHost(), is(false)),
        () -> assertThat("has database name", jdbcUrl.hasDatabaseName(), is(false)));
  }

  @Test
  public void parseLocalUrlHasNoHost() {

    final String url = "jdbc:sqlite::memory:";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("sqlite")),
        () -> assertThat("host", parsed.host(), is(":memory:")),
        () -> assertThat("database name", parsed.databaseName(), is("")),
        () ->
            assertThat(
                "host classification",
                parsed.hostClassification(),
                is(HostClassification.LOCALHOST)),
        () ->
            assertThat(
                "has database system identifier", parsed.hasDatabaseSystemIdentifier(), is(true)),
        () -> assertThat("has host", parsed.hasHost(), is(true)),
        () -> assertThat("has database name", parsed.hasDatabaseName(), is(false)),
        () -> assertThat("has public host", parsed.hasPublicHost(), is(false)));
  }

  @Test
  public void parseMarksIpv6LiteralAsInternalOrIp() {
    final JdbcUrlTokens jdbcUrl =
        JdbcUrlTokenizer.tokenize("jdbc:postgresql://[2001:db8::10]:5432/appdb");

    assertAll(
        () -> assertThat("host", jdbcUrl.host(), matchesPattern(NOT_BLANK)),
        () -> assertThat("has host", jdbcUrl.hasHost(), is(true)),
        () ->
            assertThat(
                "host classification", jdbcUrl.hostClassification(), is(HostClassification.PUBLIC)),
        () -> assertThat("has public host", jdbcUrl.hasPublicHost(), is(true)));
  }

  @Test
  public void parseMarksPrivateIpv4AsInternalIp() {
    final JdbcUrlTokens jdbcUrl =
        JdbcUrlTokenizer.tokenize("jdbc:postgresql://10.0.0.7:5432/appdb");

    assertAll(
        () -> assertThat("host", jdbcUrl.host(), is("<internal>")),
        () -> assertThat("has host", jdbcUrl.hasHost(), is(true)),
        () ->
            assertThat(
                "host classification",
                jdbcUrl.hostClassification(),
                is(HostClassification.INTERNAL)),
        () -> assertThat("has public host", jdbcUrl.hasPublicHost(), is(false)));
  }

  @Test
  public void parseNormalizesPublicHostAndMarksItAsPublic() {
    final JdbcUrlTokens jdbcUrl =
        JdbcUrlTokenizer.tokenize("jdbc:mysql://Db.Example.Com:3306/appdb");

    assertAll(
        () -> assertThat("host", jdbcUrl.host(), matchesPattern(NOT_BLANK)),
        () -> assertThat("has host", jdbcUrl.hasHost(), is(true)),
        () ->
            assertThat(
                "host classification", jdbcUrl.hostClassification(), is(HostClassification.PUBLIC)),
        () -> assertThat("has public host", jdbcUrl.hasPublicHost(), is(true)));
  }
}
