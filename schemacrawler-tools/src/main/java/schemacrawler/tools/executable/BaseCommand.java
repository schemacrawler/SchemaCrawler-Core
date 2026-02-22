/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.executable;

import static java.util.Objects.requireNonNull;

import java.sql.Connection;
import schemacrawler.schema.Catalog;
import us.fatehi.utility.datasource.DatabaseConnectionSource;
import us.fatehi.utility.property.PropertyName;

/** A SchemaCrawler tools executable unit. */
public abstract class BaseCommand<P extends CommandOptions> implements Command<P> {

  private final PropertyName command;
  protected P commandOptions;
  protected Catalog catalog;
  private DatabaseConnectionSource dataSource;

  protected BaseCommand(final PropertyName command) {
    this.command = requireNonNull(command, "No command specified");
  }

  @Override
  public void configure(final P commandOptions) {
    this.commandOptions = requireNonNull(commandOptions, "No command options provided");
  }

  @Override
  public final Catalog getCatalog() {
    return catalog;
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
    if (usesConnection() && dataSource != null) {
      return dataSource.get();
    }
    return null;
  }

  @Override
  public final DatabaseConnectionSource getDataSource() {
    return dataSource;
  }

  @Override
  public void initialize() {
    // Placeholder stub
  }

  @Override
  public final void setCatalog(final Catalog catalog) {
    this.catalog = requireNonNull(catalog, "No catalog provided");
  }

  @Override
  public final void setDataSource(final DatabaseConnectionSource dataSource) {
    this.dataSource = dataSource;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return command.toString();
  }
}
