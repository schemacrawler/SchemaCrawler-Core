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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

public class MultiThreadedTaskRunnerTest {

  @Test
  public void completesFastTaskWithTimeout() throws Exception {
    final AtomicBoolean taskCompleted = new AtomicBoolean();
    try (final TaskRunner taskRunner =
        new MultiThreadedTaskRunner("test", new ThreadingOptions(1, 1))) {
      taskRunner.add(new TaskDefinition("fast task", () -> taskCompleted.set(true)));
      taskRunner.submit();
    }
    assertThat(taskCompleted.get(), is(true));
  }

  @Test
  public void completesSlowTaskWhenNegativeTimeoutDisabled() throws Exception {
    final AtomicBoolean taskCompleted = new AtomicBoolean();
    try (final TaskRunner taskRunner =
        new MultiThreadedTaskRunner("test", new ThreadingOptions(1, -1))) {
      taskRunner.add(
          new TaskDefinition(
              "slow task",
              () -> {
                Thread.sleep(100);
                taskCompleted.set(true);
              }));
      taskRunner.submit();
    }
    assertThat(taskCompleted.get(), is(true));
  }

  @Test
  public void completesSlowTaskWhenTimeoutDisabled() throws Exception {
    final AtomicBoolean taskCompleted = new AtomicBoolean();
    try (final TaskRunner taskRunner =
        new MultiThreadedTaskRunner("test", new ThreadingOptions(1, 0))) {
      taskRunner.add(
          new TaskDefinition(
              "slow task",
              () -> {
                Thread.sleep(1000);
                taskCompleted.set(true);
              }));
      taskRunner.submit();
    }
    assertThat(taskCompleted.get(), is(true));
  }

  @Test
  public void preservesConfiguredTimeoutValues() {
    assertThat(new ThreadingOptions(1, 1).timeoutSeconds(), is(1));
    assertThat(new ThreadingOptions(1, 0).timeoutSeconds(), is(0));
    assertThat(new ThreadingOptions(1, -1).timeoutSeconds(), is(0));
    assertThat(new ThreadingOptions(1, 3600).timeoutSeconds(), is(3600));
  }

  @Test
  public void reportsTimedOutTaskAsFailure() throws Exception {
    final ThreadingOptions threadingOptions = mock(ThreadingOptions.class);
    when(threadingOptions.maxThreads()).thenReturn(1);
    when(threadingOptions.timeoutSeconds()).thenReturn(1);
    final AtomicBoolean taskCompleted = new AtomicBoolean();

    try (final TaskRunner taskRunner = new MultiThreadedTaskRunner("test", threadingOptions)) {
      taskRunner.add(
          new TaskDefinition(
              "slow task",
              () -> {
                Thread.sleep(5000);
                taskCompleted.set(true);
              }));

      final TaskTimeoutException exception =
          assertThrows(TaskTimeoutException.class, taskRunner::submit);
      assertThat(exception.getMessage(), containsString("slow_task"));
      assertThat(exception.getMessage(), containsString("<1> seconds"));
    }
    assertThat(taskCompleted.get(), is(false));
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
