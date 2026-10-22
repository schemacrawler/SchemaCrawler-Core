/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.Schema;

final class FilterForSchema implements Predicate<DatabaseObject> {

  private final Schema schema;

  FilterForSchema(final Schema schema) {
    this.schema = requireNonNull(schema, "No schema provided");
  }

  @Override
  public boolean test(final DatabaseObject databaseObject) {
    return databaseObject != null && databaseObject.getSchema().equals(schema);
  }
}
