/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.utility;

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
 * detached, unmodifiable lists in natural order. Implementations may provide stronger guarantees.
 *
 * @param <K> Complete lookup-key type
 * @param <N> Named-object type
 */
public interface NamedObjectIndex<N extends NamedObject> extends Serializable, Iterable<N> {

  /**
   * Checks whether the index contains the complete key.
   *
   * @param key Complete lookup key
   * @return Whether the key is present
   * @throws NullPointerException if the key is null
   */
  boolean containsKey(final NamedObjectKey key);

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
   * @return The object, if present
   * @throws NullPointerException if the key is null
   */
  Optional<N> lookup(NamedObjectKey key);

  /**
   * Looks up a database object by its schema key and exact object name.
   *
   * <p>This lookup is independent of the object's complete membership key. Blank names are matched
   * literally. Implementations select one matching object when multiple objects qualify; this
   * operation does not identify a unique overload.
   *
   * @param schema Schema to match by key
   * @param name Exact object name
   * @return The selected matching object, if present
   * @throws NullPointerException if the schema or name is null
   */
  Optional<N> lookup(Schema schema, String name);

  /**
   * Gets the number of objects in the index.
   *
   * @return Number of indexed objects
   */
  int size();

  /**
   * Gets a detached, unmodifiable unsorted collection of objects.
   *
   * @return Naturally ordered objects
   */
  Collection<N> snapshot();

  /**
   * Gets a detached, unmodifiable list of objects in natural order.
   *
   * @return Naturally ordered objects
   */
  List<N> values();
}
