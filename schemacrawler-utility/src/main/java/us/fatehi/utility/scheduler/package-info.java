/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

/**
 * Task scheduling utilities for database metadata loading.
 *
 * <p>{@link us.fatehi.utility.scheduler.TaskRunners} selects a main-thread or multi-threaded task
 * runner. Configure the selection with environment variables or Java system properties. System
 * properties take precedence over environment variables when both are set:
 *
 * <ul>
 *   <li>{@code SC_SINGLE_THREADED=true} runs tasks on the main thread.
 *   <li>{@code SC_LOAD_MAX_THREADS} sets the multi-threaded runner's thread count, from 1 through
 *       10. The default is 5.
 *   <li>{@code SC_LOAD_TIMEOUT_SECONDS} sets the timeout for each batch of tasks, in seconds. The
 *       default is 3600 seconds. A value of 0 or less disables the timeout.
 * </ul>
 *
 * <p>For Java system properties, pass the same names with {@code -D}, such as {@code
 * -DSC_SINGLE_THREADED=true}. These settings apply to each batch of tasks, not to the overall
 * database metadata load.
 *
 * <p>{@link us.fatehi.utility.scheduler.MultiThreadedTaskRunner} throws a {@link
 * us.fatehi.utility.scheduler.TaskTimeoutException} when a task exceeds the configured positive
 * timeout.
 */
package us.fatehi.utility.scheduler;
