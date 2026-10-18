/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import static java.util.stream.Collectors.joining;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;

public final class MapBackedNamedObjectIndex<N extends NamedObject>
    implements NamedObjectIndex<N>, BuildableIndex<N> {

  @Serial private static final long serialVersionUID = -569749297532359027L;

  private final Map<NamedObjectKey, N> objects;

  public MapBackedNamedObjectIndex() {
    objects = new ConcurrentHashMap<>();
  }

  public MapBackedNamedObjectIndex(final Collection<N> schemas) {
    if (schemas == null) {
      objects = Map.of();
    } else {
      final Map<NamedObjectKey, N> schemasMap = new HashMap<>();
      for (final N namedObject : schemas) {
        schemasMap.put(namedObject.key(), namedObject);
      }
      objects = Map.copyOf(schemasMap);
    }
  }

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

  @Override
  public Iterator<N> iterator() {
    return getObjects().iterator();
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
