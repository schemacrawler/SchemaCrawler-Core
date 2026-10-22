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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.EntitySubtype;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.ermodel.model.Relationship;
import schemacrawler.ermodel.model.RelationshipCardinality;
import schemacrawler.namedobject.BuildableDatabaseObjectIndex;
import schemacrawler.namedobject.MapBackedNamedObjectIndex;
import schemacrawler.namedobject.NamedObjectIndexes;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableReference;
import schemacrawler.schemacrawler.ModelImplementation;

@ModelImplementation
class MutableERModel implements ERModel {

  @Serial private static final long serialVersionUID = -1912075263587495283L;

  private static final EnumSet<EntityType> VALID_ENTITY_TYPES =
      EnumSet.of(EntityType.strong_entity, EntityType.weak_entity, EntityType.subtype);

  private final BuildableDatabaseObjectIndex<Table> tablesIndex;
  private final Set<TableReference> unmodeledTableReferences;
  private final MapBackedNamedObjectIndex<Entity> entitiesIndex;
  private final MapBackedNamedObjectIndex<Relationship> relationshipsIndex;
  private final MapBackedNamedObjectIndex<Relationship> implicitRelationshipsIndex;

  public MutableERModel() {
    tablesIndex = NamedObjectIndexes.newBuildableDatabaseObjectIndex();
    unmodeledTableReferences = new CopyOnWriteArraySet<>();
    entitiesIndex = NamedObjectIndexes.newBuildableNamedObjectIndex();
    relationshipsIndex = NamedObjectIndexes.newBuildableNamedObjectIndex();
    implicitRelationshipsIndex = NamedObjectIndexes.newBuildableNamedObjectIndex();
  }

  @Override
  public Collection<Entity> getEntities() {
    return List.copyOf(
        entitiesIndex.snapshot().stream()
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
        entitiesIndex.snapshot().stream()
            .filter(entity -> entity.getType().equals(entityType))
            .sorted()
            .toList());
  }

  @Override
  public Collection<Relationship> getImplicitRelationships() {
    final Set<Relationship> implicitRelationships =
        new HashSet<>(implicitRelationshipsIndex.snapshot());
    return List.copyOf(implicitRelationships.stream().sorted().toList());
  }

  @Override
  public Collection<Relationship> getRelationships() {
    return relationshipsIndex.getObjects();
  }

  @Override
  public Collection<Relationship> getRelationshipsByType(
      final RelationshipCardinality cardinality) {
    if (cardinality == null) {
      return List.of();
    }
    return List.copyOf(
        relationshipsIndex.snapshot().stream()
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
    return tablesIndex.getObjects();
  }

  @Override
  public Set<TableReference> getUnmodeledTableReferences() {
    return Set.copyOf(unmodeledTableReferences);
  }

  @Override
  public Collection<Table> getUnmodeledTables() {
    return tablesIndex.snapshot().stream()
        // Not a valid entity
        .filter(
            table -> {
              final Entity entity = entitiesIndex.lookup(table.key()).orElse(null);
              return entity == null || !VALID_ENTITY_TYPES.contains(entity.getType());
            })
        // Not a bridge table
        .filter(not(table -> relationshipsIndex.containsKey(table.key())))
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
    return relationshipsIndex.lookup(key);
  }

  @Override
  public Optional<Entity> lookupEntity(final String entityName) {
    return entitiesIndex.snapshot().stream()
        .filter(entity -> entity.getFullName().equals(entityName))
        .findFirst();
  }

  @Override
  public Optional<Entity> lookupEntity(final Table table) {
    if (table == null) {
      return Optional.empty();
    }
    final NamedObjectKey key = table.key();
    return entitiesIndex.lookup(key);
  }

  @Override
  public Optional<Relationship> lookupRelationship(final String relationshipName) {
    if (relationshipName == null) {
      return Optional.empty();
    }
    return relationshipsIndex.snapshot().stream()
        .filter(relationship -> relationship.getFullName().equals(relationshipName))
        .findFirst();
  }

  @Override
  public Optional<Relationship> lookupRelationship(final TableReference tableRef) {
    if (tableRef == null) {
      return Optional.empty();
    }
    final NamedObjectKey key = tableRef.key();
    final Optional<Relationship> relationship = relationshipsIndex.lookup(key);
    return relationship.isPresent() ? relationship : implicitRelationshipsIndex.lookup(key);
  }

  void addEntity(final Entity entity) {
    if (entity != null) {
      entitiesIndex.add(entity);
    }
  }

  void addImplicitRelationship(final Relationship relationship) {
    if (relationship == null) {
      return;
    }

    implicitRelationshipsIndex.add(relationship);

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

    relationshipsIndex.add(relationship);

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
      tablesIndex.add(table);
    }
  }

  void addUnmodeledTableReference(final TableReference tableRef) {
    if (tableRef != null) {
      unmodeledTableReferences.add(tableRef);
    }
  }

  private Collection<EntitySubtype> getSubtypes() {
    return entitiesIndex.snapshot().stream()
        .filter(entity -> EntityType.subtype.equals(entity.getType()))
        .map(entity -> (EntitySubtype) entity)
        .toList();
  }
}
