/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.scheduler;

import java.util.logging.Level;
import java.util.logging.Logger;
import us.fatehi.utility.readconfig.SystemConfig;

/**
 * Creates task runners for database loading.
 *
 * <p>Configure task execution with environment variables or Java system properties using the names
 * below. A system property takes precedence when both are set. For example, you can set:
 *
 * <pre>
 * SC_SINGLE_THREADED=true
 * <pre>
 *
 * or
 *
 * <pre>
 * SC_LOAD_MAX_THREADS=8
 * SC_LOAD_TIMEOUT_SECONDS=600
 * </pre>
 *
 * <p>For Java system properties, pass the same values with {@code -D}, for example {@code
 * -DSC_SINGLE_THREADED=true}.
 *
 * {@code SC_SINGLE_THREADED} takes precedence, and uses the main thread for processing.
 *
 * The default is multi-threaded loading with up to 5 threads and a
 * 3600-second timeout. Set {@code SC_LOAD_TIMEOUT_SECONDS} to {@code 0} or a negative value to
 * disable the timeout. These settings are per batch of tasks, and not for the overall load
 * of the database metadata.
 */
public class TaskRunners {

  private static final Logger LOGGER = Logger.getLogger(TaskRunners.class.getName());

  /** Set to {@code true} to run tasks on the main thread. */
  private static final String SC_SINGLE_THREADED = "SC_SINGLE_THREADED";

  /**
   * Sets the maximum number of threads used to load the database catalog. The default is 5 threads,
   * and the maximum is 10.
   */
  private static final String SC_LOAD_MAX_THREADS = "SC_LOAD_MAX_THREADS";

  private static final int DEFAULT_LOAD_MAX_THREADS = 5;

  /**
   * Sets the per-batch task timeout in seconds. The default is 3600 seconds. A value of 0 or less
   * disables the timeout.
   */
  private static final String SC_LOAD_TIMEOUT_SECONDS = "SC_LOAD_TIMEOUT_SECONDS";

  private static final int DEFAULT_LOAD_TIMEOUT_SECONDS = 3600;

  public static TaskRunner getTaskRunner(final String id) {
    final boolean isSingleThreaded = new SystemConfig().getBooleanValue(SC_SINGLE_THREADED);
    if (isSingleThreaded) {
      LOGGER.log(Level.CONFIG, "Loading database schema in the main thread");
      return new MainThreadTaskRunner(id);
    }
    LOGGER.log(Level.CONFIG, "Loading database schema using multiple threads");
    final ThreadingOptions threadingOptions = getThreadingOptions();
    return new MultiThreadedTaskRunner(id, threadingOptions);
  }

  private static ThreadingOptions getThreadingOptions() {
    final int maxThreads =
        new SystemConfig().getIntegerValue(SC_LOAD_MAX_THREADS, DEFAULT_LOAD_MAX_THREADS);
    final int timeoutSeconds =
        new SystemConfig().getIntegerValue(SC_LOAD_TIMEOUT_SECONDS, DEFAULT_LOAD_TIMEOUT_SECONDS);
    return new ThreadingOptions(maxThreads, timeoutSeconds);
  }
}
