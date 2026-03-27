/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.associations;

import static java.util.Objects.requireNonNull;

import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;

record ProposedAssociation(Column foreignKeyColumn, Column primaryKeyColumn)
    implements ColumnReference {

  ProposedAssociation {
    foreignKeyColumn = requireNonNull(foreignKeyColumn, "No foreign key column provided");
    primaryKeyColumn = requireNonNull(primaryKeyColumn, "No primary key column provided");
    if (foreignKeyColumn.equals(primaryKeyColumn)) {
      throw new IllegalArgumentException("Implicit associations cannot be self-referencing");
    }
  }

  @Override
  public int compareTo(final ColumnReference o) {
    throw new UnsupportedOperationException("Prevent adding to collection");
  }

  @Override
  public boolean equals(final Object obj) {
    throw new UnsupportedOperationException("Prevent adding to collection");
  }

  @Override
  public int hashCode() {
    throw new UnsupportedOperationException("Prevent adding to collection");
  }

  @Override
  public Column getForeignKeyColumn() {
    return foreignKeyColumn;
  }

  @Override
  public int getKeySequence() {
    return 1;
  }

  @Override
  public Column getPrimaryKeyColumn() {
    return primaryKeyColumn;
  }

  @Override
  public boolean isSelfReferencing() {
    return false;
  }

  @Override
  public String toString() {
    return foreignKeyColumn + " --> " + primaryKeyColumn;
  }
}
