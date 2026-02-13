/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.associations;

import static java.util.Objects.requireNonNull;
import static schemacrawler.utility.MetaDataUtility.isPartial;

import java.io.Serial;
import java.util.Objects;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.ColumnReference;

/**
 * Implicit association between a foreign-key-like column and a primary key column.
 *
 * <p>Validation rejects:
 *
 * <ul>
 *   <li>Self-references to the same column.
 *   <li>Pairs where both columns are partial or either column has unknown data type.
 *   <li>Pairs with non-matching standard data types.
 * </ul>
 */
public final class ImplicitColumnReference implements ColumnReference {

  @Serial private static final long serialVersionUID = 2986663326992262188L;

  private final Column foreignKeyColumn;
  private final Column primaryKeyColumn;

  public ImplicitColumnReference(final Column foreignKeyColumn, final Column primaryKeyColumn) {
    this.foreignKeyColumn = requireNonNull(foreignKeyColumn, "No foreign key column provided");
    this.primaryKeyColumn = requireNonNull(primaryKeyColumn, "No primary key column provided");
  }

  /**
   * {@inheritDoc}
   *
   * <p>NOTE: compareTo is not compatible with equals. equals compares the full name of a database
   * object, but compareTo uses more fields to define a "natural" sorting order.
   */
  @Override
  public int compareTo(final ColumnReference columnRef) {

    if (columnRef == null) {
      return -1;
    }

    int compare = 0;

    final ColumnReference other = columnRef;
    if (compare == 0) {
      compare = getKeySequence() - other.getKeySequence();
    }
    if (compare == 0) {
      compare =
          foreignKeyColumn.getFullName().compareTo(columnRef.getForeignKeyColumn().getFullName());
    }
    if (compare == 0) {
      compare =
          primaryKeyColumn.getFullName().compareTo(columnRef.getPrimaryKeyColumn().getFullName());
    }
    return compare;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || !(obj instanceof final ColumnReference other)) {
      return false;
    }
    return Objects.equals(primaryKeyColumn, other.getPrimaryKeyColumn())
        && Objects.equals(foreignKeyColumn, other.getForeignKeyColumn());
  }

  /** {@inheritDoc} */
  @Override
  public Column getForeignKeyColumn() {
    return foreignKeyColumn;
  }

  /** {@inheritDoc} */
  @Override
  public int getKeySequence() {
    return 1;
  }

  /** {@inheritDoc} */
  @Override
  public Column getPrimaryKeyColumn() {
    return primaryKeyColumn;
  }

  @Override
  public int hashCode() {
    return Objects.hash(foreignKeyColumn, primaryKeyColumn);
  }

  @Override
  public boolean isSelfReferencing() {
    return false;
  }

  /**
   * Validates a proposed association based on identity, partiality, and standard data type
   * compatibility.
   *
   * @return true if the association should be considered for matching rules
   */
  public boolean isValid() {

    if (primaryKeyColumn.equals(foreignKeyColumn) || foreignKeyColumn.isPartOfForeignKey()) {
      return false;
    }

    final boolean isPkColumnPartial = isPartial(primaryKeyColumn);
    final boolean isFkColumnPartial = isPartial(foreignKeyColumn);
    if (isFkColumnPartial && isPkColumnPartial
        || !primaryKeyColumn.isColumnDataTypeKnown()
        || !foreignKeyColumn.isColumnDataTypeKnown()) {
      return false;
    }

    final ColumnDataType fkColumnType = foreignKeyColumn.getColumnDataType();
    final ColumnDataType pkColumnType = primaryKeyColumn.getColumnDataType();
    final boolean isValid =
        fkColumnType.getStandardTypeName().equals(pkColumnType.getStandardTypeName());
    return isValid;
  }

  @Override
  public String toString() {
    return foreignKeyColumn + " ~~> " + primaryKeyColumn;
  }
}
