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

public interface ERModel {

  Collection<Entity> getEntities();

  Collection<Entity> getEntitiesByType(EntityType entityType);

  Collection<Relationship> getRelationships();

  Collection<Relationship> getRelationshipsByType(RelationshipCardinality cardinality);

  Collection<EntitySubtype> getSubtypesOf(Entity supertype);

  Collection<Table> getTables();

  Collection<Table> getUnmodeledTables();

  Optional<Relationship> lookupByBridgeTable(Table table);

  Optional<Relationship> lookupByBridgeTableName(String tableName);

  Optional<Entity> lookupEntity(String entityName);

  Optional<Entity> lookupEntity(Table table);
}
