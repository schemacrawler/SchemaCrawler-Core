/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.joining;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;

/**
 * Indexes named objects by complete key, with last-encountered duplicate replacement.
 *
 * <p>The no-argument constructor creates concurrent mutable membership. The collection constructor
 * creates detached immutable membership; non-null add/remove requests then throw {@link
 * UnsupportedOperationException}. Null add/remove requests remain no-ops in either mode. Objects
 * are shared references and keys must remain stable.
 *
 * <p>Sorted enumeration makes a fresh O(n log n) sort; comparison state must remain stable during
 * sorting. Snapshots copy membership in O(n) without comparing objects. Returned lists and
 * iterators are detached and unmodifiable. In mutable mode, individual membership operations are
 * thread-safe and enumeration is weakly consistent, not an atomic snapshot. Element state, compound
 * operations, and cross-index transactions are not synchronized. Serialization preserves the
 * membership mode when elements are serializable.
 *
 * @param <N> Named-object type
 */
public final class MapBackedNamedObjectIndex<N extends NamedObject>
    implements NamedObjectIndex<N>, BuildableIndex<N> {

  @Serial private static final long serialVersionUID = -569749297532359027L;

  private final Map<NamedObjectKey, N> objects;

  /** Creates an empty concurrent mutable index. */
  public MapBackedNamedObjectIndex() {
    objects = new ConcurrentHashMap<>();
  }

  /**
   * Copies a collection into immutable membership, retaining the last object for each key.
   *
   * @param values Objects to index; null creates an empty immutable index
   * @throws NullPointerException if an element or its key is null
   */
  public MapBackedNamedObjectIndex(final Collection<N> values) {
    if (values == null) {
      objects = Map.of();
    } else {
      final Map<NamedObjectKey, N> objectsMap = new HashMap<>();
      for (final N namedObject : values) {
        objectsMap.put(namedObject.key(), namedObject);
      }
      objects = Map.copyOf(objectsMap);
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean add(final N namedObject) {
    if (namedObject == null) {
      return false;
    }
    objects.put(namedObject.key(), namedObject);
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsKey(final NamedObjectKey key) {
    if (key == null) {
      return false;
    }
    return objects.containsKey(key);
  }

  /** {@inheritDoc} */
  @Override
  public List<N> getObjects() {
    final List<N> sortedValues = new ArrayList<>(objects.values());
    Collections.sort(sortedValues);
    return List.copyOf(sortedValues);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isEmpty() {
    return objects.isEmpty();
  }

  /** {@inheritDoc} */
  @Override
  public Optional<N> lookup(final NamedObjectKey key) {
    if (key == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(objects.get(key));
  }

  /** {@inheritDoc} */
  @Override
  public Optional<N> lookup(final String fullName) {
    // Blank schema full names are valid lookup targets.
    if (fullName == null) {
      return Optional.empty();
    }
    // Distinct schema keys can share a full name
    // Use natural order to select a match
    return snapshot().stream()
        .filter(namedObject -> fullName.equals(namedObject.getFullName()))
        .min(naturalOrder());
  }

  /** {@inheritDoc} */
  @Override
  public Optional<N> remove(final N namedObject) {
    if (namedObject == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(objects.remove(namedObject.key()));
  }

  /** {@inheritDoc} */
  @Override
  public int size() {
    return objects.size();
  }

  /** {@inheritDoc} */
  @Override
  public List<N> snapshot() {
    return List.copyOf(objects.values());
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return getObjects().stream().map(NamedObject::toString).collect(joining(", "));
  }
}
