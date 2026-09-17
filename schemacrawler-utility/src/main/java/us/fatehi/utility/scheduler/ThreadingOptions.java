/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.scheduler;

public record ThreadingOptions(int maxThreads, int timeoutSeconds) {

  private static final int MIN_TIMEOUT_SECONDS = 3600;
  private static final int MIN_THREADS = 1;
  private static final int MAX_THREADS = 10;

  public ThreadingOptions {
    maxThreads = Math.min(Math.max(maxThreads, MIN_THREADS), MAX_THREADS);
    timeoutSeconds = Math.max(timeoutSeconds, MIN_TIMEOUT_SECONDS);
  }
}
