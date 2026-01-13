/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jspecify.annotations.Nullable;
import schemacrawler.schema.EntityType;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.ForeignKeyCardinality;
import schemacrawler.schema.Table;

/** Helps build the entity model, but does not follow the SchemaCrawler builder pattern. */
public final class EntityModelBuilder {

  private static final Logger LOGGER = Logger.getLogger(EntityModelBuilder.class.getName());

  public static EntityModelBuilder builder() {
    return new EntityModelBuilder();
  }

  public void updateTableEntity(final Table table, @Nullable final EntityType entityType) {
    LOGGER.log(Level.FINEST, "Setting entity type <%s> on table <%s>".formatted(entityType, table));
    if (table instanceof final MutableTable mutableTable) {
      mutableTable.setEntityType(entityType);
    }
  }

  public void updateForeignKeyCardinality(ForeignKey fk, ForeignKeyCardinality fkCardinality) {
    LOGGER.log(Level.FINEST, "Setting <%s> on foreign key <%s>".formatted(fkCardinality, fk));
    if (fk instanceof final AbstractTableReference tableReference) {
      tableReference.setForeignKeyCardinality(fkCardinality);
    }
  }
}
