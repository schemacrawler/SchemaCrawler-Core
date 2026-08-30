package us.fatehi.utility.jdbc.serverfingerprint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;

/** Tests for PostgreSQL JDBC URL parsing. */
class PostgreSQLParserTest {

  @Test
  void testPostgreSQLComplexProperties() {

    final String url =
        "jdbc:postgresql://localhost:5432/testdb?user=postgres&password=secret&ssl=true&sslmode=verify-full&sslcert=/path/to/cert";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("postgresql")),
        () -> assertThat("host", parsed.host(), is("<localhost>")),
        () -> assertThat("database name", parsed.databaseName(), is("testdb")),
        () ->
            assertThat(
                "host classification",
                parsed.hostClassification(),
                is(HostClassification.LOCALHOST)),
        () ->
            assertThat(
                "has database system identifier", parsed.hasDatabaseSystemIdentifier(), is(true)),
        () -> assertThat("has host", parsed.hasHost(), is(true)),
        () -> assertThat("has database name", parsed.hasDatabaseName(), is(true)),
        () -> assertThat("has public host", parsed.hasPublicHost(), is(false)));
  }

  @Test
  void testPostgreSQLIPv6() {

    final String url = "jdbc:postgresql://[::1]:5432/mydb";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("postgresql")),
        () -> assertThat("host", parsed.host(), is("<localhost>")),
        () -> assertThat("database name", parsed.databaseName(), is("mydb")),
        () ->
            assertThat(
                "host classification",
                parsed.hostClassification(),
                is(HostClassification.LOCALHOST)),
        () ->
            assertThat(
                "has database system identifier", parsed.hasDatabaseSystemIdentifier(), is(true)),
        () -> assertThat("has host", parsed.hasHost(), is(true)),
        () -> assertThat("has database name", parsed.hasDatabaseName(), is(true)),
        () -> assertThat("has public host", parsed.hasPublicHost(), is(false)));
  }

  @Test
  void testPostgreSQLMultiHost() {

    final String url = "jdbc:postgresql://host1:5432,host2:5433/mydb";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("postgresql")),
        () -> assertThat("host", parsed.host(), is("host1")),
        () -> assertThat("database name", parsed.databaseName(), is("mydb")),
        () ->
            assertThat(
                "host classification", parsed.hostClassification(), is(HostClassification.PUBLIC)),
        () ->
            assertThat(
                "has database system identifier", parsed.hasDatabaseSystemIdentifier(), is(true)),
        () -> assertThat("has host", parsed.hasHost(), is(true)),
        () -> assertThat("has database name", parsed.hasDatabaseName(), is(true)),
        () -> assertThat("has public host", parsed.hasPublicHost(), is(true)));
  }

  @Test
  void testPostgreSQLWithoutPort() {

    final String url = "jdbc:postgresql://dbserver/production";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("postgresql")),
        () -> assertThat("host", parsed.host(), is("dbserver")),
        () -> assertThat("database name", parsed.databaseName(), is("production")),
        () ->
            assertThat(
                "host classification", parsed.hostClassification(), is(HostClassification.PUBLIC)),
        () ->
            assertThat(
                "has database system identifier", parsed.hasDatabaseSystemIdentifier(), is(true)),
        () -> assertThat("has host", parsed.hasHost(), is(true)),
        () -> assertThat("has database name", parsed.hasDatabaseName(), is(true)),
        () -> assertThat("has public host", parsed.hasPublicHost(), is(true)));
  }

  @Test
  void testPostgreSQLWithProperties() {

    final String url = "jdbc:postgresql://localhost:5432/testdb?ssl=true&sslmode=require";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("postgresql")),
        () -> assertThat("host", parsed.host(), is("<localhost>")),
        () -> assertThat("database name", parsed.databaseName(), is("testdb")),
        () ->
            assertThat(
                "host classification",
                parsed.hostClassification(),
                is(HostClassification.LOCALHOST)),
        () ->
            assertThat(
                "has database system identifier", parsed.hasDatabaseSystemIdentifier(), is(true)),
        () -> assertThat("has host", parsed.hasHost(), is(true)),
        () -> assertThat("has database name", parsed.hasDatabaseName(), is(true)),
        () -> assertThat("has public host", parsed.hasPublicHost(), is(false)));
  }

  @Test
  void testPostgreSQLWithSchema() {

    final String url = "jdbc:postgresql://localhost/mydb?currentSchema=public";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("postgresql")),
        () -> assertThat("host", parsed.host(), is("<localhost>")),
        () -> assertThat("database name", parsed.databaseName(), is("mydb")),
        () ->
            assertThat(
                "host classification",
                parsed.hostClassification(),
                is(HostClassification.LOCALHOST)),
        () ->
            assertThat(
                "has database system identifier", parsed.hasDatabaseSystemIdentifier(), is(true)),
        () -> assertThat("has host", parsed.hasHost(), is(true)),
        () -> assertThat("has database name", parsed.hasDatabaseName(), is(true)),
        () -> assertThat("has public host", parsed.hasPublicHost(), is(false)));
  }

  @Test
  void testSimplePostgreSQLUrl() {

    final String url = "jdbc:postgresql://localhost:5432/mydb";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("postgresql")),
        () -> assertThat("host", parsed.host(), is("<localhost>")),
        () -> assertThat("database name", parsed.databaseName(), is("mydb")),
        () ->
            assertThat(
                "host classification",
                parsed.hostClassification(),
                is(HostClassification.LOCALHOST)),
        () ->
            assertThat(
                "has database system identifier", parsed.hasDatabaseSystemIdentifier(), is(true)),
        () -> assertThat("has host", parsed.hasHost(), is(true)),
        () -> assertThat("has database name", parsed.hasDatabaseName(), is(true)),
        () -> assertThat("has public host", parsed.hasPublicHost(), is(false)));
  }
}
