/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.scheduler;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import us.fatehi.utility.string.StringFormat;

final class MultiThreadedTaskRunner extends AbstractTaskRunner {

  private static final Logger LOGGER = Logger.getLogger(MultiThreadedTaskRunner.class.getName());

  private final ExecutorService executorService;
  private final int timeoutSeconds;

  MultiThreadedTaskRunner(
      final String id, final int maxThreadsSuggested, final int timeoutSeconds) {
    super(id);

    this.timeoutSeconds = timeoutSeconds;
    final int maxThreads = Math.min(Math.max(maxThreadsSuggested, MIN_THREADS), MAX_THREADS);
    executorService = Executors.newFixedThreadPool(maxThreads);
    LOGGER.log(
        Level.INFO,
        new StringFormat(
            "Started thread pool <%s> for <%s> with <%d> threads",
            executorService, id, maxThreads));
  }

  @Override
  public boolean isStopped() {
    return executorService.isShutdown();
  }

  @Override
  public void stop() {
    try {
      executorService.shutdown();
      if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
        executorService.shutdownNow();
      }
    } catch (final InterruptedException ex) {
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  @Override
  Collection<TimedTaskResult> runTimed(final Collection<TaskDefinition> taskDefinitions)
      throws Exception {
    try {
      final List<TimedTask> timedTasks = new CopyOnWriteArrayList<>();
      for (final TaskDefinition taskDefinition : taskDefinitions) {
        final TimedTask timedTask = new TimedTask(taskDefinition, clock);
        timedTasks.add(timedTask);
      }

      final List<TimedTaskResult> runTaskResults = new CopyOnWriteArrayList<>();

      final List<Future<TimedTaskResult>> futureResults =
          timeoutSeconds <= 0
              ? executorService.invokeAll(timedTasks)
              : executorService.invokeAll(timedTasks, timeoutSeconds, TimeUnit.SECONDS);
      for (int i = 0; i < futureResults.size(); i++) {
        final Future<TimedTaskResult> futureResult = futureResults.get(i);
        if (!futureResult.isCancelled()) {
          final TimedTaskResult timedTaskResult = futureResult.get();
          runTaskResults.add(timedTaskResult);
        } else {
          final TimedTask cancelledTask = timedTasks.get(i);
          final String taskName = cancelledTask.getTaskName();
          final TimedTaskResult canceledTaskResult =
              new TimedTaskResult(
                  taskName,
                  Duration.ZERO,
                  new TaskTimeoutException(taskName, cancelledTask.getStart(), timeoutSeconds));
          runTaskResults.add(canceledTaskResult);
          LOGGER.log(
              Level.WARNING,
              new StringFormat(
                  "Task <%s> started at %s but was cancelled, possibly due to timeout",
                  taskName, cancelledTask.getStart()));
        }
      }

      return runTaskResults;
    } catch (final ExecutionException e) {
      final Throwable cause = e.getCause();
      if (cause instanceof final Exception exception) {
        throw exception;
      }
      throw new RunnerException(cause);
    }
  }
}
