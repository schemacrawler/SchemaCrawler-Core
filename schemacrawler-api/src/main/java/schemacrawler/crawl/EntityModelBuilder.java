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
import us.fatehi.utility.OptionalBoolean;

/** Helps build the entity model, but does not follow the SchemaCrawler builder pattern. */
public final class EntityModelBuilder {

  private static final Logger LOGGER = Logger.getLogger(EntityModelBuilder.class.getName());

  public static EntityModelBuilder builder() {
    return new EntityModelBuilder();
  }

  public void updateForeignKeyCardinality(
      final ForeignKey fk, final ForeignKeyCardinality fkCardinality) {
    LOGGER.log(Level.FINEST, "Setting <%s> on foreign key <%s>".formatted(fkCardinality, fk));
    if (fk instanceof final AbstractTableReference tableReference) {
      tableReference.setForeignKeyCardinality(fkCardinality);
    }
  }

  public void updateForeignKeyIndexCoverage(
      final ForeignKey fk, final OptionalBoolean coveredByIndex) {
    LOGGER.log(
        Level.FINEST,
        "Setting index coverage <%s> on foreign key <%s>".formatted(coveredByIndex, fk));
    if (fk instanceof final AbstractTableReference tableReference) {
      tableReference.setHasIndex(coveredByIndex);
    }
  }

  public void updateForeignKeyUniqueIndexCoverage(
      final ForeignKey fk, final OptionalBoolean coveredByUniqueIndex) {
    LOGGER.log(
        Level.FINEST,
        "Setting unique key coverage <%s> on foreign key <%s>".formatted(coveredByUniqueIndex, fk));
    if (fk instanceof final AbstractTableReference tableReference) {
      tableReference.setHasUniqueIndex(coveredByUniqueIndex);
    }
  }

  public void updateTableEntity(final Table table, @Nullable final EntityType entityType) {
    LOGGER.log(Level.FINEST, "Setting entity type <%s> on table <%s>".formatted(entityType, table));
    if (table instanceof final MutableTable mutableTable) {
      mutableTable.setEntityType(entityType);
    }
  }
}
