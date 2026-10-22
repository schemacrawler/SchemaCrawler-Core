/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import static java.util.Comparator.naturalOrder;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static us.fatehi.utility.Utility.isBlank;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;

/**
 * Shares keyed storage and schema-aware read operations between database-object indexes.
 *
 * <p>Schema-scoped enumeration filters stored key prefixes before comparing matching objects.
 * Subclasses select the storage map and its mutation and concurrency policies.
 *
 * @param <D> Database-object type
 */
abstract sealed class AbstractDatabaseObjectIndex<D extends DatabaseObject>
    implements DatabaseObjectIndex<D>, Serializable
    permits AbstractConcurrentIndex, ImmutableDatabaseObjectIndex {

  @Serial private static final long serialVersionUID = -6220925141708190854L;

  private final Map<NamedObjectKey, D> objects;
  private final boolean supportsOverloads;

  /**
   * Uses the supplied storage map directly, without copying it.
   *
   * @param objects Storage map with non-null objects and complete keys
   * @throws NullPointerException if the storage map is null
   */
  AbstractDatabaseObjectIndex(final Map<NamedObjectKey, D> objects) {
    this(objects, false);
  }

  /**
   * Uses the supplied storage map and ordinary-name lookup policy.
   *
   * @param objects Storage map with non-null objects and stable hierarchical keys
   * @param supportsOverloads Whether schema/name queries match appended key components
   * @throws NullPointerException if the storage map is null
   */
  AbstractDatabaseObjectIndex(
      final Map<NamedObjectKey, D> objects, final boolean supportsOverloads) {
    this.objects = requireNonNull(objects, "Objects cannot be null");
    this.supportsOverloads = supportsOverloads;
  }

  /** {@inheritDoc} */
  @Override
  public final boolean containsKey(final NamedObjectKey key) {
    if (key == null) {
      return false;
    }
    return objects.containsKey(key);
  }

  /** {@inheritDoc} */
  @Override
  public final List<D> getObjects() {
    final List<D> sortedValues = new ArrayList<>(objects.values());
    Collections.sort(sortedValues);
    return List.copyOf(sortedValues);
  }

  /** {@inheritDoc} */
  @Override
  public final List<D> getObjects(final Schema schema) {
    if (schema == null) {
      return List.of();
    }
    return sortedMatches(schema.key());
  }

  /** {@inheritDoc} */
  @Override
  public final List<D> getObjects(final Schema schema, final String name) {
    if (schema == null || isBlank(name)) {
      return List.of();
    }
    if (supportsOverloads) {
      return sortedMatches(schema.key().with(name));
    }
    final Optional<D> lookedupObject = lookup(schema, name);
    if (lookedupObject.isEmpty()) {
      return List.of();
    }
    return List.of(lookedupObject.get());
  }

  /** {@inheritDoc} */
  @Override
  public final boolean hasObjects(final Schema schema) {
    if (schema == null) {
      return false;
    }
    final NamedObjectKey prefix = schema.key();
    for (final NamedObjectKey key : objects.keySet()) {
      if (key.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public final boolean isEmpty() {
    return objects.isEmpty();
  }

  /** {@inheritDoc} */
  @Override
  public final Optional<D> lookup(final NamedObjectKey key) {
    if (key == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(objects.get(key));
  }

  /** {@inheritDoc} */
  @Override
  public final Optional<D> lookup(final Schema schema, final String name) {
    if (schema == null || isBlank(name)) {
      return Optional.empty();
    }
    final NamedObjectKey prefix = schema.key().with(name);
    if (!supportsOverloads) {
      return lookup(prefix);
    }
    return matchingObjects(prefix).min(naturalOrder());
  }

  /** {@inheritDoc} */
  @Override
  public Optional<D> lookup(final String fullName) {
    if (isBlank(fullName)) {
      return Optional.empty();
    }
    return objects.values().stream()
        .filter(object -> fullName.equals(object.getFullName()))
        .min(naturalOrder());
  }

  /** {@inheritDoc} */
  @Override
  public final int size() {
    return objects.size();
  }

  /** {@inheritDoc} */
  @Override
  public final List<D> snapshot() {
    return List.copyOf(objects.values());
  }

  /** {@inheritDoc} */
  @Override
  public final String toString() {
    return getObjects().stream().map(NamedObject::toString).collect(joining(", "));
  }

  /** Returns the backing map for membership operations in permitted subclasses. */
  Map<NamedObjectKey, D> internalMap() {
    return objects;
  }

  private Stream<D> matchingObjects(final NamedObjectKey prefix) {
    return objects.keySet().stream()
        .filter(key -> key.startsWith(prefix))
        .map(objects::get)
        // Concurrent removal can occur between key traversal and value retrieval.
        .filter(object -> object != null);
  }

  private List<D> sortedMatches(final NamedObjectKey prefix) {
    return matchingObjects(prefix).sorted().toList();
  }
}
