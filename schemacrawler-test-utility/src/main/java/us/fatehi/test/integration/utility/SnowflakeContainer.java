/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.test.integration.utility;

import static java.time.temporal.ChronoUnit.MINUTES;

import java.time.Duration;
import java.util.Set;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

public class SnowflakeContainer extends JdbcDatabaseContainer<SnowflakeContainer> {

  private static final int EDGE_PORT = 4566;
  private static final int HTTPS_PORT = 443;
  private static final int SERVICE_PORT_START = 4510;
  private static final int SERVICE_PORT_END = 4559;

  private String databaseName = "test";
  private String authToken = resolveAuthToken();

  public SnowflakeContainer(final DockerImageName dockerImageName) {
    super(dockerImageName);
  }

  @Override
  public String getDatabaseName() {
    return databaseName;
  }

  @Override
  public String getDriverClassName() {
    return "net.snowflake.client.jdbc.SnowflakeDriver";
  }

  @Override
  public String getJdbcUrl() {
    return "jdbc:snowflake://%s:%d/?account=localstack"
        .formatted(localstackSnowflakeHost(), getMappedPort(HTTPS_PORT));
  }

  @Override
  public Set<Integer> getLivenessCheckPortNumbers() {
    return Set.of(getMappedPort(EDGE_PORT), getMappedPort(HTTPS_PORT));
  }

  @Override
  public String getPassword() {
    return "test";
  }

  @Override
  public String getUsername() {
    return "test";
  }

  @Override
  public SnowflakeContainer withDatabaseName(final String databaseName) {
    this.databaseName = databaseName;
    return self();
  }

  public SnowflakeContainer withAuthToken(final String authToken) {
    if (authToken != null && !authToken.isBlank()) {
      this.authToken = authToken;
    }
    return self();
  }

  @Override
  public SnowflakeContainer withPassword(final String password) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SnowflakeContainer withUrlParam(final String paramName, final String paramValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SnowflakeContainer withUsername(final String username) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void configure() {
    super.configure();
    addExposedPort(EDGE_PORT);
    addExposedPort(HTTPS_PORT);
    for (int port = SERVICE_PORT_START; port <= SERVICE_PORT_END; port++) {
      addExposedPort(port);
    }
    addEnv("SERVICES", "snowflake");
    addEnv("EAGER_SERVICE_LOADING", "1");
    addEnv("LOCALSTACK_AUTH_TOKEN", requireAuthToken());
    waitingFor(
        new LogMessageWaitStrategy()
            .withRegEx(".*Ready\\..*")
            .withStartupTimeout(Duration.of(5, MINUTES)));
  }

  @Override
  protected String getTestQueryString() {
    return "SELECT 1";
  }

  @Override
  protected void waitUntilContainerStarted() {
    getWaitStrategy().waitUntilReady(this);
  }

  private String localstackSnowflakeHost() {
    final String host = getHost();
    if ("localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host)) {
      return "snowflake.localhost.localstack.cloud";
    }
    return host;
  }

  private String requireAuthToken() {
    if (authToken == null || authToken.isBlank()) {
      throw new IllegalStateException(
          "Missing LocalStack auth token for Snowflake container. Set LOCALSTACK_AUTH_TOKEN "
              + "or pass -Dlocalstack.auth.token=<developer-token>.");
    }
    return authToken;
  }

  private static String resolveAuthToken() {
    final String systemPropertyToken = System.getProperty("localstack.auth.token");
    if (systemPropertyToken != null && !systemPropertyToken.isBlank()) {
      return systemPropertyToken;
    }

    final String localstackPropertyToken = System.getProperty("LOCALSTACK_AUTH_TOKEN");
    if (localstackPropertyToken != null && !localstackPropertyToken.isBlank()) {
      return localstackPropertyToken;
    }

    return System.getenv("LOCALSTACK_AUTH_TOKEN");
  }
}
