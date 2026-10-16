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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;

/**
 * Provides a concurrent mutable index of named objects keyed by each object's complete key.
 *
 * <p>Adding an object replaces any existing object with its key. Individual map operations are
 * thread-safe, and snapshots are weakly consistent during concurrent membership changes. A snapshot
 * is not an atomic point-in-time view. Schema/name lookup filters the snapshot and selects the
 * natural-order first match; comparison ties have unspecified selection. Compound operations,
 * cross-index transactions, and object state are not synchronized. Objects are shared references;
 * comparison and identity keys must remain stable, and comparison state must remain stable while
 * values are sorted. Returned lists are detached and unmodifiable. The index is serializable when
 * its objects are serializable.
 *
 * @param <N> Named-object type
 */
public final class ConcurrentNamedObjectIndex<N extends NamedObject>
    implements NamedObjectIndex<NamedObjectKey, N>, Serializable {

  @Serial private static final long serialVersionUID = -1089958942591485649L;

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

  private final ConcurrentMap<NamedObjectKey, N> objects = new ConcurrentHashMap<>();

  /** Creates an empty concurrent index. */
  public ConcurrentNamedObjectIndex() {}

  /**
   * Adds or replaces an object by its complete key.
   *
   * @param namedObject Object to add
   * @return Always true after a successful insertion
   * @throws NullPointerException if the object or its key is null
   */
  public boolean add(final N namedObject) {
    requireNonNull(namedObject, "Named object cannot be null");
    final NamedObjectKey key = requireNonNull(namedObject.key(), "Named object key cannot be null");
    objects.put(key, namedObject);
    return true;
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
    if (name == null) {
      return Optional.empty();
    }
    final Comparator<N> comparator = Comparator.naturalOrder();
    return snapshot().stream()
        .filter(namedObject -> matchesSchemaAndName(namedObject, schemaKey, name))
        .min(comparator);
  }

  /**
   * Removes and returns the object mapped to a complete key.
   *
   * @param key Complete lookup key
   * @return The removed object, if present
   * @throws NullPointerException if the key is null
   */
  public Optional<N> remove(final NamedObjectKey key) {
    return Optional.ofNullable(objects.remove(requireNonNull(key, "Lookup key cannot be null")));
  }

  /** {@inheritDoc} */
  @Override
  public int size() {
    return objects.size();
  }

  /**
   * Gets an unmodifiable detached snapshot in unspecified order.
   *
   * <p>The result may reflect any state observed during concurrent membership changes. It is not an
   * atomic point-in-time snapshot.
   *
   * @return Detached objects
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
