package us.fatehi.utility.jdbc.serverfingerprint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;

/** Tests for HSQLDB (HyperSQL) JDBC URL parsing. */
class HSQLDBParserTest {

  @Test
  void testHSQLDBFile() {

    final String url = "jdbc:hsqldb:file:/opt/db/testdb";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("hsqldb")),
        () -> assertThat("host", parsed.host(), is("file")),
        () -> assertThat("has port", parsed.hasPort(), is(false)),
        () -> assertThat("database name", parsed.databaseName(), is("/opt/db/testdb")),
        () ->
            assertThat(
                "host classification",
                parsed.hostClassification(),
                is(HostClassification.INTERNAL)),
        () ->
            assertThat(
                "has database system identifier", parsed.hasDatabaseSystemIdentifier(), is(true)),
        () -> assertThat("has host", parsed.hasHost(), is(true)),
        () -> assertThat("has database name", parsed.hasDatabaseName(), is(true)),
        () -> assertThat("has public host", parsed.hasPublicHost(), is(false)));
  }

  @Test
  void testHSQLDBFileWithProperties() {

    final String url = "jdbc:hsqldb:file:testdb;shutdown=true;hsqldb.tx=mvcc";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("hsqldb")),
        () -> assertThat("host", parsed.host(), is("file")),
        () -> assertThat("has port", parsed.hasPort(), is(false)),
        () -> assertThat("database name", parsed.databaseName(), is("testdb")),
        () ->
            assertThat(
                "host classification",
                parsed.hostClassification(),
                is(HostClassification.INTERNAL)),
        () ->
            assertThat(
                "has database system identifier", parsed.hasDatabaseSystemIdentifier(), is(true)),
        () -> assertThat("has host", parsed.hasHost(), is(true)),
        () -> assertThat("has database name", parsed.hasDatabaseName(), is(true)),
        () -> assertThat("has public host", parsed.hasPublicHost(), is(false)));
  }

  @Test
  void testHSQLDBHTTP() {

    final String url = "jdbc:hsqldb:http://localhost:8080/mydb";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("hsqldb")),
        () -> assertThat("host", parsed.host(), is("localhost")),
        () -> assertThat("port", parsed.port(), is(8080)),
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
  void testHSQLDBHTTPS() {

    final String url = "jdbc:hsqldb:https://secure.example.com:8443/securedb";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("hsqldb")),
        () -> assertThat("host", parsed.host(), is("secure.example.com")),
        () -> assertThat("port", parsed.port(), is(8443)),
        () -> assertThat("database name", parsed.databaseName(), is("securedb")),
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
  void testHSQLDBImpliedFile() {

    final String url = "jdbc:hsqldb:/data/mydb";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("hsqldb")),
        () -> assertThat("host", parsed.host(), is("")),
        () -> assertThat("has port", parsed.hasPort(), is(false)),
        () -> assertThat("database name", parsed.databaseName(), is("/data/mydb")),
        () ->
            assertThat(
                "host classification",
                parsed.hostClassification(),
                is(HostClassification.INTERNAL)),
        () ->
            assertThat(
                "has database system identifier", parsed.hasDatabaseSystemIdentifier(), is(true)),
        () -> assertThat("has host", parsed.hasHost(), is(false)),
        () -> assertThat("has database name", parsed.hasDatabaseName(), is(true)),
        () -> assertThat("has public host", parsed.hasPublicHost(), is(false)));
  }

  @Test
  void testHSQLDBInMemory() {

    final String url = "jdbc:hsqldb:mem:testdb";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("hsqldb")),
        () -> assertThat("host", parsed.host(), is("mem")),
        () -> assertThat("has port", parsed.hasPort(), is(false)),
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
  void testHSQLDBInMemoryWithProperties() {

    final String url = "jdbc:hsqldb:mem:testdb?shutdown=true";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("hsqldb")),
        () -> assertThat("host", parsed.host(), is("mem")),
        () -> assertThat("has port", parsed.hasPort(), is(false)),
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
  void testHSQLDBResource() {

    final String url = "jdbc:hsqldb:res:/org/mydatabase/mydb";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("hsqldb")),
        () -> assertThat("host", parsed.host(), is("res")),
        () -> assertThat("has port", parsed.hasPort(), is(false)),
        () -> assertThat("database name", parsed.databaseName(), is("/org/mydatabase/mydb")),
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
  void testHSQLDBServer() {

    final String url = "jdbc:hsqldb:hsql://localhost:9001/testdb";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("hsqldb")),
        () -> assertThat("host", parsed.host(), is("localhost")),
        () -> assertThat("port", parsed.port(), is(9001)),
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
  void testHSQLDBServerSSL() {

    final String url = "jdbc:hsqldb:hsqls://dbserver:9002/production";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("hsqldb")),
        () -> assertThat("host", parsed.host(), is("dbserver")),
        () -> assertThat("port", parsed.port(), is(9002)),
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
}
