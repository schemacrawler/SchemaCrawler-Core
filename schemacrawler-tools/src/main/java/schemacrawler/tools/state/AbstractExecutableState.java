/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.state;

import static java.util.Objects.requireNonNull;

import schemacrawler.ermodel.model.ERModel;
import schemacrawler.schema.Catalog;
import us.fatehi.utility.Nullable;
import us.fatehi.utility.datasource.DatabaseConnectionSource;

public abstract class AbstractExecutableState implements ExecutableState {

  private Catalog catalog;
  private ERModel erModel;
  private DatabaseConnectionSource connectionSource;

  @Override
  public final Catalog getCatalog() {
    return catalog;
  }

  @Override
  public final DatabaseConnectionSource getConnectionSource() {
    return connectionSource;
  }

  @Override
  public final ERModel getERModel() {
    return erModel;
  }

  @Override
  public final boolean hasCatalog() {
    return catalog != null;
  }

  @Override
  public final boolean hasConnectionSource() {
    return connectionSource != null;
  }

  @Override
  public final boolean hasERModel() {
    return erModel != null;
  }

  @Override
  public final void setCatalog(@Nullable final Catalog catalog) {
    this.catalog = catalog;
  }

  @Override
  public final void setConnectionSource(final DatabaseConnectionSource connectionSource) {
    this.connectionSource = requireNonNull(connectionSource, "No data source provided");
  }

  @Override
  public final void setERModel(@Nullable final ERModel erModel) {
    this.erModel = erModel;
  }

  protected final void clear() {
    catalog = null;
    connectionSource = null;
    erModel = null;
  }

  protected final void clearConnectionSource() {
    connectionSource = null;
  }
}
