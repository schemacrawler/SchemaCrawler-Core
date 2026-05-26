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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MainThreadTaskRunnerTest {

  @BeforeEach
  public void setUp() {
    System.setProperty("SC_SINGLE_THREADED", "true");
  }

  @AfterEach
  public void tearDown() {
    System.clearProperty("SC_SINGLE_THREADED");
  }

  @Test
  public void getTaskRunnerReturnsSingleThreaded() {
    final TaskRunner runner = TaskRunners.getTaskRunner("test-main", 4);
    assertThat(runner, is(not(nullValue())));
    assertThat(runner, instanceOf(MainThreadTaskRunner.class));
  }

  @Test
  public void mainThreadRunnerIsNotStopped() throws Exception {
    try (final TaskRunner runner = TaskRunners.getTaskRunner("test-main-run", 4)) {
      assertThat(runner.isStopped(), is(false));
    }
  }

  @Test
  public void mainThreadRunnerRunsTask() throws Exception {
    try (final TaskRunner runner = TaskRunners.getTaskRunner("test-main-task", 4)) {
      final boolean[] executed = {false};
      runner.add(new TaskDefinition("task1", () -> executed[0] = true));
      runner.submit();
      assertThat(executed[0], is(true));
    }
  }

  @Test
  public void mainThreadRunnerStop() throws Exception {
    try (final TaskRunner runner = TaskRunners.getTaskRunner("test-main-stop", 4)) {
      assertThat(runner.isStopped(), is(false));
      runner.stop();
      // After stop, isStopped should still return false for MainThreadTaskRunner (no-op stop)
      assertThat(runner.isStopped(), is(false));
    }
  }
}
