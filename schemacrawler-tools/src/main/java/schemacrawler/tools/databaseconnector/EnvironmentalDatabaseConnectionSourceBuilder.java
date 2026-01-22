package schemacrawler.tools.databaseconnector;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.isBlank;
import static us.fatehi.utility.Utility.trimToEmpty;

import us.fatehi.utility.UtilityMarker;
import us.fatehi.utility.datasource.DatabaseConnectionSourceBuilder;
import us.fatehi.utility.datasource.MultiUseUserCredentials;
import us.fatehi.utility.datasource.UserCredentials;
import us.fatehi.utility.readconfig.EnvironmentVariableConfig;
import us.fatehi.utility.readconfig.ReadConfig;

@UtilityMarker
public final class EnvironmentalDatabaseConnectionSourceBuilder {

  private static final String JDBC_URL = "SCHCRWLR_JDBC_URL";
  private static final String SERVER = "SCHCRWLR_SERVER";
  private static final String DATABASE = "SCHCRWLR_DATABASE";
  private static final String HOST = "SCHCRWLR_HOST";
  private static final String PORT = "SCHCRWLR_PORT";
  private static final String DATABASE_USER = "SCHCRWLR_DATABASE_USER";
  private static final String DATABASE_PASSWORD = "SCHCRWLR_DATABASE_PASSWORD";

  /**
   * Builds a database connection reading standard environmental variables.
   *
   * @return Builder
   */
  public static DatabaseConnectionSourceBuilder builder() {
    return builder((EnvironmentVariableConfig) System::getenv);
  }

  /**
   * Builds a database connection reading from a config.
   *
   * @param config Config for string properties.
   * @return Builder
   */
  public static DatabaseConnectionSourceBuilder builder(final ReadConfig config) {

    requireNonNull(config, "No environmental accessor provided");

    final DatabaseConnectionSourceBuilder dbConnectionSourceBuilder;
    final String connectionUrl = trimToEmpty(config.getStringValue(JDBC_URL));

    if (!isBlank(connectionUrl)) {
      dbConnectionSourceBuilder = builderFromUrl(connectionUrl);
    } else {
      dbConnectionSourceBuilder = builderForServer(config);
    }

    final UserCredentials userCredentials =
        new MultiUseUserCredentials(
            trimToEmpty(config.getStringValue(DATABASE_USER)),
            config.getStringValue(DATABASE_PASSWORD, null));
    dbConnectionSourceBuilder.withUserCredentials(userCredentials);

    return dbConnectionSourceBuilder;
  }

  private static DatabaseConnectionSourceBuilder builderForServer(final ReadConfig config) {
    final DatabaseConnectionSourceBuilder dbConnectionSourceBuilder;
    final String databaseSystemIdentifier = trimToEmpty(config.getStringValue(SERVER));

    final DatabaseConnectorRegistry databaseConnectorRegistry =
        DatabaseConnectorRegistry.getDatabaseConnectorRegistry();
    final DatabaseConnector databaseConnector =
        databaseConnectorRegistry.findDatabaseConnectorFromDatabaseSystemIdentifier(
            databaseSystemIdentifier);

    dbConnectionSourceBuilder = databaseConnector.databaseConnectionSourceBuilder();

    final String host = trimToEmpty(config.getStringValue(HOST));
    dbConnectionSourceBuilder.withHost(host);

    final String port = trimToEmpty(config.getStringValue(PORT));
    if (isValidPort(port)) {
      dbConnectionSourceBuilder.withPort(Integer.valueOf(port));
    }

    final String database = trimToEmpty(config.getStringValue(DATABASE));
    dbConnectionSourceBuilder.withDatabase(database);

    return dbConnectionSourceBuilder;
  }

  private static DatabaseConnectionSourceBuilder builderFromUrl(final String connectionUrl) {
    final DatabaseConnectionSourceBuilder dbConnectionSourceBuilder;

    // This JDBC URL is not expected to have any substitutable parameters, so subsequent
    // settings should not have any effect
    dbConnectionSourceBuilder = DatabaseConnectionSourceBuilder.builder(connectionUrl);

    final DatabaseConnectorRegistry databaseConnectorRegistry =
        DatabaseConnectorRegistry.getDatabaseConnectorRegistry();
    DatabaseConnector databaseConnector =
        databaseConnectorRegistry.findDatabaseConnectorFromUrl(connectionUrl);
    dbConnectionSourceBuilder.withConnectionInitializer(
        databaseConnector.databaseConnectionSourceBuilder().getConnectionInitializer());

    return dbConnectionSourceBuilder;
  }

  /**
   * Checks if a string is a valid numeric value.
   *
   * @param value The string to check
   * @return true if the string is a valid numeric value, false otherwise
   */
  private static boolean isValidPort(final String value) {
    if (isBlank(value)) {
      return false;
    }
    try {
      final int port = Integer.parseInt(value);
      return port > 1023 && port < 65536;
    } catch (final NumberFormatException e) {
      return false;
    }
  }

  private EnvironmentalDatabaseConnectionSourceBuilder() {
    // Prevent instantiation
  }
}
