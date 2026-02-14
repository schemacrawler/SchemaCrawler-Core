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
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.utility.EntityModelUtility;
import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.exceptions.ExecutionRuntimeException;
import us.fatehi.utility.property.PropertyName;

/** A SchemaCrawler tools executable unit. */
public abstract class BaseCommand<C, R> implements Command<C, R> {

  protected final PropertyName command;
  protected C commandOptions;
  protected Catalog catalog;
  protected ERModel erModel;
  protected Connection connection;

  protected BaseCommand(final PropertyName command) {
    this.command = requireNonNull(command, "No command specified");
  }

  @Override
  public void configure(final C commandOptions) {
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

  @Override
  public final Connection getConnection() {
    return connection;
  }

  @Override
  public ERModel getERModel() {
    return erModel;
  }

  @Override
  public void initialize() {
    if (erModel == null) {
      erModel = EntityModelUtility.buildEmptyERModel();
    }
  }

  @Override
  public final void setCatalog(final Catalog catalog) {
    this.catalog = requireNonNull(catalog, "No catalog provided");
  }

  @Override
  public final void setConnection(final Connection connection) {
    if (!usesConnection()) {
      throw new ExecutionRuntimeException(
          "<%s> does not use a connection".formatted(command.getName()));
    }
    this.connection = connection;
  }

  @Override
  public void setERModel(final ERModel erModel) {
    this.erModel = requireNonNull(erModel, "No ER model provided");
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return command.toString();
  }
}
