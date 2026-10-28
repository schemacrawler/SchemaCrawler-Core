/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static java.util.Comparator.comparingInt;
import static java.util.Objects.requireNonNull;

import java.io.Serial;
import java.util.Comparator;
import java.util.Objects;
import schemacrawler.namedobject.NamedObjectComparators;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;

/** Represents a single column mapping from a primary key column to a foreign key column. */
final class ImmutableColumnReference implements ColumnReference {

  @Serial private static final long serialVersionUID = -4411771492159843382L;

  /** Compares mapping sequence, then normalized foreign and primary column names; nulls first. */
  private static final Comparator<ColumnReference> COLUMN_REFERENCE_COMPARATOR =
      comparingInt(ColumnReference::getKeySequence)
          .thenComparing(
              ColumnReference::getForeignKeyColumn,
              NamedObjectComparators.NORMALIZED_NAME_COMPARATOR)
          .thenComparing(
              ColumnReference::getPrimaryKeyColumn,
              NamedObjectComparators.NORMALIZED_NAME_COMPARATOR);

  private final Column foreignKeyColumn;
  private final Column primaryKeyColumn;
  private final int keySequence;
  private final boolean isSelfReferencing;

  protected ImmutableColumnReference(
      final int keySequence, final Column foreignKeyColumn, final Column primaryKeyColumn) {
    this.keySequence = keySequence;
    this.foreignKeyColumn = requireNonNull(foreignKeyColumn, "No foreign key column provided");
    this.primaryKeyColumn = requireNonNull(primaryKeyColumn, "No primary key column provided");
    isSelfReferencing = foreignKeyColumn.equals(primaryKeyColumn);
  }

  /**
   * {@inheritDoc}
   *
   * <p>NOTE: compareTo is not compatible with equals. equals compares the full name of a database
   * object, but compareTo uses more fields to define a "natural" sorting order.
   */
  @Override
  public int compareTo(final ColumnReference columnRef) {
    if (this == columnRef) {
      return 0;
    }
    if (columnRef == null) {
      return 1;
    }
    return COLUMN_REFERENCE_COMPARATOR.compare(this, columnRef);
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
    return keySequence;
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
    return isSelfReferencing;
  }

  @Override
  public String toString() {
    return foreignKeyColumn + " --> " + primaryKeyColumn;
  }
}
