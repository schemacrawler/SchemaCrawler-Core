/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */
package schemacrawler.schema;

import static java.util.Objects.requireNonNull;

/**
 * Describes the cardinality of a foreign-key relationship in terms of how many target rows
 * may or must be referenced from the source row.
 *
 * <p>The notation <code>(min..max)</code> follows common ER/UML conventions:</p>
 *
 * <ul>
 *   <li><b>min = 0</b> → optional relationship (foreign key may be <code>NULL</code>)</li>
 *   <li><b>min = 1</b> → mandatory relationship (foreign key must be <code>NOT NULL</code>)</li>
 *   <li><b>max = 1</b> → at most one target row</li>
 *   <li><b>max = many</b> → any number of target rows</li>
 * </ul>
 *
 * <p>
 * Note: Standard relational schemas express optional vs. mandatory relationships primarily
 * through <code>NULL</code>/<code>NOT NULL</code> constraints on the foreign-key column.
 * The minimum cardinality on the “many” side (for example, <code>1..many</code>)
 * is usually enforced by application logic rather than pure DDL.
 * </p>
 */
public enum ForeignKeyCardinality {

  /**
   * Cardinality is unknown or not specified.
   *
   * <p>Used when the relationship rules are not modeled or have not yet been determined.</p>
   */
  unknown(""),

  /**
   * Zero-or-one related row: <code>(0..1)</code>.
   *
   * <p>
   * Optional relationship. The foreign-key column may be <code>NULL</code>, meaning
   * “no related row”, or may reference exactly one existing target row.
   * </p>
   *
   * <p>Example schema:</p>
   *
   * <pre>
   * CUSTOMER(
   *   Id PK,
   *   ...
   * )
   *
   * ORDER(
   *   Id PK,
   *   CustomerId,
   *   FK NULL REFERENCES CUSTOMER(Id)
   * )
   * </pre>
   *
   * <p>Here, an <code>ORDER</code> may or may not be linked to a <code>CUSTOMER</code>
   * (<code>CustomerId</code> is nullable).</p>
   */
  zero_one("(0..1)"),

  /**
   * Zero-or-many related rows: <code>(0..many)</code>.
   *
   * <p>
   * Optional one-to-many or many-to-many relationship. A given target row may be
   * referenced by any number of source rows, including none at all.
   * </p>
   *
   * <p>Example schema:</p>
   *
   * <pre>
   * CUSTOMER(
   *   Id PK,
   *   ...
   * )
   *
   * ORDER(
   *   Id PK,
   *   CustomerId,
   *   FK NULL REFERENCES CUSTOMER(Id)
   * )
   * </pre>
   *
   * <p>
   * A <code>CUSTOMER</code> can have zero, one, or many <code>ORDER</code> records.
   * An <code>ORDER</code> may exist without a <code>CUSTOMER</code>
   * if <code>CustomerId</code> is <code>NULL</code>.
   * </p>
   */
  zero_many("(0..many)"),

  /**
   * Exactly one related row: <code>(1..1)</code>.
   *
   * <p>
   * Mandatory single relationship from the source row's perspective:
   * the foreign-key column must always reference exactly one existing target row,
   * and must never be <code>NULL</code>.
   * </p>
   *
   * <p>Example schema:</p>
   *
   * <pre>
   * CUSTOMER(
   *   Id PK,
   *   ...
   * )
   *
   * CUSTOMER_PROFILE(
   *   Id PK,
   *   CustomerId FK NOT NULL UNIQUE REFERENCES CUSTOMER(Id)
   * )
   * </pre>
   *
   * <p>
   * Each <code>CUSTOMER_PROFILE</code> must be linked to exactly one
   * <code>CUSTOMER</code>. The <code>UNIQUE</code> constraint ensures that no
   * two profiles point to the same customer.
   * </p>
   */
  one_one("(1..1)"),

  /**
   * One-or-many related rows: <code>(1..many)</code>.
   *
   * <p>
   * From the “one” side, each target row must have at least one referencing row
   * on the “many” side. From the “many” side, each source row must reference
   * exactly one target row.
   * </p>
   *
   * <p>Example schema:</p>
   *
   * <pre>
   * DEPARTMENT(
   *   Id PK,
   *   ...
   * )
   *
   * EMPLOYEE(
   *   Id PK,
   *   DepartmentId FK NOT NULL REFERENCES DEPARTMENT(Id)
   * )
   * </pre>
   *
   * <p>
   * Each <code>EMPLOYEE</code> must belong to exactly one <code>DEPARTMENT</code>
   * (<code>DepartmentId</code> is <code>NOT NULL</code>).
   * However, typical databases do not enforce that every department
   * has at least one employee — such constraints are usually implemented
   * by application logic or triggers.
   * </p>
   */
  one_many("(1..many)");

  private final String description;

  ForeignKeyCardinality(final String description) {
    this.description = requireNonNull(description, "No description provided");
  }

  @Override
  public String toString() {
    return description;
  }
}
