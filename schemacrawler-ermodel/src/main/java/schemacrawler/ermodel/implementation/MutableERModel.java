/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.implementation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.EntitySubtype;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.ermodel.model.Relationship;
import schemacrawler.ermodel.model.RelationshipCardinality;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;

public class MutableERModel implements ERModel {

  private final Map<NamedObjectKey, Table> tablesMap;
  private final Map<NamedObjectKey, Entity> entitiesMap;
  private final Map<NamedObjectKey, Relationship> relationshipsMap;

  public MutableERModel() {
    tablesMap = new HashMap<>();
    entitiesMap = new HashMap<>();
    relationshipsMap = new HashMap<>();
  }

  @Override
  public Collection<Entity> getEntities() {
    return Set.copyOf(entitiesMap.values());
  }

  @Override
  public Collection<Entity> getEntitiesByType(final EntityType entityType) {
    if (entityType == null) {
      return Collections.emptySet();
    }
    return entitiesMap.values().stream()
        .filter(entity -> entity.getType().equals(entityType))
        .collect(Collectors.toSet());
  }

  @Override
  public Collection<Relationship> getRelationships() {
    return Set.copyOf(relationshipsMap.values());
  }

  @Override
  public Collection<Relationship> getRelationshipsByType(
      final RelationshipCardinality cardinality) {
    if (cardinality == null) {
      return Collections.emptySet();
    }
    return relationshipsMap.values().stream()
        .filter(relationship -> relationship.getType().equals(cardinality))
        .collect(Collectors.toSet());
  }

  @Override
  public Collection<EntitySubtype> getSubtypesOf(final Entity supertype) {
    if (supertype == null) {
      return Collections.emptySet();
    }
    return getSubtypes().stream()
        .filter(subtype -> subtype.getSupertype().equals(supertype))
        .collect(Collectors.toSet());
  }

  @Override
  public Collection<Table> getTables() {
    return Set.copyOf(tablesMap.values());
  }

  @Override
  public Optional<Relationship> lookupByBridgeTable(final Table table) {
    if (table == null) {
      return Optional.empty();
    }
    final NamedObjectKey key = table.key();
    return relationshipsMap.values().stream()
        .filter(relationship -> relationship.key().equals(key))
        .findFirst();
  }

  @Override
  public Optional<Relationship> lookupByBridgeTableName(final String tableName) {
    return relationshipsMap.values().stream()
        .filter(relationship -> relationship.getFullName().equals(tableName))
        .findFirst();
  }

  @Override
  public Optional<Entity> lookupEntity(final String entityName) {
    return entitiesMap.values().stream()
        .filter(entity -> entity.getFullName().equals(entityName))
        .findFirst();
  }

  @Override
  public Optional<Entity> lookupEntity(final Table table) {
    if (table == null) {
      return Optional.empty();
    }
    final NamedObjectKey key = table.key();
    return Optional.ofNullable(entitiesMap.get(key));
  }

  void addEntity(final Entity entity) {
    if (entity != null) {
      entitiesMap.put(entity.key(), entity);
    }
  }

  void addRelationship(final Relationship relationship) {
    if (relationship != null) {
      relationshipsMap.put(relationship.key(), relationship);
    }
  }

  void addTable(final Table table) {
    if (table != null) {
      tablesMap.put(table.key(), table);
    }
  }

  private Collection<EntitySubtype> getSubtypes() {
    return entitiesMap.values().stream()
        .filter(entity -> EntityType.subtype.equals(entity.getType()))
        .map(entity -> (EntitySubtype) entity)
        .collect(Collectors.toSet());
  }
}
