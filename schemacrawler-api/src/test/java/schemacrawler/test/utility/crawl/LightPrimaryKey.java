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

  private static Column firstNonNull(final Column... columns) {
    if (columns == null || columns.length == 0) {
      throw new IllegalArgumentException("No columns provided");
    }
    for (final Column column : columns) {
      if (column != null) {
        return column;
      }
    }
    throw new IllegalArgumentException("No columns provided");
  }

  private final Table parent;

  private final List<TableConstraintColumn> tableConstraintColumns;

  public LightPrimaryKey(final Column... columns) {
    super(firstNonNull(columns).getSchema(), "PRIMARY_KEY");
    parent = firstNonNull(columns).getParent();
    tableConstraintColumns = new ArrayList<>();
    for (int i = 0; i < columns.length; i++) {
      final Column column = columns[i];
      if (column == null) {
        // Increment index, and continue
        continue;
      }
      tableConstraintColumns.add(new LightTableConstraintColumn(column, i + 1, this));
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
