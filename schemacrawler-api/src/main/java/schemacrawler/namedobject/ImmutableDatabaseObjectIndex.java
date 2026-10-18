/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;

/**
 * Provides immutable membership for database objects keyed by each object's complete key.
 *
 * <p>When keys are duplicated, the first object in collection encounter order is retained. The
 * collection is detached, but objects are shared references. Keys must remain stable. Values are
 * naturally sorted afresh on each call; objects must have stable comparison state during a sort.
 * Schema/name lookup returns the natural-order first matching object, with comparison ties
 * unspecified. Snapshots have unspecified iteration order and do not compare objects. Returned
 * lists are detached and unmodifiable. The index is shallowly immutable and serializable when its
 * objects are serializable.
 *
 * @param <D> Database-object type
 */
public final class ImmutableDatabaseObjectIndex<D extends DatabaseObject>
    extends AbstractDatabaseObjectIndex<D> {

  @Serial private static final long serialVersionUID = -4924635231319889869L;

  private static <N extends NamedObject> Map<NamedObjectKey, N> immutableNamedObjectMap(
      final Collection<? extends N> values) {
    requireNonNull(values, "Objects cannot be null");
    final Map<NamedObjectKey, N> index = new HashMap<>();
    for (final N namedObject : values) {
      requireNonNull(namedObject, "Named objects cannot be null");
      index.putIfAbsent(namedObject.key(), namedObject);
    }
    return Map.copyOf(index);
  }

  /**
   * Creates an index from an encounter-ordered collection.
   *
   * @param values Objects to index
   * @throws NullPointerException if the collection, an object, or an object's key is null
   */
  public ImmutableDatabaseObjectIndex(final Collection<? extends D> values) {
    super(immutableNamedObjectMap(values));
  }
}
