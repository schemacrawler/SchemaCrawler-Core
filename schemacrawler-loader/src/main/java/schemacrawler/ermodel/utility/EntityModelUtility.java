/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.utility;

import static schemacrawler.utility.MetaDataUtility.isPartial;

import schemacrawler.ermodel.build.TableEntityModelInferrer;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.ermodel.model.ForeignKeyCardinality;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableReference;
import us.fatehi.utility.OptionalBoolean;
import us.fatehi.utility.UtilityMarker;

/** Utility for inferring entity model information from tables and foreign keys. */
@UtilityMarker
public class EntityModelUtility {

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
    final OptionalBoolean coveredByIndex = tableEntityModel.foreignKeyCoveredByIndex(fk);
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
    final OptionalBoolean coveredByIndex = tableEntityModel.foreignKeyCoveredByUniqueIndex(fk);
    return coveredByIndex;
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

  /**
   * Infers the cardinality of a foreign key.
   *
   * @param fk Foreign key
   * @return Inferred cardinality
   */
  public static ForeignKeyCardinality inferCardinality(final TableReference fk) {
    if (fk == null) {
      return ForeignKeyCardinality.unknown;
    }

    final Table table = fk.getForeignKeyTable();
    if (table == null || isPartial(table)) {
      return ForeignKeyCardinality.unknown;
    }

    final TableEntityModelInferrer tableEntityModel = new TableEntityModelInferrer(table);
    final ForeignKeyCardinality fkCardinality = tableEntityModel.inferForeignKeyCardinality(fk);
    return fkCardinality;
  }

  private EntityModelUtility() {
    // Prevent instantiation
  }
}
