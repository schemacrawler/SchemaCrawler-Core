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

  private static final String SC_SINGLE_THREADED = "SC_SINGLE_THREADED";
  private static final String SC_LOAD_TIMEOUT_SECONDS = "SC_LOAD_TIMEOUT_SECONDS";
  private static final int DEFAULT_LOAD_TIMEOUT_SECONDS = 3600;

  public static TaskRunner getTaskRunner(final String id) {
    final SystemPropertiesConfig systemPropertiesConfig = new SystemPropertiesConfig();
    final boolean isSingleThreaded = systemPropertiesConfig.getBooleanValue(SC_SINGLE_THREADED);
    if (isSingleThreaded) {
      LOGGER.log(Level.CONFIG, "Loading database schema in the main thread");
      return new MainThreadTaskRunner(id);
    }
    LOGGER.log(Level.CONFIG, "Loading database schema using multiple threads");
    final int timeoutSeconds =
        systemPropertiesConfig.getIntegerValue(
            SC_LOAD_TIMEOUT_SECONDS, DEFAULT_LOAD_TIMEOUT_SECONDS);
    final int maxThreadsSuggested = 0;
    return new MultiThreadedTaskRunner(id, maxThreadsSuggested, timeoutSeconds);
  }
}
