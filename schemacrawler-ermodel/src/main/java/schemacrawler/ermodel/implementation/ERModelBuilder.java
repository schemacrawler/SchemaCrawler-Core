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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.ermodel.model.RelationshipCardinality;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;
import us.fatehi.utility.Builder;

public class ERModelBuilder implements Builder<ERModel> {

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
        final MutableManyToManyRelationship rel = new MutableManyToManyRelationship(table);
        final List<ForeignKey> foreignKeys = new ArrayList<>(table.getForeignKeys());
        if (foreignKeys.size() != 2) {
          continue;
        }
        final Table leftTable = foreignKeys.get(0).getPrimaryKeyTable();
        rel.setLeftEntity(lookupOrCreateEntity(leftTable));
        final Table rightTable = foreignKeys.get(1).getPrimaryKeyTable();
        rel.setRightEntity(lookupOrCreateEntity(rightTable));

        erModel.addRelationship(rel);
      } else {
        // Build table reference relationships
        for (final ForeignKey fk : table.getImportedForeignKeys()) {
          final MutableTableReferenceRelationship rel = new MutableTableReferenceRelationship(fk);
          final RelationshipCardinality cardinality = modelInferrer.inferCardinality(fk);
          rel.setCardinality(cardinality);

          final Table leftTable = fk.getForeignKeyTable();
          rel.setLeftEntity(lookupOrCreateEntity(leftTable));
          final Table rightTable = fk.getPrimaryKeyTable();
          rel.setRightEntity(lookupOrCreateEntity(rightTable));

          erModel.addRelationship(rel);
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
          final TableEntityModelInferrer modelInferrer = getModelInferrer(table);
          final EntityType entityType = modelInferrer.inferEntityType();
          final MutableEntity newEntity;
          if (entityType != EntityType.subtype) {
            newEntity = new MutableEntity(table);
            newEntity.setEntityType(entityType);
          } else {
            newEntity = new MutableEntitySubtype(table);
            newEntity.setEntityType(entityType);
            final Table superTypeTable = modelInferrer.inferSuperType().get();
            ((MutableEntitySubtype) newEntity).setSupertype(lookupOrCreateEntity(superTypeTable));
          }
          erModel.addEntity(newEntity);
          return newEntity;
        });
  }
}
