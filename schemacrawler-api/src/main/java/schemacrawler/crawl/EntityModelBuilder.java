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
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.ForeignKeyCardinality;

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
}
