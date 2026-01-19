/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.implementation;

import java.io.Serial;
import java.util.EnumSet;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.exceptions.ConfigurationException;

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

  private static final EnumSet<EntityType> VALID_ENTITY_TYPES =
      EnumSet.of(EntityType.strong_entity, EntityType.weak_entity, EntityType.subtype);

  void setEntityType(final EntityType entityType) {
    if (entityType == null || !VALID_ENTITY_TYPES.contains(entityType)) {
      throw new ConfigurationException("Not a valid entity type <%s>".formatted(entityType));
    }
    this.entityType = entityType;
  }
}
