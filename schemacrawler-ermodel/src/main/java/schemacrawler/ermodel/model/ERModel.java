/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.model;

import java.util.Collection;
import java.util.Optional;
import schemacrawler.schema.Table;

/** Entity-relationship model. */
public interface ERModel {

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
   * Look up a relationship by its bridge table name.
   *
   * @param tableName Bridge table name
   * @return Relationship, if found
   */
  Optional<Relationship> lookupByBridgeTableName(String tableName);

  /**
   * Look up an entity by its name.
   *
   * @param entityName Entity name
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
}
