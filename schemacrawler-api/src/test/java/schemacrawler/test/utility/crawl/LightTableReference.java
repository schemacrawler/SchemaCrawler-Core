/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import java.io.Serial;
import java.util.Iterator;
import java.util.List;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.Identifiers;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableConstraintColumn;
import schemacrawler.schema.TableConstraintType;
import schemacrawler.schema.TableReference;

public final class LightTableReference extends AbstractLightDatabaseObject
    implements TableReference {

  @Serial private static final long serialVersionUID = -5359990477303202179L;

  private final Table fkTable;
  private final Table pkTable;

  public LightTableReference(final String name, final Table fkTable, final Table pkTable) {
    super(fkTable.getSchema(), name);
    this.fkTable = fkTable;
    this.pkTable = pkTable;
  }

  @Override
  public int compareTo(final NamedObject o) {
    return getFullName().compareTo(o.getFullName());
  }

  @Override
  public List<ColumnReference> getColumnReferences() {
    return List.of();
  }

  @Override
  public List<TableConstraintColumn> getConstrainedColumns() {
    return List.of();
  }

  @Override
  public String getDefinition() {
    return null;
  }

  @Override
  public Table getForeignKeyTable() {
    return fkTable;
  }

  @Override
  public Table getParent() {
    return fkTable;
  }

  @Override
  public Table getPrimaryKeyTable() {
    return pkTable;
  }

  @Override
  public Schema getSchema() {
    return null;
  }

  @Override
  public String getShortName() {
    return getName();
  }

  @Override
  public TableConstraintType getType() {
    return TableConstraintType.foreign_key;
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
  public boolean isOptional() {
    return false;
  }

  @Override
  public boolean isParentPartial() {
    return false;
  }

  @Override
  public boolean isSelfReferencing() {
    return fkTable.equals(pkTable);
  }

  @Override
  public Iterator<ColumnReference> iterator() {
    return getColumnReferences().iterator();
  }

  @Override
  public void withQuoting(final Identifiers identifiers) {}
}
