/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.implementation;

import static java.util.function.Predicate.not;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.EntitySubtype;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.ermodel.model.Relationship;
import schemacrawler.ermodel.model.RelationshipCardinality;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableReference;

public class MutableERModel implements ERModel {

  @Serial private static final long serialVersionUID = -1912075263587495283L;

  private static final EnumSet<EntityType> VALID_ENTITY_TYPES =
      EnumSet.of(EntityType.strong_entity, EntityType.weak_entity, EntityType.subtype);

  private final Map<NamedObjectKey, Table> tablesMap;
  private final Map<NamedObjectKey, Entity> entitiesMap;
  private final Map<NamedObjectKey, Relationship> relationshipsMap;
  private final Map<NamedObjectKey, Relationship> weakRelationshipsMap;

  public MutableERModel() {
    tablesMap = new HashMap<>();
    entitiesMap = new HashMap<>();
    relationshipsMap = new HashMap<>();
    weakRelationshipsMap = new HashMap<>();
  }

  @Override
  public Collection<Entity> getEntities() {
    return List.copyOf(
        entitiesMap.values().stream()
            .filter(entity -> VALID_ENTITY_TYPES.contains(entity.getType()))
            .sorted()
            .toList());
  }

  @Override
  public Collection<Entity> getEntitiesByType(final EntityType entityType) {
    if (entityType == null) {
      return Collections.emptySet();
    }
    return List.copyOf(
        entitiesMap.values().stream()
            .filter(entity -> entity.getType().equals(entityType))
            .sorted()
            .toList());
  }

  @Override
  public Collection<Relationship> getRelationships() {
    return List.copyOf(relationshipsMap.values().stream().sorted().toList());
  }

  @Override
  public Collection<Relationship> getRelationshipsByType(
      final RelationshipCardinality cardinality) {
    if (cardinality == null) {
      return Collections.emptySet();
    }
    return List.copyOf(
        relationshipsMap.values().stream()
            .filter(relationship -> relationship.getType().equals(cardinality))
            .sorted()
            .toList());
  }

  @Override
  public Collection<EntitySubtype> getSubtypesOf(final Entity supertype) {
    if (supertype == null) {
      return Collections.emptySet();
    }
    return List.copyOf(
        getSubtypes().stream()
            .filter(subtype -> subtype.getSupertype().equals(supertype))
            .sorted()
            .toList());
  }

  @Override
  public Collection<Table> getTables() {
    return List.copyOf(tablesMap.values().stream().sorted().toList());
  }

  @Override
  public Collection<Table> getUnmodeledTables() {
    return tablesMap.keySet().stream()
        // Not a valid entity
        .filter(
            key -> {
              final Entity entity = entitiesMap.get(key);
              return entity == null || !VALID_ENTITY_TYPES.contains(entity.getType());
            })
        // Not a bridge table
        .filter(not(key -> relationshipsMap.containsKey(key)))
        .map(key -> tablesMap.get(key))
        .sorted()
        .toList();
  }

  @Override
  public Collection<Relationship> getWeakRelationships() {
    return List.copyOf(weakRelationshipsMap.values().stream().sorted().toList());
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
  public Optional<Relationship> lookupByTableReference(final TableReference tableRef) {
    if (tableRef == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(relationshipsMap.get(tableRef.key()));
  }

  @Override
  public Optional<Relationship> lookupByTableReferenceName(final String tableRefName) {
    if (tableRefName == null) {
      return Optional.empty();
    }
    return relationshipsMap.values().stream()
        .filter(relationship -> relationship.getFullName().equals(tableRefName))
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

  void addWeakRelationship(final Relationship relationship) {
    if (relationship != null) {
      weakRelationshipsMap.put(relationship.key(), relationship);
    }
  }

  private Collection<EntitySubtype> getSubtypes() {
    return entitiesMap.values().stream()
        .filter(entity -> EntityType.subtype.equals(entity.getType()))
        .map(entity -> (EntitySubtype) entity)
        .toList();
  }
}
