package us.fatehi.utility.jdbc.serverfingerprint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertAll;
import static us.fatehi.test.utility.TestUtility.NOT_BLANK;

import org.junit.jupiter.api.Test;

/** Tests for Oracle JDBC URL parsing. */
class OracleTNSParserTest {

  @Test
  void testOracleDescriptorCaseInsensitive() {

    final String url =
        "jdbc:oracle:thin:@(description=(address=(protocol=TCP)(host=myhost)(port=1522))(connect_data=(service_name=PROD)))";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () -> assertThat("has host", parsed.hasHost(), is(false)),
        () -> assertThat("database name", parsed.databaseName(), matchesPattern(NOT_BLANK)));
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
        () -> assertThat("database name", parsed.databaseName(), matchesPattern(NOT_BLANK)));
  }

  @Test
  void testOracleDescriptorWithSID() {

    final String url =
        "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=dbhost)(PORT=1521))(CONNECT_DATA=(SID=ORCL)))";
    final String connectionUrl = url;
    final JdbcUrlTokens parsed = JdbcUrlTokenizer.tokenize(connectionUrl);

    assertAll(
        () -> assertThat("has host", parsed.hasHost(), is(false)),
        () -> assertThat("database name", parsed.databaseName(), matchesPattern(NOT_BLANK)));
  }
}
