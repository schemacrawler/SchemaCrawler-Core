package us.fatehi.utility.jdbc.serverfingerprint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;

/** Tests for SQLite JDBC URL parsing. */
class SQLiteParserTest {

  @Test
  void testSQLiteAbsolutePath() {

    final String url = "jdbc:sqlite:/data/databases/mydb.db";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("sqlite")),
        () -> assertThat("host", parsed.host(), is("")),
        () -> assertThat("database name", parsed.databaseName(), is("/data/databases/mydb.db")),
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
  void testSQLiteComplexPath() {

    final String url = "jdbc:sqlite:/var/lib/app/data/application.db?cache=shared&mode=ro";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("sqlite")),
        () -> assertThat("host", parsed.host(), is("")),
        () ->
            assertThat(
                "database name", parsed.databaseName(), is("/var/lib/app/data/application.db")),
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
  void testSQLiteEmptyPath() {

    final String url = "jdbc:sqlite:";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("sqlite")),
        () -> assertThat("host", parsed.host(), is("")),
        () -> assertThat("database name", parsed.databaseName(), is("")),
        () ->
            assertThat(
                "host classification", parsed.hostClassification(), is(HostClassification.UNKNOWN)),
        () ->
            assertThat(
                "has database system identifier", parsed.hasDatabaseSystemIdentifier(), is(true)),
        () -> assertThat("has host", parsed.hasHost(), is(false)),
        () -> assertThat("has database name", parsed.hasDatabaseName(), is(false)),
        () -> assertThat("has public host", parsed.hasPublicHost(), is(false)));
  }

  @Test
  void testSQLiteInMemory() {

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
  void testSQLiteRelativePath() {

    final String url = "jdbc:sqlite:../data/mydb.db";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("sqlite")),
        () -> assertThat("host", parsed.host(), is("")),
        () -> assertThat("database name", parsed.databaseName(), is("../data/mydb.db")),
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
  void testSQLiteSimpleFile() {

    final String url = "jdbc:sqlite:test.db";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("sqlite")),
        () -> assertThat("host", parsed.host(), is("")),
        () -> assertThat("database name", parsed.databaseName(), is("test.db")),
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
  void testSQLiteWindowsPath() {

    final String url = "jdbc:sqlite:C:\\databases\\test.db";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("sqlite")),
        () -> assertThat("host", parsed.host(), is("")),
        () -> assertThat("database name", parsed.databaseName(), is("c:\\databases\\test.db")),
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
  void testSQLiteWithProperties() {

    final String url = "jdbc:sqlite:test.db?foreign_keys=true&journal_mode=WAL";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("sqlite")),
        () -> assertThat("host", parsed.host(), is("")),
        () -> assertThat("database name", parsed.databaseName(), is("test.db")),
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
}
