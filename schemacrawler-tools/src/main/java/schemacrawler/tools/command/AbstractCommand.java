/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.command;

import static java.util.Objects.requireNonNull;

import java.sql.Connection;
import schemacrawler.tools.state.AbstractExecutionState;
import us.fatehi.utility.property.PropertyName;

/** A SchemaCrawler tools executable unit. */
public abstract class AbstractCommand<P extends CommandOptions> extends AbstractExecutionState
    implements BaseCommand<P> {

  protected final PropertyName command;
  protected P commandOptions;

  protected AbstractCommand(final PropertyName command) {
    this.command = requireNonNull(command, "No command specified");
  }

  @Override
  public void configure(final P commandOptions) {
    this.commandOptions = requireNonNull(commandOptions, "No command options provided");
  }

  @Override
  public final PropertyName getCommandName() {
    return command;
  }

  /** {@inheritDoc} */
  @Override
  public final P getCommandOptions() {
    return commandOptions;
  }

  public final Connection getConnection() {
    if (usesConnection() && hasConnectionSource()) {
      return getConnectionSource().get();
    }
    return null;
  }

  @Override
  public void initialize() {
    // Placeholder stub
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return command.toString();
  }
}
