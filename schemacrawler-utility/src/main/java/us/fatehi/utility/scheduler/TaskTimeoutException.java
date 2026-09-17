/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.scheduler;

import java.time.ZonedDateTime;

public class TaskTimeoutException extends RunnerException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates an exception describing a task cancelled after exceeding its configured timeout.
   *
   * @param taskName the cancelled task name
   * @param start the task start time, or null if the task had not started
   * @param timeoutSeconds the configured timeout in seconds
   */
  public TaskTimeoutException(
      final String taskName, final ZonedDateTime start, final int timeoutSeconds) {
    super(
        "Task <%s> started at <%s> was cancelled after exceeding the configured timeout of <%d> seconds"
            .formatted(taskName, start, timeoutSeconds));
  }
}
