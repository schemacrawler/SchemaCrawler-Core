/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.state;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableSet;

import java.util.Set;
import java.util.function.Predicate;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;
import us.fatehi.utility.Nullable;
import us.fatehi.utility.datasource.DatabaseConnectionSource;

public abstract class AbstractExecutionState implements ExecutionState {

  private Catalog catalog;
  private ERModel erModel;
  private DatabaseConnectionSource connectionSource;
  private Predicate<Table> tableVisibilityPredicate = table -> false;

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

  protected final Predicate<Table> getTableVisibilityPredicate() {
    return tableVisibilityPredicate;
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
    this.catalog = requireNonNull(catalog, "No catalog provided");
    tableVisibilityPredicate = buildTableVisibilityPredicate(catalog);
  }

  @Override
  public final void setConnectionSource(final DatabaseConnectionSource connectionSource) {
    this.connectionSource = requireNonNull(connectionSource, "No data source provided");
  }

  @Override
  public final void setERModel(@Nullable final ERModel erModel) {
    this.erModel = requireNonNull(erModel, "No ER model provided");
  }

  @Override
  public void transferState(final ExecutionState to) {
    final ExecutionState from = this;
    if (to == null) {
      return;
    }
    if (from.hasCatalog()) {
      to.setCatalog(from.getCatalog());
    }
    if (from.hasERModel()) {
      to.setERModel(from.getERModel());
    }
    if (to instanceof final DatabaseOperator command) {
      if (!command.usesConnection()) {
        return;
      }
    }
    if (from.hasConnectionSource()) {
      to.setConnectionSource(from.getConnectionSource());
    }
  }

  protected final void clear() {
    catalog = null;
    connectionSource = null;
    erModel = null;
    tableVisibilityPredicate = table -> false;
  }

  protected final void clearConnectionSource() {
    connectionSource = null;
  }

  private static Predicate<Table> buildTableVisibilityPredicate(final Catalog catalog) {
    final Set<NamedObjectKey> visibleTableKeys =
        catalog.getTables().stream().map(Table::key).collect(toUnmodifiableSet());
    return table -> table != null && visibleTableKeys.contains(table.key());
  }
}
