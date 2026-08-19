/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.utility;

import static schemacrawler.utility.MetaDataUtility.isPartial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import schemacrawler.ermodel.implementation.ERModelBuilder;
import schemacrawler.ermodel.implementation.TableEntityModelInferrer;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.ermodel.model.Relationship;
import schemacrawler.ermodel.model.RelationshipCardinality;
import schemacrawler.ermodel.model.TableReferenceRelationship;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableReference;
import us.fatehi.utility.OptionalBoolean;
import us.fatehi.utility.UtilityMarker;

/** Utility for inferring entity model information from tables and foreign keys. */
@UtilityMarker
public class ERModelUtility {

  public static ERModel buildEmptyERModel() {
    return ERModelBuilder.buildEmptyERModel();
  }

  public static Collection<? extends TableReference> collectImplicitAssociations(
      final Table table, final ERModel erModel) {
    final Entity entity = erModel.lookupEntity(table).orElse(null);
    if (entity == null) {
      return List.of();
    }
    final List<TableReference> implicitAssociations = new ArrayList<>();
    for (final Relationship rel : entity.getImplicitRelationships()) {
      if (rel instanceof final TableReferenceRelationship tableRel) {
        final TableReference tableReference = tableRel.getTableReference();
        implicitAssociations.add(tableReference);
      }
    }
    erModel.getUnmodeledTableReferences().stream()
        .filter(
            tableRel ->
                tableRel.getPrimaryKeyTable().equals(table)
                    || tableRel.getForeignKeyTable().equals(table))
        .forEach(implicitAssociations::add);
    Collections.sort(implicitAssociations);
    return implicitAssociations;
  }

  /**
   * Checks if a foreign key is covered by an index.
   *
   * @param fk Foreign key
   * @return Whether the foreign key is covered by an index
   */
  public static OptionalBoolean coveredByIndex(final TableReference fk) {
    if (fk == null) {
      return OptionalBoolean.unknown;
    }

    final Table table = fk.getForeignKeyTable();
    if (table == null || isPartial(table)) {
      return OptionalBoolean.unknown;
    }

    final TableEntityModelInferrer tableEntityModel = new TableEntityModelInferrer(table);
    final OptionalBoolean coveredByIndex = tableEntityModel.coveredByIndex(fk);
    return coveredByIndex;
  }

  /**
   * Checks if a foreign key is covered by a unique index.
   *
   * @param fk Foreign key
   * @return Whether the foreign key is covered by a unique index
   */
  public static OptionalBoolean coveredByUniqueIndex(final TableReference fk) {
    if (fk == null) {
      return OptionalBoolean.unknown;
    }

    final Table table = fk.getForeignKeyTable();
    if (table == null || isPartial(table)) {
      return OptionalBoolean.unknown;
    }

    final TableEntityModelInferrer tableEntityModel = new TableEntityModelInferrer(table);
    final OptionalBoolean coveredByIndex = tableEntityModel.coveredByUniqueIndex(fk);
    return coveredByIndex;
  }

  /**
   * Infers if the table is a bridge table.
   *
   * @param table Table
   * @return Inferred bridge table type
   */
  public static OptionalBoolean inferBridgeTable(final Table table) {
    if (table == null || isPartial(table)) {
      return OptionalBoolean.unknown;
    }

    final TableEntityModelInferrer tableEntityModel = new TableEntityModelInferrer(table);
    final boolean isBridgeTable = tableEntityModel.inferBridgeTable();
    return OptionalBoolean.fromBoolean(isBridgeTable);
  }

  /**
   * Infers the cardinality of a foreign key.
   *
   * @param fk Foreign key
   * @return Inferred cardinality
   */
  public static RelationshipCardinality inferCardinality(final TableReference fk) {
    if (fk == null) {
      return RelationshipCardinality.unknown;
    }

    final Table table = fk.getForeignKeyTable();
    if (table == null || isPartial(table)) {
      return RelationshipCardinality.unknown;
    }

    final TableEntityModelInferrer tableEntityModel = new TableEntityModelInferrer(table);
    final RelationshipCardinality fkCardinality = tableEntityModel.inferCardinality(fk);
    return fkCardinality;
  }

  /**
   * Infers the entity type of a table.
   *
   * @param table Table
   * @return Inferred entity type
   */
  public static EntityType inferEntityType(final Table table) {
    if (table == null || isPartial(table)) {
      return EntityType.unknown;
    }

    final TableEntityModelInferrer tableEntityModel = new TableEntityModelInferrer(table);
    final EntityType entityType = tableEntityModel.inferEntityType();
    return entityType;
  }

  private ERModelUtility() {
    // Prevent instantiation
  }
}
