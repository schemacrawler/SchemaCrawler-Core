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

import org.junit.jupiter.api.Test;

public class JdbcUrlHostTest {

  @Test
  public void parseNormalizesPublicHostAndMarksItAsPublic() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:mysql://Db.Example.Com:3306/appdb");

    assertThat(jdbcUrl.hostHash(), matchesPattern(NOT_BLANK));
    assertThat(jdbcUrl.hasHost(), is(true));
    assertThat(jdbcUrl.hostClassification(), is(HostClassification.PUBLIC));
    assertThat(jdbcUrl.hasPublicHost(), is(true));
  }

  @Test
  public void parseMarksPrivateIpv4AsInternalIp() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:postgresql://10.0.0.7:5432/appdb");

    assertThat(jdbcUrl.hostHash(), is("<internal>"));
    assertThat(jdbcUrl.hasHost(), is(true));
    assertThat(jdbcUrl.hostClassification(), is(HostClassification.INTERNAL));
    assertThat(jdbcUrl.hasPublicHost(), is(false));
  }

  @Test
  public void parseMarksIpv6LiteralAsInternalOrIp() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:postgresql://[2001:db8::10]:5432/appdb");

    assertThat(jdbcUrl.hostHash(), matchesPattern(NOT_BLANK));
    assertThat(jdbcUrl.hasHost(), is(true));
    assertThat(jdbcUrl.hostClassification(), is(HostClassification.PUBLIC));
    assertThat(jdbcUrl.hasPublicHost(), is(true));
  }

  @Test
  public void parseLocalUrlHasNoHost() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:sqlite::memory:");

    assertThat(jdbcUrl.hasDatabaseSystemIdentifier(), is(true));
    assertThat(jdbcUrl.hasHost(), is(false));
    assertThat(jdbcUrl.hasDatabaseName(), is(true));
    assertThat(jdbcUrl.hostClassification(), is(HostClassification.PUBLIC));
    assertThat(jdbcUrl.hasPublicHost(), is(false));
  }

  @Test
  public void parseJdbcWithoutDriverBodyHasTypeButNoHostOrPort() {
    final JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:mysql");

    assertThat(jdbcUrl.hasDatabaseSystemIdentifier(), is(true));
    assertThat(jdbcUrl.hasHost(), is(false));
    assertThat(jdbcUrl.hasDatabaseName(), is(false));
  }
}
