/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.utility;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;

/**
 * Provides immutable membership for named objects keyed by each object's complete key.
 *
 * <p>When keys are duplicated, the first object in collection encounter order is retained. The
 * collection is detached, but objects are shared references. Keys must remain stable. Values are
 * naturally sorted afresh on each call; objects must have stable comparison state during a sort.
 * Schema/name lookup returns the first matching retained object in encounter order. Snapshots
 * preserve encounter order without comparing objects. Returned lists are detached and unmodifiable.
 * The index is shallowly immutable and serializable when its objects are serializable.
 *
 * @param <N> Named-object type
 */
public final class ImmutableNamedObjectIndex<N extends NamedObject>
    implements NamedObjectIndex<NamedObjectKey, N>, Serializable {

  @Serial private static final long serialVersionUID = -4924635231319889869L;

  private static boolean matchesSchemaAndName(
      final NamedObject namedObject, final NamedObjectKey schemaKey, final String name) {
    if (!(namedObject instanceof final DatabaseObject databaseObject)) {
      return false;
    }
    final Schema objectSchema = databaseObject.getSchema();
    return objectSchema != null
        && schemaKey.equals(objectSchema.key())
        && name.equals(databaseObject.getName());
  }

  private static NamedObjectKey schemaKey(final Schema schema) {
    return requireNonNull(
        requireNonNull(schema, "Schema cannot be null").key(), "Schema key cannot be null");
  }

  private final Map<NamedObjectKey, N> objects;

  /**
   * Creates an index from an encounter-ordered collection.
   *
   * @param values Objects to index
   * @throws NullPointerException if the collection, an object, or an object's key is null
   */
  public ImmutableNamedObjectIndex(final Collection<? extends N> values) {
    requireNonNull(values, "Objects cannot be null");
    final Map<NamedObjectKey, N> index = new LinkedHashMap<>();
    for (final N namedObject : values) {
      requireNonNull(namedObject, "Named objects cannot be null");
      final NamedObjectKey key =
          requireNonNull(namedObject.key(), "Named object keys cannot be null");
      index.putIfAbsent(key, namedObject);
    }
    objects = Collections.unmodifiableMap(index);
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsKey(final NamedObjectKey key) {
    return objects.containsKey(requireNonNull(key, "Lookup key cannot be null"));
  }

  /** {@inheritDoc} */
  @Override
  public boolean isEmpty() {
    return objects.isEmpty();
  }

  /** {@inheritDoc} */
  @Override
  public Optional<N> lookup(final NamedObjectKey key) {
    return Optional.ofNullable(objects.get(requireNonNull(key, "Lookup key cannot be null")));
  }

  /** {@inheritDoc} */
  @Override
  public Optional<N> lookup(final Schema schema, final String name) {
    final NamedObjectKey schemaKey = schemaKey(schema);
    requireNonNull(name, "Object name cannot be null");
    return snapshot().stream()
        .filter(namedObject -> matchesSchemaAndName(namedObject, schemaKey, name))
        .findFirst();
  }

  /** {@inheritDoc} */
  @Override
  public int size() {
    return objects.size();
  }

  /**
   * Gets an unmodifiable snapshot in original collection encounter order.
   *
   * @return Encounter-ordered objects
   */
  public List<N> snapshot() {
    return List.copyOf(objects.values());
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return values().stream().map(NamedObject::toString).collect(joining(", "));
  }

  /** {@inheritDoc} */
  @Override
  public List<N> values() {
    final List<N> sortedValues = new ArrayList<>(snapshot());
    Collections.sort(sortedValues);
    return List.copyOf(sortedValues);
  }
}
