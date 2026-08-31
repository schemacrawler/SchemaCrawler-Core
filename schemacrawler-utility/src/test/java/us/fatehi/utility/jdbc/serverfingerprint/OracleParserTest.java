package us.fatehi.utility.jdbc.serverfingerprint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/** Tests for Oracle JDBC URL parsing. */
@Disabled
class OracleParserTest {

  @Test
  void testOracleDescriptorCaseInsensitive() {

    final String url =
        "jdbc:oracle:thin:@(description=(address=(protocol=TCP)(host=myhost)(port=1522))(connect_data=(service_name=PROD)))";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () -> assertThat("has host", parsed.hasHost(), is(false)),
        () -> assertThat("database name", parsed.databaseName(), is("PROD")));
  }

  @Test
  void testOracleDescriptorFormat() {

    final String url =
        "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=localhost)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=myservice)))";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("oracle")),
        () -> assertThat("has host", parsed.hasHost(), is(false)),
        () -> assertThat("database name", parsed.databaseName(), is("myservice")));
  }

  @Test
  void testOracleDescriptorWithSID() {

    final String url =
        "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=dbhost)(PORT=1521))(CONNECT_DATA=(SID=ORCL)))";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () -> assertThat("has host", parsed.hasHost(), is(false)),
        () -> assertThat("database name", parsed.databaseName(), is("")));
  }

  @Test
  void testOracleOCIDriver() {

    final String url = "jdbc:oracle:oci:@//localhost:1521/mydb";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("oracle")),
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
  void testOracleThinWithServiceName() {

    final String url = "jdbc:oracle:thin:@//localhost:1521/XEPDB1";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("oracle")),
        () -> assertThat("host", parsed.host(), is("<localhost>")),
        () -> assertThat("database name", parsed.databaseName(), is("XEPDB1")),
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
  void testOracleThinWithSID() {

    final String url = "jdbc:oracle:thin:@localhost:1521:ORCL";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("oracle")),
        () -> assertThat("host", parsed.host(), is("localhost")),
        () -> assertThat("database name", parsed.databaseName(), is("ORCL")),
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
  void testOracleWithServiceNameNoPort() {

    final String url = "jdbc:oracle:thin:@dbserver.example.com:1522/myservice";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () ->
            assertThat(
                "database system identifier", parsed.databaseSystemIdentifier(), is("oracle")),
        () -> assertThat("host", parsed.host(), is("dbserver.example.com")),
        () -> assertThat("database name", parsed.databaseName(), is("myservice")),
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
