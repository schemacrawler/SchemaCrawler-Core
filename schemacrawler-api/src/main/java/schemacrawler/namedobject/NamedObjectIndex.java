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

/**
 * Provides read-only lookup and enumeration of named objects.
 *
 * <p>Membership mutability and thread safety depend on the implementation. {@link #getObjects()}
 * returns a detached, unmodifiable list freshly sorted in natural order, requiring O(n log n)
 * comparisons in the general case. {@link #snapshot()} copies membership in O(n) time without
 * comparing objects and has unspecified iteration order. Elements are shared references: their
 * complete keys must remain stable while indexed, and their comparison state must remain stable
 * during sorting. Equal comparison results have unspecified relative order.
 *
 * <p>The index hierarchy is sealed; external implementations are not supported. Create indexes
 * through {@link NamedObjectIndexes}; implementation constructors are package-private.
 *
 * @param <N> Named-object type
 */
public sealed interface NamedObjectIndex<N extends NamedObject> extends Serializable
    permits DatabaseObjectIndex, BuildableNamedObjectIndex {

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
   * Checks whether the index contains no named objects.
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
   * Looks up the natural-order first object with an exact full name.
   *
   * <p>Comparison ties have unspecified selection. Blank-name handling depends on the
   * implementation: {@link MapBackedNamedObjectIndex} accepts blank full names, whereas database
   * object indexes reject them.
   *
   * @param fullName Full named object name
   * @return The object, if present; empty if the full name is null or absent
   */
  Optional<N> lookup(String fullName);

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
