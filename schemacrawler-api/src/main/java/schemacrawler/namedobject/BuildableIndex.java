/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import java.util.Optional;
import schemacrawler.schema.NamedObject;

/**
 * Provides optional keyed membership mutation.
 *
 * <p>Implementations with immutable membership reject non-null mutation requests with {@link
 * UnsupportedOperationException}. Null requests are no-ops in either membership mode. Thread safety
 * depends on the implementation.
 *
 * <p>The mutation hierarchy is sealed; external implementations are not supported.
 *
 * @param <N> Named-object type
 */
public sealed interface BuildableIndex<N extends NamedObject>
    permits BuildableDatabaseObjectIndex, MapBackedNamedObjectIndex {

  /**
   * Adds or replaces an object by its complete key.
   *
   * @param namedObject Object to add
   * @return True for every successful non-null request, including replacement of an existing key;
   *     false if the object is null
   * @throws NullPointerException if membership is mutable and the object's key is null
   * @throws UnsupportedOperationException if membership is immutable and the object is non-null
   */
  boolean add(N namedObject);

  /**
   * Removes and returns the object mapped to the supplied object's complete key.
   *
   * <p>The stored object need not be the same instance as the supplied object.
   *
   * @param namedObject Object whose complete key identifies the mapping to remove
   * @return The removed object, if present; empty if the object is null or absent
   * @throws NullPointerException if membership is mutable and the object's key is null
   * @throws UnsupportedOperationException if membership is immutable and the object is non-null
   */
  Optional<N> remove(N namedObject);
}
