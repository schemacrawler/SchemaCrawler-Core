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
import java.util.Collection;
import java.util.Optional;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.Identifiers;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Privilege;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableConstraint;
import schemacrawler.schema.TableConstraintColumn;
import schemacrawler.utility.MetaDataUtility;

final class LightTableConstraintColumn extends AbstractLightDatabaseObject
    implements TableConstraintColumn {

  @Serial private static final long serialVersionUID = 6302894541283541769L;

  private final Column column;
  private final int tableConstraintOrdinalPosition;
  private final Table parent;
  private final TableConstraint tableConstraint;

  LightTableConstraintColumn(
      final Column column,
      final int tableConstraintOrdinalPosition,
      final TableConstraint tableConstraint) {
    super(requireNonNull(column, "No column provided").getSchema(), column.getName());
    parent = column.getParent();
    this.tableConstraintOrdinalPosition = tableConstraintOrdinalPosition;
    this.column = column;
    this.tableConstraint = requireNonNull(tableConstraint, "No table constraint provided");
  }

  @Override
  public int compareTo(final NamedObject o) {
    return column.compareTo(o);
  }

  @Override
  public ColumnDataType getColumnDataType() {
    return column.getColumnDataType();
  }

  @Override
  public int getDecimalDigits() {
    return column.getDecimalDigits();
  }

  @Override
  public String getDefaultValue() {
    return column.getDefaultValue();
  }

  @Override
  public String getFullName() {
    return column.getFullName();
  }

  @Override
  public String getName() {
    return column.getName();
  }

  @Override
  public int getOrdinalPosition() {
    return column.getOrdinalPosition();
  }

  @Override
  public Table getParent() {
    return parent;
  }

  @Override
  public Collection<Privilege<Column>> getPrivileges() {
    return column.getPrivileges();
  }

  @Override
  public Column getReferencedColumn() {
    return column.getReferencedColumn();
  }

  @Override
  public String getRemarks() {
    return column.getRemarks();
  }

  @Override
  public Schema getSchema() {
    return column.getSchema();
  }

  @Override
  public String getShortName() {
    return parent.getName() + "." + getName();
  }

  @Override
  public int getSize() {
    return column.getSize();
  }

  @Override
  public TableConstraint getTableConstraint() {
    return tableConstraint;
  }

  @Override
  public int getTableConstraintOrdinalPosition() {
    return tableConstraintOrdinalPosition;
  }

  @Override
  public ColumnDataType getType() {
    return column.getType();
  }

  @Override
  public String getWidth() {
    return column.getWidth();
  }

  @Override
  public boolean hasDefaultValue() {
    return column.hasDefaultValue();
  }

  @Override
  public boolean isAutoIncremented() {
    return column.isAutoIncremented();
  }

  @Override
  public boolean isColumnDataTypeKnown() {
    return column.isColumnDataTypeKnown();
  }

  @Override
  public boolean isGenerated() {
    return column.isGenerated();
  }

  @Override
  public boolean isHidden() {
    return column.isHidden();
  }

  @Override
  public boolean isNullable() {
    return column.isNullable();
  }

  @Override
  public boolean isParentPartial() {
    return MetaDataUtility.isPartial(parent);
  }

  @Override
  public boolean isPartial() {
    return MetaDataUtility.isPartial(column);
  }

  @Override
  public boolean isPartOfForeignKey() {
    return column.isPartOfForeignKey();
  }

  @Override
  public boolean isPartOfIndex() {
    return column.isPartOfIndex();
  }

  @Override
  public boolean isPartOfPrimaryKey() {
    return column.isPartOfPrimaryKey();
  }

  @Override
  public boolean isPartOfSelfReferencingRelationship() {
    return column.isPartOfSelfReferencingRelationship();
  }

  @Override
  public boolean isPartOfUniqueIndex() {
    return column.isPartOfUniqueIndex();
  }

  @Override
  public boolean isSignificant() {
    return column.isSignificant();
  }

  @Override
  public NamedObjectKey key() {
    return column.key();
  }

  @Override
  public <P extends Privilege<Column>> Optional<P> lookupPrivilege(final String name) {
    return column.lookupPrivilege(name);
  }

  @Override
  public void withQuoting(final Identifiers identifiers) {
    column.withQuoting(identifiers);
  }
}
