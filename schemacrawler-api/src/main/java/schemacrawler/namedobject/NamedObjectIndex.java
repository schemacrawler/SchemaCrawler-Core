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
 * <p>The interface does not specify whether membership is immutable or thread-safe. Values are
 * detached, unmodifiable lists in natural order and sorting them takes O(n log n) comparisons.
 * Snapshots are detached and unmodifiable, take O(n) time, and have unspecified iteration order.
 * Elements are shared references.
 *
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
