package us.fatehi.utility.jdbc.serverfingerprint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;

/** Tests for MySQL and MariaDB JDBC URL parsing. */
class MySQLParserTest {

  @Test
  void testMariaDBUrl() {

    final String url = "jdbc:mariadb://db.example.com:3307/production";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("mariadb")),
        () -> assertThat("host", parsed.host(), is("db.example.com")),
        () -> assertThat("port", parsed.port(), is(3307)),
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
  void testMariaDBWithProperties() {

    final String url = "jdbc:mariadb://localhost:3306/testdb?user=root&password=secret";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("mariadb")),
        () -> assertThat("host", parsed.host(), is("localhost")),
        () -> assertThat("port", parsed.port(), is(3306)),
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
  void testMySQLComplexUrl() {

    final String url =
        "jdbc:mysql://primary:3306,replica1:3306,replica2:3306/mydb?useSSL=true&rewriteBatchedStatements=true";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("mysql")),
        () -> assertThat("host", parsed.host(), is("primary")),
        () -> assertThat("port", parsed.port(), is(3306)),
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
  void testMySQLEmptyDatabase() {

    final String url = "jdbc:mysql://localhost:3306/";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("mysql")),
        () -> assertThat("host", parsed.host(), is("localhost")),
        () -> assertThat("port", parsed.port(), is(3306)),
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
  void testMySQLMultiHost() {

    final String url = "jdbc:mysql://host1:3306,host2:3307/mydb";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("mysql")),
        () -> assertThat("host", parsed.host(), is("host1")),
        () -> assertThat("port", parsed.port(), is(3306)),
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
  void testMySQLWithoutPort() {

    final String url = "jdbc:mysql://localhost/mydb";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("mysql")),
        () -> assertThat("host", parsed.host(), is("localhost")),
        () -> assertThat("has port", parsed.hasPort(), is(false)),
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
  void testMySQLWithProperties() {

    final String url = "jdbc:mysql://localhost:3306/mydb?useSSL=true&serverTimezone=UTC";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("mysql")),
        () -> assertThat("host", parsed.host(), is("localhost")),
        () -> assertThat("port", parsed.port(), is(3306)),
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
  void testSimpleMySQLUrl() {

    final String url = "jdbc:mysql://localhost:3306/mydb";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("mysql")),
        () -> assertThat("host", parsed.host(), is("localhost")),
        () -> assertThat("port", parsed.port(), is(3306)),
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
