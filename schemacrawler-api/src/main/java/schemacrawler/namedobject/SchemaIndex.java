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
import java.io.Serializable;
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
import schemacrawler.schema.Schema;

public final class SchemaIndex implements NamedObjectIndex<Schema>, Serializable {

  @Serial private static final long serialVersionUID = -569749297532359027L;

  private final Map<NamedObjectKey, Schema> objects;

  public SchemaIndex() {
    objects = new ConcurrentHashMap<>();
  }

  public SchemaIndex(final Collection<Schema> schemas) {
    if (schemas == null) {
      objects = Map.of();
    } else {
      final Map<NamedObjectKey, Schema> schemasMap = new HashMap<>();
      for (final Schema schema : schemas) {
        schemasMap.put(schema.key(), schema);
      }
      objects = Map.copyOf(schemasMap);
    }
  }

  public boolean add(final Schema schema) {
    if (schema == null) {
      return false;
    }
    objects.put(schema.key(), schema);
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
  public List<Schema> getObjects() {
    final List<Schema> sortedValues = new ArrayList<>(objects.values());
    Collections.sort(sortedValues);
    return List.copyOf(sortedValues);
  }

  /** {@inheritDoc} */
  @Override
  public List<Schema> getObjects(final Schema schema) {
    throw new UnsupportedOperationException();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isEmpty() {
    return objects.isEmpty();
  }

  @Override
  public Iterator<Schema> iterator() {
    return getObjects().iterator();
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Schema> lookup(final NamedObjectKey key) {
    if (key == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(objects.get(key));
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Schema> lookup(final Schema schema, final String name) {
    throw new UnsupportedOperationException();
  }

  /** {@inheritDoc} */
  @Override
  public int size() {
    return objects.size();
  }

  /** {@inheritDoc} */
  @Override
  public List<Schema> snapshot() {
    return List.copyOf(objects.values());
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return getObjects().stream().map(NamedObject::toString).collect(joining(", "));
  }
}
