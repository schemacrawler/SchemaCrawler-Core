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
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Table;

/** Conceptual entity backed by a SchemaCrawler table. */
class MutableEntity extends AbstractTableBacked implements Entity {

  @Serial private static final long serialVersionUID = 3946422106166202467L;

  private EntityType entityType;

  public MutableEntity(final Table table) {
    super(table);
    entityType = EntityType.unknown;
    // No checks for partial table - exceptions will be thrown while calling
    // unsupported methods
  }

  @Override
  public int compareTo(final NamedObject namedObj) {
    if (namedObj == null) {
      return 1;
    }
    return key().compareTo(namedObj.key());
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof final NamedObject namedObj) {
      return key().equals(namedObj.key());
    }
    return false;
  }

  @Override
  public EntityType getType() {
    return entityType;
  }

  @Override
  public int hashCode() {
    return key().hashCode();
  }

  void setEntityType(final EntityType entityType) {
    if (entityType != null) {
      this.entityType = entityType;
    }
    // No checks done about setting entity type for unknown or non entities
  }
}
