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

  <E extends Entity> Collection<E> getEntities();

  <E extends Entity> Collection<E> getEntitiesByType(EntityType entityType);

  <R extends Relationship> Collection<R> getRelationships();

  <R extends Relationship> Collection<R> getRelationshipsByType(
      RelationshipCardinality cardinality);

  <E extends EntitySubtype> Collection<E> getSubtypesOf(Entity supertype);

  <T extends Table> Collection<T> getTables();

  <R extends Relationship> Optional<R> lookupByBridgeTable(Table table);

  <R extends Relationship> Optional<R> lookupByBridgeTableName(String tableName);

  <E extends Entity> Optional<E> lookupEntity(String entityName);

  <E extends Entity> Optional<E> lookupEntity(Table table);
}
