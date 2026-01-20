/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.implementation;

import static java.util.Objects.requireNonNull;
import static schemacrawler.utility.MetaDataUtility.isPartial;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.ermodel.model.ForeignKeyCardinality;
import schemacrawler.ermodel.model.RelationshipCardinality;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;
import us.fatehi.utility.Builder;

public class ERModelBuilder implements Builder<ERModel> {

  private static final EnumSet<EntityType> VALID_ENTITY_TYPES =
      EnumSet.of(EntityType.strong_entity, EntityType.weak_entity, EntityType.subtype);

  private final Catalog catalog;
  final MutableERModel erModel;
  final Map<NamedObjectKey, TableEntityModelInferrer> inferrerMap;
  final Map<NamedObjectKey, MutableEntity> entityMap;

  public ERModelBuilder(final Catalog catalog) {
    this.catalog = requireNonNull(catalog, "No catalog provided");

    inferrerMap = new HashMap<>();
    // Contains all entities, including ones not added to the ER model
    entityMap = new HashMap<>();

    erModel = new MutableERModel();
  }

  @Override
  public ERModel build() {

    for (final Table table : catalog.getTables()) {
      erModel.addTable(table);
      if (isPartial(table)) {
        continue;
      }

      // Build main entity
      lookupOrCreateEntity(table);

      // Check for M..N relationship
      final TableEntityModelInferrer modelInferrer = getModelInferrer(table);
      if (modelInferrer.inferBridgeTable()) {
        // Build M..N relationship
        final MutableManyToManyRelationship mnRel = new MutableManyToManyRelationship(table);
        final List<ForeignKey> foreignKeys = new ArrayList<>(table.getForeignKeys());
        if (foreignKeys.size() != 2) {
          continue;
        }
        final Table leftTable = foreignKeys.get(0).getParent();
        mnRel.setLeftEntity(lookupOrCreateEntity(leftTable));
        final Table rightTable = foreignKeys.get(0).getParent();
        mnRel.setRightEntity(lookupOrCreateEntity(rightTable));

        erModel.addRelationship(mnRel);
      } else {
        // Build table reference relationships
        for (final ForeignKey fk : table.getImportedForeignKeys()) {
          final MutableTableReferenceRelationship tableRel =
              new MutableTableReferenceRelationship(fk);
          final ForeignKeyCardinality cardinality = modelInferrer.inferForeignKeyCardinality(fk);
          tableRel.setCardinality(RelationshipCardinality.from(cardinality));
          erModel.addRelationship(tableRel);
        }
      }
    }

    return erModel;
  }

  private TableEntityModelInferrer getModelInferrer(final Table table) {
    return inferrerMap.computeIfAbsent(table.key(), key -> new TableEntityModelInferrer(table));
  }

  private MutableEntity lookupOrCreateEntity(final Table table) {
    return entityMap.computeIfAbsent(
        table.key(),
        key -> {
          final MutableEntity newEntity = new MutableEntity(table);
          final TableEntityModelInferrer modelInferrer = getModelInferrer(table);
          final EntityType entityType = modelInferrer.inferEntityType();
          newEntity.setEntityType(entityType);
          if (VALID_ENTITY_TYPES.contains(entityType)) {
            erModel.addEntity(newEntity);
          }
          return newEntity;
        });
  }
}
