/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import java.io.Serial;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import schemacrawler.namedobject.NamedObjectComparators;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.KeyColumn;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Privilege;
import schemacrawler.schema.Table;
import schemacrawler.utility.MetaDataUtility;

abstract sealed class AbstractKeyColumn extends AbstractDependantObject<Table> implements KeyColumn
    permits MutableIndexColumn, MutableTableConstraintColumn {

  @Serial private static final long serialVersionUID = 6988029161945610279L;

  private static final Comparator<AbstractKeyColumn> KEY_COLUMN_COMPARATOR =
      Comparator.comparingInt((AbstractKeyColumn column) -> column.keyOrdinalPosition)
          .thenComparing(NamedObjectComparators.TYPED_OBJECT_COMPARATOR)
          .thenComparing(NamedObjectComparators.NAME_THEN_KEY_COMPARATOR);

  private final Column column;
  private final boolean isPartial;
  private int keyOrdinalPosition;

  AbstractKeyColumn(final Column column) {
    super(new TablePointer(column.getParent()), column.getName());
    this.column = column;
    isPartial = MetaDataUtility.isPartial(column);
  }

  /**
   * {@inheritDoc}
   *
   * <p>NOTE: compareTo is not compatible with equals. equals compares the full name of a database
   * object, but compareTo uses more fields to define a "natural" sorting order. compareTo may
   * return incorrect results until the object is fully built by SchemaCrawler.
   */
  @Override
  public int compareTo(final NamedObject obj) {
    if (obj == null) {
      return -1;
    }

    final int databaseObjectTypeComparison =
        NamedObjectComparators.DATABASE_OBJECT_SIMPLE_TYPE_COMPARATOR.compare(this, obj);
    if (databaseObjectTypeComparison != 0) {
      return databaseObjectTypeComparison;
    }

    if (obj instanceof final AbstractKeyColumn other) {
      return KEY_COLUMN_COMPARATOR.compare(this, other);
    }

    return super.compareTo(obj);
  }

  /** {@inheritDoc} */
  @Override
  public ColumnDataType getColumnDataType() {
    return column.getColumnDataType();
  }

  /** {@inheritDoc} */
  @Override
  public int getDecimalDigits() {
    return column.getDecimalDigits();
  }

  /** {@inheritDoc} */
  @Override
  public String getDefaultValue() {
    return column.getDefaultValue();
  }

  /** {@inheritDoc} */
  @Override
  public int getOrdinalPosition() {
    return column.getOrdinalPosition();
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Privilege<Column>> getPrivileges() {
    return column.getPrivileges();
  }

  /** {@inheritDoc} */
  @Override
  public Column getReferencedColumn() {
    return column.getReferencedColumn();
  }

  /** {@inheritDoc} */
  @Override
  public int getSize() {
    return column.getSize();
  }

  /** {@inheritDoc} */
  @Override
  public ColumnDataType getType() {
    return column.getType();
  }

  /** {@inheritDoc} */
  @Override
  public String getWidth() {
    return column.getWidth();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isAutoIncremented() {
    return column.isAutoIncremented();
  }

  @Override
  public boolean isColumnDataTypeKnown() {
    return column.isColumnDataTypeKnown();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isGenerated() {
    return column.isGenerated();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isHidden() {
    return column.isHidden();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isNullable() {
    return column.isNullable();
  }

  @Override
  public final boolean isPartial() {
    return isPartial;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isPartOfForeignKey() {
    return column.isPartOfForeignKey();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isPartOfIndex() {
    return column.isPartOfIndex();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isPartOfPrimaryKey() {
    return column.isPartOfPrimaryKey();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isPartOfSelfReferencingRelationship() {
    return column.isPartOfSelfReferencingRelationship();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isPartOfUniqueIndex() {
    return column.isPartOfUniqueIndex();
  }

  /** {@inheritDoc} */
  @Override
  public Optional<? extends Privilege<Column>> lookupPrivilege(final String name) {
    return column.lookupPrivilege(name);
  }

  int getKeyOrdinalPosition() {
    return keyOrdinalPosition;
  }

  void setGenerated(final boolean isGenerated) {
    if (column instanceof final MutableColumn mutableColumn) {
      mutableColumn.setGenerated(isGenerated);
    }
  }

  void setKeyOrdinalPosition(final int keyOrdinalPosition) {
    this.keyOrdinalPosition = keyOrdinalPosition;
  }
}
