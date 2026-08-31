package us.fatehi.utility.jdbc.serverfingerprint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;

/** Tests for H2 Database JDBC URL parsing. */
class H2ParserTest {

  @Test
  void testH2FileAbsolute() {
    final String url = "jdbc:h2:file:/data/mydb";
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(url);

    assertAll(
        () -> assertThat("database system identifier", parsed.databaseSystemIdentifier(), is("h2")),
        () -> assertThat("host", parsed.host(), is("file")),
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
        () -> assertThat("has host", parsed.hasHost(), is(true)),
        () -> assertThat("has database name", parsed.hasDatabaseName(), is(true)),
        () -> assertThat("has public host", parsed.hasPublicHost(), is(false)));
  }

  @Test
  void testH2FileRelative() {
    final String url = "jdbc:h2:~/test";
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(url);

    assertAll(
        () -> assertThat("database system identifier", parsed.databaseSystemIdentifier(), is("h2")),
        () -> assertThat("host", parsed.host(), is("")),
        () -> assertThat("has port", parsed.hasPort(), is(false)),
        () -> assertThat("database name", parsed.databaseName(), is("~/test")),
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
  void testH2FileWithProperties() {
    final String url = "jdbc:h2:~/testdb;MODE=MySQL;DATABASE_TO_LOWER=TRUE";
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(url);

    assertAll(
        () -> assertThat("database system identifier", parsed.databaseSystemIdentifier(), is("h2")),
        () -> assertThat("host", parsed.host(), is("")),
        () -> assertThat("has port", parsed.hasPort(), is(false)),
        () -> assertThat("database name", parsed.databaseName(), is("~/testdb")),
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
  void testH2ImpliedFile() {
    final String url = "jdbc:h2:/opt/databases/mydb";
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(url);

    assertAll(
        () -> assertThat("database system identifier", parsed.databaseSystemIdentifier(), is("h2")),
        () -> assertThat("host", parsed.host(), is("")),
        () -> assertThat("has port", parsed.hasPort(), is(false)),
        () -> assertThat("database name", parsed.databaseName(), is("/opt/databases/mydb")),
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
  void testH2InMemory() {
    final String url = "jdbc:h2:mem:testdb";
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(url);

    assertAll(
        () -> assertThat("database system identifier", parsed.databaseSystemIdentifier(), is("h2")),
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
  void testH2InMemoryWithOptions() {
    final String url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL";
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(url);

    assertAll(
        () -> assertThat("database system identifier", parsed.databaseSystemIdentifier(), is("h2")),
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
  void testH2SSL() {
    final String url = "jdbc:h2:ssl://dbserver:9092/~/production";
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(url);

    assertAll(
        () -> assertThat("database system identifier", parsed.databaseSystemIdentifier(), is("h2")),
        () -> assertThat("host", parsed.host(), is("dbserver")),
        () -> assertThat("port", parsed.port(), is(9092)),
        () -> assertThat("database name", parsed.databaseName(), is("~/production")),
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
  void testH2TCP() {
    final String url = "jdbc:h2:tcp://localhost:9092/~/testdb";
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(url);

    assertAll(
        () -> assertThat("database system identifier", parsed.databaseSystemIdentifier(), is("h2")),
        () -> assertThat("host", parsed.host(), is("localhost")),
        () -> assertThat("port", parsed.port(), is(9092)),
        () -> assertThat("database name", parsed.databaseName(), is("~/testdb")),
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
  void testH2WithQuestionMarkProperties() {
    final String url = "jdbc:h2:mem:testdb?MODE=PostgreSQL&DATABASE_TO_LOWER=TRUE";
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(url);

    assertAll(
        () -> assertThat("database system identifier", parsed.databaseSystemIdentifier(), is("h2")),
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
}
