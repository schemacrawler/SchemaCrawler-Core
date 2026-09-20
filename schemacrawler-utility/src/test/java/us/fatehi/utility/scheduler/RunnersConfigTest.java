/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.scheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class RunnersConfigTest {

  @Test
  public void invalidThreadingSettingsUseDefaults() {
    System.setProperty("SC_SINGLE_THREADED", "false");
    System.setProperty("SC_LOAD_MAX_THREADS", "not-a-number");
    System.setProperty("SC_LOAD_TIMEOUT_SECONDS", "not-a-number");

    assertThat(TaskRunners.isSingleThreaded(), is(false));

    final ThreadingOptions options = TaskRunners.getThreadingOptions();

    assertThat(options.maxThreads(), is(5));
    assertThat(options.timeoutSeconds(), is(3600));

    assertTaskRunnerReturnsMultiThreaded();
  }

  @Test
  public void negativeThreadingSettingsAreClamped() {
    System.setProperty("SC_SINGLE_THREADED", "false");
    System.setProperty("SC_LOAD_MAX_THREADS", "-1");
    System.setProperty("SC_LOAD_TIMEOUT_SECONDS", "-1");

    assertThat(TaskRunners.isSingleThreaded(), is(false));

    final ThreadingOptions options = TaskRunners.getThreadingOptions();

    assertThat(options.maxThreads(), is(1));
    assertThat(options.timeoutSeconds(), is(0));

    assertTaskRunnerReturnsMultiThreaded();
  }

  @Test
  public void noSettings() {

    assertThat(TaskRunners.isSingleThreaded(), is(false));

    final ThreadingOptions options = TaskRunners.getThreadingOptions();
    assertThat(options.maxThreads(), is(5));
    assertThat(options.timeoutSeconds(), is(3600));

    assertTaskRunnerReturnsMultiThreaded();
  }

  @Test
  public void readsThreadingSettings() {
    System.setProperty("SC_SINGLE_THREADED", "false");
    System.setProperty("SC_LOAD_MAX_THREADS", "3");
    System.setProperty("SC_LOAD_TIMEOUT_SECONDS", "42");

    assertThat(TaskRunners.isSingleThreaded(), is(false));

    final ThreadingOptions options = TaskRunners.getThreadingOptions();
    assertThat(options.maxThreads(), is(3));
    assertThat(options.timeoutSeconds(), is(42));

    assertTaskRunnerReturnsMultiThreaded();
  }

  @Test
  public void singleThreadingSettingsAreUsed() {
    System.setProperty("SC_SINGLE_THREADED", "true");
    System.setProperty("SC_LOAD_MAX_THREADS", "1");
    System.setProperty("SC_LOAD_TIMEOUT_SECONDS", "10");

    assertThat(TaskRunners.isSingleThreaded(), is(true));

    final ThreadingOptions options = TaskRunners.getThreadingOptions();

    assertThat(options.maxThreads(), is(1));
    assertThat(options.timeoutSeconds(), is(10));

    assertTaskRunnerReturnsSingleThreaded();
  }

  @AfterEach
  public void tearDown() {
    System.clearProperty("SC_SINGLE_THREADED");
    System.clearProperty("SC_LOAD_MAX_THREADS");
    System.clearProperty("SC_LOAD_TIMEOUT_SECONDS");
  }

  private void assertTaskRunnerReturnsMultiThreaded() {
    assertThat(TaskRunners.isSingleThreaded(), is(false));
    final TaskRunner runner = TaskRunners.getTaskRunner("test-multi");
    assertThat(runner, is(not(nullValue())));
    assertThat(runner, instanceOf(MultiThreadedTaskRunner.class));
  }

  private void assertTaskRunnerReturnsSingleThreaded() {
    assertThat(TaskRunners.isSingleThreaded(), is(true));
    final TaskRunner runner = TaskRunners.getTaskRunner("test-main");
    assertThat(runner, is(not(nullValue())));
    assertThat(runner, instanceOf(MainThreadTaskRunner.class));
  }
}
