package us.fatehi.utility.jdbc.serverfingerprint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;

/** Tests for Microsoft SQL Server JDBC URL parsing. */
class SQLServerParserTest {

  @Test
  void testSimpleSQLServerUrl() {

    final String url = "jdbc:sqlserver://localhost:1433;databaseName=mydb";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("sqlserver")),
        () -> assertThat("host", parsed.host(), is("localhost")),
        () -> assertThat("port", parsed.port(), is(1433)),
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
  void testSQLServerComplexUrl() {

    final String url =
        "jdbc:sqlserver://myserver:1433;databaseName=AdventureWorks;user=sa;password=secret;encrypt=true;trustServerCertificate=true;loginTimeout=30";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("sqlserver")),
        () -> assertThat("host", parsed.host(), is("myserver")),
        () -> assertThat("port", parsed.port(), is(1433)),
        () -> assertThat("database name", parsed.databaseName(), is("AdventureWorks")),
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
  void testSQLServerDefaultPort() {

    final String url = "jdbc:sqlserver://dbserver;databaseName=production";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("sqlserver")),
        () -> assertThat("host", parsed.host(), is("dbserver")),
        () -> assertThat("has port", parsed.hasPort(), is(false)),
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
  void testSQLServerInstanceWithoutPort() {

    final String url = "jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=mydb";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("sqlserver")),
        () -> assertThat("host", parsed.host(), is("localhost\\SQLEXPRESS")),
        () -> assertThat("has port", parsed.hasPort(), is(false)),
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
  void testSQLServerIntegratedSecurity() {

    final String url = "jdbc:sqlserver://localhost;databaseName=mydb;integratedSecurity=true";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("sqlserver")),
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
  void testSQLServerWithDatabase() {

    final String url = "jdbc:sqlserver://localhost:1433;database=testdb";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("sqlserver")),
        () -> assertThat("host", parsed.host(), is("localhost")),
        () -> assertThat("port", parsed.port(), is(1433)),
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
  void testSQLServerWithInstanceName() {

    final String url = "jdbc:sqlserver://localhost\\SQLEXPRESS:1433;databaseName=mydb";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("sqlserver")),
        () -> assertThat("host", parsed.host(), is("localhost\\SQLEXPRESS")),
        () -> assertThat("port", parsed.port(), is(1433)),
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
  void testSQLServerWithProperties() {

    final String url =
        "jdbc:sqlserver://localhost:1433;databaseName=testdb;encrypt=true;trustServerCertificate=false";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("sqlserver")),
        () -> assertThat("host", parsed.host(), is("localhost")),
        () -> assertThat("port", parsed.port(), is(1433)),
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
}
