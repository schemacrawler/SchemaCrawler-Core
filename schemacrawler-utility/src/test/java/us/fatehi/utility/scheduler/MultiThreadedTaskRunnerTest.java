/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.scheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class MultiThreadedTaskRunnerTest {

  @Test
  public void completesFastTaskWithTimeout() throws Exception {
    try (final TaskRunner taskRunner = new MultiThreadedTaskRunner("test", 1, 1)) {
      taskRunner.add(new TaskDefinition("fast task"));
      taskRunner.submit();
    }
  }

  @Test
  public void completesSlowTaskWhenNegativeTimeoutDisabled() throws Exception {
    try (final TaskRunner taskRunner = new MultiThreadedTaskRunner("test", 1, -1)) {
      taskRunner.add(new TaskDefinition("slow task", () -> Thread.sleep(100)));
      taskRunner.submit();
    }
  }

  @Test
  public void completesSlowTaskWhenTimeoutDisabled() throws Exception {
    try (final TaskRunner taskRunner = new MultiThreadedTaskRunner("test", 1, 0)) {
      taskRunner.add(new TaskDefinition("slow task", () -> Thread.sleep(1000)));
      taskRunner.submit();
    }
  }

  @Test
  public void reportsTimedOutTaskAsFailure() throws Exception {
    try (final TaskRunner taskRunner = new MultiThreadedTaskRunner("test", 1, 1)) {
      taskRunner.add(new TaskDefinition("slow task", () -> Thread.sleep(5000)));

      final TaskTimeoutException exception =
          assertThrows(TaskTimeoutException.class, taskRunner::submit);
      assertThat(exception.getMessage(), containsString("slow_task"));
      assertThat(exception.getMessage(), containsString("<1> seconds"));
    }
  }

  @Test
  public void timeoutExceptionDescribesTask() {
    final TaskTimeoutException exception = new TaskTimeoutException("task_name", null, 42);
    assertThat(
        exception.getMessage(),
        is(
            "Task <task_name> started at <null> was cancelled after exceeding the configured"
                + " timeout of <42> seconds"));
  }
}
