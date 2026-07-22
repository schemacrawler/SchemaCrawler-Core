package us.fatehi.test.integration.utility;

import java.time.Duration;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;

final class SnowflakeLicenseActivationWaitStrategy extends AbstractWaitStrategy {

  private static final Predicate<String> LICENSE_ACTIVATION_SUCCESSFUL_LOG_MESSAGE =
      Pattern.compile(".*Successfully .*activated.* license.*", Pattern.DOTALL).asMatchPredicate();
  private static final String LICENSE_ACTIVATION_FAILED_LOG_MESSAGE = "License activation failed!";

  private static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(5);
  private static final Duration WAIT_POLL_INTERVAL = Duration.ofMillis(500);

  @Override
  protected void waitUntilReady() {
    final Duration startupTimeout =
        this.startupTimeout == null ? STARTUP_TIMEOUT : this.startupTimeout;
    final long timeoutNanos = startupTimeout.toNanos();
    final long startNanos = System.nanoTime();
    while (System.nanoTime() - startNanos < timeoutNanos) {
      final String logs = waitStrategyTarget.getLogs();
      if (logs != null && logs.contains(LICENSE_ACTIVATION_FAILED_LOG_MESSAGE)) {
        throw new ContainerLaunchException(
            "Snowflake license activation failed; check LOCALSTACK_AUTH_TOKEN");
      }
      if (logs != null && LICENSE_ACTIVATION_SUCCESSFUL_LOG_MESSAGE.test(logs)) {
        return;
      }
      sleepPollInterval();
    }
    throw new ContainerLaunchException("Timed out waiting for successful Snowflake startup");
  }

  Duration startupTimeoutForTesting() {
    return startupTimeout;
  }

  private void sleepPollInterval() {
    try {
      Thread.sleep(WAIT_POLL_INTERVAL.toMillis());
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(
          "Interrupted while waiting for Snowflake license activation", e);
    }
  }
}
