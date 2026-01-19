/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */
package schemacrawler.ermodel.model;

import static java.util.Objects.requireNonNull;

public enum RelationshipCardinality {

  /**
   * Cardinality is unknown or not specified.
   *
   * <p>Used when the relationship rules are not modeled or have not yet been determined.
   */
  unknown(""),
  zero_one("(0..1)"),
  zero_many("(0..many)"),
  one_one("(1..1)"),
  one_many("(1..many)"),
  many_many("(many..many)");

  public static RelationshipCardinality from(final ForeignKeyCardinality fkCardinality) {
    if (fkCardinality == null) {
      return unknown;
    }
    return switch (fkCardinality) {
      case one_one -> one_one;
      case one_many -> one_many;
      case zero_one -> zero_one;
      case zero_many -> zero_many;
      default -> unknown;
    };
  }

  private final String description;

  RelationshipCardinality(final String description) {
    this.description = requireNonNull(description, "No description provided");
  }

  @Override
  public String toString() {
    return description;
  }
}
