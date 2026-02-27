/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.executable;

/** A SchemaCrawler executable unit. */
public interface CallableCommand<P extends CommandOptions, R> extends BaseCommand<P> {

  /**
   * Executes command, after configuration and pre-checks. May throw runtime exceptions on errors.
   *
   * <p>R Return value
   */
  R call();
}
