/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.utility;

import static java.util.Objects.requireNonNull;

import schemacrawler.loader.entities.TableEntityIdentifier;
import schemacrawler.schema.EntityType;
import schemacrawler.schema.Table;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public class EntityModelUtility {

  public static EntityType identifyEntityType(final Table table) {
    requireNonNull(table, "No table provided");
    final EntityType entityType = new TableEntityIdentifier(table).identifyEntityType();
    return entityType;
  }

  private EntityModelUtility() {
    // Prevent instantiation
  }
}
