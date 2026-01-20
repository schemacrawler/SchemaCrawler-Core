/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.implementation;

import java.io.Serial;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.schema.Table;

/** Conceptual entity backed by a SchemaCrawler table. */
class MutableEntity extends AbstractTableBacked implements Entity {

  @Serial private static final long serialVersionUID = 3946422106166202467L;

  private EntityType entityType;

  public MutableEntity(final Table table) {
    super(table);
    entityType = EntityType.unknown;
  }

  @Override
  public EntityType getType() {
    return entityType;
  }

  void setEntityType(final EntityType entityType) {
    if (entityType != null) {
      this.entityType = entityType;
    }
    // No checks done about setting entity type for unknown or non entities
  }
}
