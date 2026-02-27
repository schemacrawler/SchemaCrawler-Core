/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.executable;

import schemacrawler.schema.Catalog;
import us.fatehi.utility.datasource.DatabaseConnectionSource;
import us.fatehi.utility.property.PropertyName;

/** A SchemaCrawler executable unit. */
public interface Command<P extends CommandOptions, R> {

  /**
   * Executes command, after configuration and pre-checks. May throw runtime exceptions on errors.
   *
   * <p>R Return value
   */
  R call();

  void configure(P parameters);

  Catalog getCatalog();

  PropertyName getCommandName();

  P getCommandOptions();

  DatabaseConnectionSource getConnectionSource();

  /** Initializes the command for execution. */
  void initialize();

  void setCatalog(Catalog catalog);

  void setConnectionSource(DatabaseConnectionSource connectionSource);

  default boolean usesConnection() {
    return false;
  }
}
