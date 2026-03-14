/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import java.util.Iterator;
import java.util.List;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.ForeignKeyDeferrability;
import schemacrawler.schema.ForeignKeyUpdateRule;
import schemacrawler.schema.Identifiers;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableConstraintColumn;
import schemacrawler.schema.TableConstraintType;

public final class LightForeignKey extends AbstractLightDatabaseObject implements ForeignKey {

  @Serial private static final long serialVersionUID = -5359990477303202179L;

  private final Table fkTable;
  private final Table pkTable;
  private final ColumnReference columnReference;

  public LightForeignKey(final String name, final Column fkColumn, final Column pkColumn) {
    super(requireNonNull(fkColumn, "No foreign key column provided").getSchema(), name);
    fkTable = fkColumn.getParent();
    pkTable = pkColumn.getParent();
    columnReference = new LightColumnReference(fkColumn, pkColumn);
  }

  public LightForeignKey(final String name, final Table fkTable, final Table pkTable) {
    super(fkTable.getSchema(), name);
    this.fkTable = fkTable;
    this.pkTable = pkTable;
    columnReference = null;
  }

  @Override
  public int compareTo(final NamedObject o) {
    return getFullName().compareTo(o.getFullName());
  }

  @Override
  public List<ColumnReference> getColumnReferences() {
    if (columnReference == null) {
      return List.of();
    }
    return List.of(columnReference);
  }

  @Override
  public List<TableConstraintColumn> getConstrainedColumns() {
    return List.of();
  }

  @Override
  public ForeignKeyDeferrability getDeferrability() {
    return ForeignKeyDeferrability.unknown;
  }

  @Override
  public String getDefinition() {
    return null;
  }

  @Override
  public ForeignKeyUpdateRule getDeleteRule() {
    return ForeignKeyUpdateRule.unknown;
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
  public ForeignKeyUpdateRule getUpdateRule() {
    return ForeignKeyUpdateRule.unknown;
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
