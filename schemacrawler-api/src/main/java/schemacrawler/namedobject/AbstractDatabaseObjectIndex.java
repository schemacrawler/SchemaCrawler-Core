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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;

/**
 * Shares keyed storage and schema-aware read operations between database-object indexes.
 *
 * <p>Schema-scoped enumeration filters an unsorted snapshot before comparing matching objects.
 * Subclasses select the storage map and its mutation and concurrency policies.
 *
 * @param <D> Database-object type
 */
abstract sealed class AbstractDatabaseObjectIndex<D extends DatabaseObject>
    implements DatabaseObjectIndex<D>, Serializable
    permits AbstractConcurrentIndex, ImmutableDatabaseObjectIndex, ImmutableRoutineIndex {

  @Serial private static final long serialVersionUID = -6220925141708190854L;

  private final Map<NamedObjectKey, D> objects;

  /**
   * Uses the supplied storage map directly, without copying it.
   *
   * @param objects Storage map with non-null objects and complete keys
   * @throws NullPointerException if the storage map is null
   */
  public AbstractDatabaseObjectIndex(final Map<NamedObjectKey, D> objects) {
    this.objects = requireNonNull(objects, "Objects cannot be null");
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
    final List<D> sortedValues = new ArrayList<>(internalSnapshot());
    Collections.sort(sortedValues);
    return List.copyOf(sortedValues);
  }

  /** {@inheritDoc} */
  @Override
  public final List<D> getObjects(final Schema schema) {
    if (schema == null) {
      return List.of();
    }
    final FilterForSchema schemaFilter = new FilterForSchema(schema);
    final List<D> filteredObjects = new ArrayList<>();
    for (final D object : internalSnapshot()) {
      if (!schemaFilter.test(object)) {
        continue;
      }
      filteredObjects.add(object);
    }
    Collections.sort(filteredObjects);
    return List.copyOf(filteredObjects);
  }

  /** {@inheritDoc} */
  @Override
  public List<D> getObjects(final Schema schema, final String name) {
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
    final FilterForSchema schemaFilter = new FilterForSchema(schema);
    for (final D object : internalSnapshot()) {
      if (schemaFilter.test(object)) {
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
  public Optional<D> lookup(final Schema schema, final String name) {
    if (schema == null || name == null) {
      return Optional.empty();
    }
    return lookup(schema.key().with(name));
  }

  /** {@inheritDoc} */
  @Override
  public Optional<D> lookup(final String fullName) {
    if (isBlank(fullName)) {
      return Optional.empty();
    }
    return internalSnapshot().stream()
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
    return List.copyOf(internalSnapshot());
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

  private Collection<D> internalSnapshot() {
    return objects.values();
  }
}
