/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */
package us.fatehi.test.integration.utility;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.DockerImageName;

public class SnowflakeContainerTest {

  private static final DockerImageName SNOWFLAKE_IMAGE_NAME =
      DockerImageName.parse("localstack/snowflake");

  @Test
  public void usesExplicitAuthTokenOverOtherSources() {
    final TestableSnowflakeContainer container =
        new TestableSnowflakeContainer("explicit", "env", "property")
            .withAuthTokenForTest("explicit");

    container.configureForTest();

    assertThat(container.environment().get("LOCALSTACK_AUTH_TOKEN"), is("explicit"));
  }

  @Test
  public void usesEnvironmentAuthTokenWhenExplicitTokenMissing() {
    final TestableSnowflakeContainer container =
        new TestableSnowflakeContainer(null, "env", "property");

    container.configureForTest();

    assertThat(container.environment().get("LOCALSTACK_AUTH_TOKEN"), is("env"));
  }

  @Test
  public void usesSystemPropertyAuthTokenWhenEnvironmentTokenMissing() {
    final TestableSnowflakeContainer container =
        new TestableSnowflakeContainer(null, null, "property");

    container.configureForTest();

    assertThat(container.environment().get("LOCALSTACK_AUTH_TOKEN"), is("property"));
  }

  @Test
  public void failsWhenNoAuthTokenIsProvided() {
    final TestableSnowflakeContainer container = new TestableSnowflakeContainer(null, null, null);

    final IllegalStateException exception =
        assertThrows(IllegalStateException.class, container::configureForTest);

    assertThat(
        exception.getMessage(),
        containsString("Set LOCALSTACK_AUTH_TOKEN using withAuthToken(...)"));
  }

  @Test
  public void waitsForSuccessfulLicenseActivationLogMessage() throws Exception {
    final TestableSnowflakeContainer container =
        new TestableSnowflakeContainer("explicit", null, null).withAuthTokenForTest("explicit");

    container.configureForTest();

    final WaitStrategy waitStrategy = container.waitStrategyForTest();
    assertThat(waitStrategy, instanceOf(SnowflakeLicenseActivationWaitStrategy.class));

    final SnowflakeLicenseActivationWaitStrategy activationWaitStrategy =
        (SnowflakeLicenseActivationWaitStrategy) waitStrategy;
    final Duration startupTimeout = activationWaitStrategy.startupTimeoutForTesting();
    assertThat(startupTimeout, is(Duration.ofMinutes(5)));
  }

  private static final class TestableSnowflakeContainer extends SnowflakeContainer {

    private final String environmentAuthToken;
    private final String systemPropertyAuthToken;

    private TestableSnowflakeContainer(
        final String explicitAuthToken,
        final String environmentAuthToken,
        final String systemPropertyAuthToken) {
      super(SNOWFLAKE_IMAGE_NAME);
      this.environmentAuthToken = environmentAuthToken;
      this.systemPropertyAuthToken = systemPropertyAuthToken;
      if (explicitAuthToken != null) {
        withAuthToken(explicitAuthToken);
      }
    }

    private TestableSnowflakeContainer withAuthTokenForTest(final String authToken) {
      withAuthToken(authToken);
      return this;
    }

    @Override
    protected String authTokenFromEnvironment() {
      return environmentAuthToken;
    }

    @Override
    protected String authTokenFromSystemProperty() {
      return systemPropertyAuthToken;
    }

    private void configureForTest() {
      configure();
    }

    private Map<String, String> environment() {
      return getEnvMap();
    }

    private WaitStrategy waitStrategyForTest() {
      return getWaitStrategy();
    }
  }
}
