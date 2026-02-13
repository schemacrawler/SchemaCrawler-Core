/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableReference;

/** Entity-relationship model. */
public interface ERModel extends Serializable {

  /**
   * Gets all modeled entities.
   *
   * @return All modeled entities
   */
  Collection<Entity> getEntities();

  /**
   * Gets modeled entities of a given type.
   *
   * @param entityType Entity type
   * @return Modeled entities of a given type
   */
  Collection<Entity> getEntitiesByType(EntityType entityType);

  /**
   * Gets implicit relationships inferred from the catalog.
   *
   * @return All implicit relationships
   */
  Collection<Relationship> getImplicitRelationships();

  /**
   * Gets all modeled relationships.
   *
   * @return All modeled relationships
   */
  Collection<Relationship> getRelationships();

  /**
   * Gets modeled relationships of a given cardinality.
   *
   * @param cardinality Relationship cardinality
   * @return Modeled relationships of a given cardinality
   */
  Collection<Relationship> getRelationshipsByType(RelationshipCardinality cardinality);

  /**
   * Gets all subtypes of a given supertype.
   *
   * @param supertype Supertype entity
   * @return Subtypes of a given supertype
   */
  Collection<EntitySubtype> getSubtypesOf(Entity supertype);

  /**
   * Gets all tables from the catalog.
   *
   * @return All tables
   */
  Collection<Table> getTables();

  /**
   * Gets all tables from the catalog that are not modeled as entities or relationships.
   *
   * @return Unmodeled tables
   */
  Collection<Table> getUnmodeledTables();

  /**
   * Look up a relationship by its bridge table.
   *
   * @param table Bridge table
   * @return Relationship, if found
   */
  Optional<Relationship> lookupByBridgeTable(Table table);

  /**
   * Look up an entity by its full name.
   *
   * @param entityName Full entity name
   * @return Entity, if found
   */
  Optional<Entity> lookupEntity(String entityName);

  /**
   * Look up an entity by its table.
   *
   * @param table Table
   * @return Entity, if found
   */
  Optional<Entity> lookupEntity(Table table);

  /**
   * Look up a relationship by its full name. This may be the same as the full name of a bridge
   * table, a foreign key, or an implicit relationship.
   *
   * @param relationshipName Full relationship name
   * @return Relationship, if found
   */
  Optional<Relationship> lookupRelationship(String relationshipName);

  /**
   * Look up a relationship by a table reference.
   *
   * @param tableRef Table reference.
   * @return Relationship, if found
   */
  Optional<Relationship> lookupRelationship(TableReference tableRef);
}
