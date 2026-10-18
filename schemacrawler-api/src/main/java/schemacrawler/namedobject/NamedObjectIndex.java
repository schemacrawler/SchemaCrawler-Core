/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;

/**
 * Provides read-only lookup and enumeration of named objects.
 *
 * <p>The interface does not specify whether membership is immutable or thread-safe. Values are
 * detached, unmodifiable lists in natural order and sorting them takes O(n log n) comparisons.
 * Snapshots are detached and unmodifiable, take O(n) time, and have unspecified iteration order.
 * Elements are shared references.
 *
 * @param <K> Complete lookup-key type
 * @param <N> Named-object type
 */
public interface NamedObjectIndex<N extends NamedObject> extends Serializable, Iterable<N> {

  /**
   * Checks whether the index contains the complete key.
   *
   * @param key Complete lookup key
   * @return Whether the key is present; false if the key is null
   */
  boolean containsKey(final NamedObjectKey key);

  /**
   * Gets a detached, unmodifiable list of objects freshly sorted in natural order.
   *
   * @return Naturally ordered objects
   */
  List<N> getObjects();

  /**
   * Gets a detached, unmodifiable list of objects for an equal schema in natural order. The
   * operation scans the index and sorts the matching objects.
   *
   * @param schema Schema to match using {@link Schema#equals(Object)}
   * @return Naturally ordered objects for the schema, or an empty list if the schema is null
   */
  List<N> getObjects(Schema schema);

  /**
   * Checks whether the index contains any named objects.
   *
   * @return Whether the index contains no objects
   */
  boolean isEmpty();

  /**
   * Looks up an object by its complete key.
   *
   * @param key Complete lookup key
   * @return The object, if present; empty if the key is null or absent
   */
  Optional<N> lookup(NamedObjectKey key);

  /**
   * Looks up a database object by an equal schema and exact object name.
   *
   * <p>This lookup is independent of the object's complete membership key. Blank names are matched
   * literally. Implementations select the natural-order first matching object; natural comparison
   * ties have unspecified selection, and this operation does not identify a unique overload.
   * Indexed database objects are expected to have non-null schemas.
   *
   * @param schema Schema to match using {@link Schema#equals(Object)}
   * @param name Exact object name
   * @return The selected matching object, if present; empty if a query argument is null
   */
  Optional<N> lookup(Schema schema, String name);

  /**
   * Gets the number of objects in the index.
   *
   * @return Number of indexed objects
   */
  int size();

  /**
   * Gets a detached, unmodifiable unsorted collection of objects in O(n) time, without comparing
   * elements.
   *
   * @return Detached objects in unspecified order
   */
  Collection<N> snapshot();
}
