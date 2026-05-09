/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import schemacrawler.schema.Column;
import schemacrawler.schema.PrimaryKey;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableConstraintColumn;
import schemacrawler.schema.TableConstraintType;

public final class LightPrimaryKey extends AbstractLightDatabaseObject implements PrimaryKey {

  @Serial private static final long serialVersionUID = -8327896738506432571L;

  private final Table parent;
  private final List<TableConstraintColumn> tableConstraintColumns;

  public LightPrimaryKey(final Column... columns) {
    super(columns[0].getSchema(), "PRIMARY_KEY");
    parent = columns[0].getParent();
    tableConstraintColumns = new ArrayList<>();
    for (final Column column : columns) {
      tableConstraintColumns.add(new LightTableConstraintColumn(column, 1 + 1, this));
    }
  }

  @Override
  public List<TableConstraintColumn> getConstrainedColumns() {
    return tableConstraintColumns;
  }

  @Override
  public String getDefinition() {
    return null;
  }

  @Override
  public Table getParent() {
    return parent;
  }

  @Override
  public String getShortName() {
    return parent.getName() + ".PRIMARY_KEY";
  }

  @Override
  public TableConstraintType getType() {
    return TableConstraintType.primary_key;
  }

  @Override
  public boolean hasDefinition() {
    return false;
  }

  @Override
  public boolean isDeferrable() {
    return false;
  }

  @Override
  public boolean isInitiallyDeferred() {
    return false;
  }

  @Override
  public boolean isParentPartial() {
    return false;
  }
}
