/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import static us.fatehi.utility.Utility.isBlank;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;

/**
 * Provides a concurrent mutable index of database objects keyed by each object's complete key.
 *
 * <p>Inherits the mutation, snapshot, and thread-safety behavior documented by {@link
 * AbstractConcurrentIndex}. Schema-scoped reads follow {@link DatabaseObjectIndex}.
 *
 * @param <R> Database-object type
 */
public final class ConcurrentRoutineIndex<R extends Routine> extends AbstractConcurrentIndex<R> {

  @Serial private static final long serialVersionUID = -1089958942591485649L;

  /** Creates an empty concurrent index. */
  public ConcurrentRoutineIndex() {}

  /** {@inheritDoc} */
  @Override
  public List<R> getObjects(final Schema schema, final String name) {
    if (schema == null || isBlank(name)) {
      return List.of();
    }
    final Predicate<DatabaseObject> filter =
        new FilterForSchema(schema).and(object -> object.getName().equals(name));
    final List<R> filteredObjects = new ArrayList<>();
    for (final R object : snapshot()) {
      if (!filter.test(object)) {
        continue;
      }
      filteredObjects.add(object);
    }
    Collections.sort(filteredObjects);
    return List.copyOf(filteredObjects);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<R> lookup(final Schema schema, final String name) {
    if (schema == null || name == null) {
      return Optional.empty();
    }
    final List<R> objects = getObjects(schema, name);
    return objects.stream().findFirst();
  }
}
