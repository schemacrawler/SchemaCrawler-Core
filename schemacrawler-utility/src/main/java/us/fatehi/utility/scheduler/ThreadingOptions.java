/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.scheduler;

/**
 * The number of threads needs to be between 1 and 10. Using one thread is not the same as being
 * single-threaded - single threading is done using the main thread. A timeout value of 0 does not
 * time out tasks, but a non-zero positive value will time them out.
 */
public record ThreadingOptions(int maxThreads, int timeoutSeconds) {

  private static final int MIN_THREADS = 1;
  private static final int MAX_THREADS = 10;

  public ThreadingOptions {
    maxThreads = Math.min(Math.max(maxThreads, MIN_THREADS), MAX_THREADS);
    if (timeoutSeconds < 0) {
      timeoutSeconds = 0;
    }
  }
}
