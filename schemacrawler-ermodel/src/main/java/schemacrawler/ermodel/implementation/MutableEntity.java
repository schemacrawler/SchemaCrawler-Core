/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.implementation;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.ermodel.model.Relationship;
import schemacrawler.schema.Table;

/** Conceptual entity backed by a SchemaCrawler table. */
class MutableEntity extends AbstractTableBacked implements Entity {

  @Serial private static final long serialVersionUID = 3946422106166202467L;

  private final EntityType entityType;
  private final Set<Relationship> relationships;

  public MutableEntity(final Table table, final EntityType entityType) {
    super(table);

    // No checks done about setting entity type for unknown or non entities
    this.entityType = requireNonNull(entityType, "No entity type provided");

    // No checks for partial table - exceptions will be thrown while calling
    // unsupported methods
    relationships = new TreeSet<>();
  }

  @Override
  public Collection<Relationship> getRelationships() {
    return List.copyOf(relationships);
  }

  @Override
  public EntityType getType() {
    return entityType;
  }

  void addRelationship(final Relationship relationship) {
    if (relationship != null) {
      relationships.add(relationship);
    }
  }
}
