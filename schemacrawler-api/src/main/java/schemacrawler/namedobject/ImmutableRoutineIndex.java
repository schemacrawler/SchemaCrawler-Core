/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.isBlank;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;

/** Provides immutable routine membership with schema/name lookup for overloaded routines. */
public final class ImmutableRoutineIndex<R extends Routine> extends AbstractDatabaseObjectIndex<R> {

  @Serial private static final long serialVersionUID = -4924635231319889870L;

  private static <R extends Routine> Map<NamedObjectKey, R> immutableRoutineMap(
      final Collection<? extends R> values) {
    requireNonNull(values, "Objects cannot be null");
    final Map<NamedObjectKey, R> index = new HashMap<>();
    for (final R routine : values) {
      requireNonNull(routine, "Routines cannot be null");
      index.putIfAbsent(routine.key(), routine);
    }
    return Map.copyOf(index);
  }

  /**
   * Copies routines into immutable membership, retaining the first routine encountered for each
   * complete key.
   *
   * @param routines Routines to index
   * @throws NullPointerException if the collection, a routine, or a routine's key is null
   */
  public ImmutableRoutineIndex(final Collection<? extends R> routines) {
    super(immutableRoutineMap(routines));
  }

  /** {@inheritDoc} */
  @Override
  public List<R> getObjects(final Schema schema, final String name) {
    if (schema == null || isBlank(name)) {
      return List.of();
    }
    final Predicate<DatabaseObject> filter =
        new FilterForSchema(schema).and(object -> object.getName().equals(name));
    final List<R> filteredObjects = new ArrayList<>();
    for (final R routine : snapshot()) {
      if (filter.test(routine)) {
        filteredObjects.add(routine);
      }
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
    return getObjects(schema, name).stream().findFirst();
  }
}
