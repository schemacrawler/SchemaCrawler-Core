/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.weakassociations;

import static java.util.Objects.requireNonNull;
import static schemacrawler.utility.MetaDataUtility.isPartial;

import java.io.Serial;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.ColumnReference;

/**
 * Proposed weak association between a foreign-key-like column and a primary key column.
 *
 * <p>Validation rejects:
 *
 * <ul>
 *   <li>Self-references to the same column.
 *   <li>Pairs where both columns are partial or either column has unknown data type.
 *   <li>Pairs with non-matching standard data types.
 * </ul>
 */
public final class ProposedWeakAssociation implements ColumnReference {

  @Serial private static final long serialVersionUID = 2986663326992262188L;

  private final Column primaryKeyColumn;
  private final Column foreignKeyColumn;

  ProposedWeakAssociation(final Column foreignKeyColumn, final Column primaryKeyColumn) {
    this.primaryKeyColumn = requireNonNull(primaryKeyColumn, "No primary key column provided");
    this.foreignKeyColumn = requireNonNull(foreignKeyColumn, "No foreign key column provided");
  }

  @Override
  public int compareTo(final ColumnReference o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Column getForeignKeyColumn() {
    return foreignKeyColumn;
  }

  @Override
  public int getKeySequence() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Column getPrimaryKeyColumn() {
    return primaryKeyColumn;
  }

  /**
   * Validates a proposed association based on identity, partiality, and standard data type
   * compatibility.
   *
   * @return true if the association should be considered for matching rules
   */
  public boolean isValid() {

    if (primaryKeyColumn.equals(foreignKeyColumn)) {
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
