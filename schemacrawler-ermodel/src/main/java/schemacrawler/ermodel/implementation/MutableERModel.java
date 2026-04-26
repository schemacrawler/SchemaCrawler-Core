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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.EntitySubtype;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.ermodel.model.Relationship;
import schemacrawler.ermodel.model.RelationshipCardinality;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableReference;
import schemacrawler.schemacrawler.ModelImplementation;

@ModelImplementation
public class MutableERModel implements ERModel {

  @Serial private static final long serialVersionUID = -1912075263587495283L;

  private static final EnumSet<EntityType> VALID_ENTITY_TYPES =
      EnumSet.of(EntityType.strong_entity, EntityType.weak_entity, EntityType.subtype);

  private final Map<NamedObjectKey, Table> tablesMap;
  private final Set<TableReference> unmodeledTableReferences;
  private final Map<NamedObjectKey, Entity> entitiesMap;
  private final Map<NamedObjectKey, Relationship> relationshipsMap;
  private final Map<NamedObjectKey, Relationship> implicitRelationshipsMap;

  public MutableERModel() {
    tablesMap = new ConcurrentHashMap<>();
    unmodeledTableReferences = new CopyOnWriteArraySet<>();
    entitiesMap = new ConcurrentHashMap<>();
    relationshipsMap = new ConcurrentHashMap<>();
    implicitRelationshipsMap = new ConcurrentHashMap<>();
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
      return List.of();
    }
    return List.copyOf(
        entitiesMap.values().stream()
            .filter(entity -> entity.getType().equals(entityType))
            .sorted()
            .toList());
  }

  @Override
  public Collection<Relationship> getImplicitRelationships() {
    final Set<Relationship> implicitRelationships =
        new HashSet<>(implicitRelationshipsMap.values());
    return List.copyOf(implicitRelationships.stream().sorted().toList());
  }

  @Override
  public Collection<Relationship> getRelationships() {
    return List.copyOf(relationshipsMap.values().stream().sorted().toList());
  }

  @Override
  public Collection<Relationship> getRelationshipsByType(
      final RelationshipCardinality cardinality) {
    if (cardinality == null) {
      return List.of();
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
      return List.of();
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
  public Set<TableReference> getUnmodeledTableReferences() {
    return Set.copyOf(unmodeledTableReferences);
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
  public Optional<Relationship> lookupByBridgeTable(final Table table) {
    if (table == null) {
      return Optional.empty();
    }
    final NamedObjectKey key = table.key();
    // Look up bridge table
    // Only bridge tables can be looked up by this method, since the relationship
    // key for many--many
    // relationships is the same as the bridge table key
    if (relationshipsMap.containsKey(key)) {
      return Optional.of(relationshipsMap.get(key));
    }
    return Optional.empty();
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

  @Override
  public Optional<Relationship> lookupRelationship(final String relationshipName) {
    if (relationshipName == null) {
      return Optional.empty();
    }
    return relationshipsMap.values().stream()
        .filter(relationship -> relationship.getFullName().equals(relationshipName))
        .findFirst();
  }

  @Override
  public Optional<Relationship> lookupRelationship(final TableReference tableRef) {
    if (tableRef == null) {
      return Optional.empty();
    }
    final NamedObjectKey key = tableRef.key();
    if (relationshipsMap.containsKey(key)) {
      return Optional.of(relationshipsMap.get(key));
    }
    if (implicitRelationshipsMap.containsKey(key)) {
      return Optional.of(implicitRelationshipsMap.get(key));
    }
    return Optional.empty();
  }

  void addEntity(final Entity entity) {
    if (entity != null) {
      entitiesMap.put(entity.key(), entity);
    }
  }

  void addImplicitRelationship(final Relationship relationship) {
    if (relationship == null) {
      return;
    }

    implicitRelationshipsMap.put(relationship.key(), relationship);

    final MutableEntity leftEntity = (MutableEntity) relationship.getLeftEntity();
    if (leftEntity != null) {
      leftEntity.addImplicitRelationship(relationship);
    }

    final MutableEntity rightEntity = (MutableEntity) relationship.getRightEntity();
    if (rightEntity != null) {
      rightEntity.addImplicitRelationship(relationship);
    }
  }

  void addRelationship(final Relationship relationship) {
    if (relationship == null) {
      return;
    }

    relationshipsMap.put(relationship.key(), relationship);

    final MutableEntity leftEntity = (MutableEntity) relationship.getLeftEntity();
    if (leftEntity != null) {
      leftEntity.addRelationship(relationship);
    }

    final MutableEntity rightEntity = (MutableEntity) relationship.getRightEntity();
    if (rightEntity != null) {
      rightEntity.addRelationship(relationship);
    }
  }

  void addTable(final Table table) {
    if (table != null) {
      tablesMap.put(table.key(), table);
    }
  }

  void addUnmodeledTableReference(final TableReference tableRef) {
    if (tableRef != null) {
      unmodeledTableReferences.add(tableRef);
    }
  }

  private Collection<EntitySubtype> getSubtypes() {
    return entitiesMap.values().stream()
        .filter(entity -> EntityType.subtype.equals(entity.getType()))
        .map(entity -> (EntitySubtype) entity)
        .toList();
  }
}
