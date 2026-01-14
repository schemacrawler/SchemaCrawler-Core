/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.utility;

import schemacrawler.loader.entities.TableEntityModel;
import schemacrawler.schema.EntityType;
import schemacrawler.schema.ForeignKeyCardinality;
import schemacrawler.schema.PartialDatabaseObject;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableReference;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public class EntityModelUtility {

  public static EntityType identifyEntityType(final Table table) {
    if (table instanceof PartialDatabaseObject) {
      return EntityType.unknown;
    }

    final TableEntityModel tableEntityModel = new TableEntityModel(table);
    final EntityType entityType = tableEntityModel.identifyEntityType();
    return entityType;
  }

  public static ForeignKeyCardinality identifyForeignKeyCardinality(final TableReference fk) {
    if (fk == null) {
      return ForeignKeyCardinality.unknown;
    }

    final Table table = fk.getForeignKeyTable();
    if (table instanceof PartialDatabaseObject) {
      return ForeignKeyCardinality.unknown;
    }

    final TableEntityModel tableEntityModel = new TableEntityModel(table);
    final ForeignKeyCardinality fkCardinality = tableEntityModel.identifyForeignKeyCardinality(fk);
    return fkCardinality;
  }

  private EntityModelUtility() {
    // Prevent instantiation
  }
}
