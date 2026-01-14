/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schema;

import java.util.List;
import us.fatehi.utility.OptionalBoolean;

/** Represents a foreign-key mapping to a primary key in another table. */
public interface TableReference
    extends NamedObject,
        AttributedObject,
        DescribedObject,
        TableConstraint,
        Iterable<ColumnReference> {

  /**
   * Gets the list of column pairs.
   *
   * @return Column pairs
   */
  List<ColumnReference> getColumnReferences();

  /**
   * Gets dependent or child table for this reference.
   *
   * @return Dependent table for this reference.
   */
  default Table getDependentTable() {
    return getForeignKeyTable();
  }

  /**
   * Gets the foreign key cardinality if computed, otherwise returns unknown.
   *
   * @return Foreign key cardinality
   */
  ForeignKeyCardinality getForeignKeyCardinality();

  /**
   * Gets the dependent table with an imported foreign key.
   *
   * @return Dependent table.
   */
  Table getForeignKeyTable();

  /**
   * Gets the referenced table.
   *
   * @return Referenced table.
   */
  Table getPrimaryKeyTable();

  /**
   * Gets referenced or parent table for this reference.
   *
   * @return Referenced table for this reference.
   */
  default Table getReferencedTable() {
    return getPrimaryKeyTable();
  }

  /**
   * If the foreign key is covered by an index. Returns `OptionalBoolean.true_value` if the foreign
   * key is covered by an index, `OptionalBoolean.false_value` if not, `OptionalBoolean.unkown` if
   * the metadata is not available.
   *
   * @return OptionalBoolean
   */
  OptionalBoolean hasIndex();

  /**
   * If the foreign key is unique. Returns `OptionalBoolean.true_value` if the foreign key is
   * unique, `OptionalBoolean.false_value` if not, `OptionalBoolean.unkown` if the metadata is not
   * available.
   *
   * @return OptionalBoolean
   */
  OptionalBoolean hasUniqueIndex();

  /**
   * If the table reference is nullable, or optional.
   *
   * @return True if the table reference is optional
   */
  boolean isOptional();

  /**
   * If the table references itself.
   *
   * @return True if the table is self-referencing.
   */
  boolean isSelfReferencing();
}
