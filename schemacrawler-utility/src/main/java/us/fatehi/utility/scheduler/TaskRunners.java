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
import us.fatehi.utility.UtilityMarker;
import us.fatehi.utility.readconfig.ReadConfig;
import us.fatehi.utility.readconfig.SystemConfigBuilder;

/** Creates task runners for database loading. */
@UtilityMarker
public class TaskRunners {

  private static final Logger LOGGER = Logger.getLogger(TaskRunners.class.getName());

  /** Controls single-threaded loading and connection-source selection. */
  private static final String SC_SINGLE_THREADED = "SC_SINGLE_THREADED";

  private static final String SC_LOAD_MAX_THREADS = "SC_LOAD_MAX_THREADS";
  private static final int DEFAULT_LOAD_MAX_THREADS = 5;

  private static final String SC_LOAD_TIMEOUT_SECONDS = "SC_LOAD_TIMEOUT_SECONDS";
  private static final int DEFAULT_LOAD_TIMEOUT_SECONDS = 3600;

  private static final ReadConfig CONFIG =
      SystemConfigBuilder.builder()
          .withSystemProperties()
          .withEnvironmentVariables()
          .withAlias(SC_SINGLE_THREADED, "SCHCRWLR_SINGLE_THREADED")
          .withAlias(SC_LOAD_MAX_THREADS, "SCHCRWLR_LOAD_MAX_THREADS")
          .withAlias(SC_LOAD_TIMEOUT_SECONDS, "SCHCRWLR_LOAD_TIMEOUT_SECONDS")
          .build();

  public static TaskRunner getTaskRunner(final String id) {
    if (isSingleThreaded()) {
      LOGGER.log(Level.CONFIG, "Loading database schema in the main thread");
      return new MainThreadTaskRunner(id);
    }
    LOGGER.log(Level.CONFIG, "Loading database schema using multiple threads");
    final ThreadingOptions threadingOptions = getThreadingOptions();
    return new MultiThreadedTaskRunner(id, threadingOptions);
  }

  public static boolean isSingleThreaded() {
    return CONFIG.getBooleanValue(SC_SINGLE_THREADED);
  }

  static ThreadingOptions getThreadingOptions() {
    final int maxThreads = CONFIG.getIntegerValue(SC_LOAD_MAX_THREADS, DEFAULT_LOAD_MAX_THREADS);
    final int timeoutSeconds =
        CONFIG.getIntegerValue(SC_LOAD_TIMEOUT_SECONDS, DEFAULT_LOAD_TIMEOUT_SECONDS);
    return new ThreadingOptions(maxThreads, timeoutSeconds);
  }

  private TaskRunners() {
    // Prevent instantiation
  }
}
