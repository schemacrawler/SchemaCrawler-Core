/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.command;

import schemacrawler.tools.state.ExecutionState;
import us.fatehi.utility.property.PropertyName;

/** A SchemaCrawler executable unit. */
public interface BaseCommand<P extends CommandOptions> extends ExecutionState {

  void configure(P parameters);

  PropertyName getCommandName();

  P getCommandOptions();

  /** Initializes the command for execution. */
  void initialize();

  default boolean usesConnection() {
    return false;
  }
}
