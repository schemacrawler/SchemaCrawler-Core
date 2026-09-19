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
import us.fatehi.utility.readconfig.SystemPropertiesConfig;

public class TaskRunners {

  private static final Logger LOGGER = Logger.getLogger(TaskRunners.class.getName());

  /**
   * Set to {@code true} to run tasks on the main thread. This setting may be provided as an
   * environment variable or a Java system property.
   */
  private static final String SC_SINGLE_THREADED = "SC_SINGLE_THREADED";

  /**
   * Sets the maximum number of threads used to load the database catalog. This setting may be
   * provided as an environment variable or a Java system property. The default is 5 threads, and
   * the maximum is 10.
   */
  private static final String SC_LOAD_MAX_THREADS = "SC_LOAD_MAX_THREADS";

  private static final int DEFAULT_LOAD_MAX_THREADS = 5;

  /**
   * Sets the per-batch task timeout in seconds. This setting may be provided as an environment
   * variable or a Java system property. The default is 3600 seconds but a value of 0 or less
   * disables the timeout.
   */
  private static final String SC_LOAD_TIMEOUT_SECONDS = "SC_LOAD_TIMEOUT_SECONDS";

  private static final int DEFAULT_LOAD_TIMEOUT_SECONDS = 3600;

  public static TaskRunner getTaskRunner(final String id) {
    final boolean isSingleThreaded =
        new SystemPropertiesConfig().getBooleanValue(SC_SINGLE_THREADED);
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
        new SystemPropertiesConfig().getIntegerValue(SC_LOAD_MAX_THREADS, DEFAULT_LOAD_MAX_THREADS);
    final int timeoutSeconds =
        new SystemPropertiesConfig()
            .getIntegerValue(SC_LOAD_TIMEOUT_SECONDS, DEFAULT_LOAD_TIMEOUT_SECONDS);
    return new ThreadingOptions(maxThreads, timeoutSeconds);
  }
}
