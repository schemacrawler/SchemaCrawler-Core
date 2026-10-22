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
import schemacrawler.schema.NamedObjectKey;

/**
 * Provides immutable membership for database objects keyed by each object's complete key.
 *
 * <p>When keys are duplicated, the first object in collection encounter order is retained. The
 * membership is copied from the collection, but objects are shared references. Keys must remain
 * stable. Sorted enumeration sorts afresh on each call; objects must have stable comparison state
 * during a sort. By default, schema/name lookup uses a direct complete-key read. Overload-aware
 * mode instead selects the natural-order first prefix match, with comparison ties unspecified.
 * Snapshots have unspecified iteration order and do not compare objects. Returned lists are
 * detached and unmodifiable. The index is shallowly immutable and serializable when its objects are
 * serializable.
 *
 * @param <D> Database-object type
 */
public final class ImmutableDatabaseObjectIndex<D extends DatabaseObject>
    extends AbstractDatabaseObjectIndex<D> {

  @Serial private static final long serialVersionUID = -4924635231319889869L;

  private static <D extends DatabaseObject> Map<NamedObjectKey, D> immutableNamedObjectMap(
      final Collection<? extends D> values) {
    requireNonNull(values, "Objects cannot be null");
    final Map<NamedObjectKey, D> index = new HashMap<>();
    for (final D object : values) {
      requireNonNull(object, "Database objects cannot be null");
      index.putIfAbsent(object.key(), object);
    }
    return Map.copyOf(index);
  }

  /**
   * Copies a collection into immutable membership, retaining the first object encountered for each
   * key.
   *
   * @param values Objects to index
   * @throws NullPointerException if the collection, an object, or an object's key is null
   */
  public ImmutableDatabaseObjectIndex(final Collection<? extends D> values) {
    this(values, false);
  }

  /**
   * Copies membership with optional overload-aware schema/name lookup, retaining the first object
   * encountered for each complete key.
   *
   * @param values Objects to index
   * @param supportsOverloads Whether schema/name queries match appended key components, as for
   *     routine specific names; false retains direct exact-key lookup
   * @throws NullPointerException if the collection, an object, or an object's key is null
   */
  public ImmutableDatabaseObjectIndex(
      final Collection<? extends D> values, final boolean supportsOverloads) {
    super(immutableNamedObjectMap(values), supportsOverloads);
  }
}
