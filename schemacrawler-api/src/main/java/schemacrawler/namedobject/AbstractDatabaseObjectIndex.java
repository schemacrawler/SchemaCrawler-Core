/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;

/** Shares keyed storage and schema-aware read operations between database-object indexes. */
abstract class AbstractDatabaseObjectIndex<D extends DatabaseObject>
    implements DatabaseObjectIndex<D>, Serializable {

  private static final class FilterForSchema implements Predicate<DatabaseObject> {

    private final Schema schema;

    FilterForSchema(final Schema schema) {
      this.schema = requireNonNull(schema, "No schema provided");
    }

    @Override
    public boolean test(final DatabaseObject databaseObject) {
      return databaseObject != null && databaseObject.getSchema().equals(schema);
    }
  }

  @Serial private static final long serialVersionUID = -6220925141708190854L;

  private final Map<NamedObjectKey, D> objects;

  /** Uses the supplied storage map directly; subclasses determine its mutation policy. */
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
    final List<D> sortedValues = new ArrayList<>(objects.values());
    Collections.sort(sortedValues);
    return List.copyOf(sortedValues);
  }

  /** {@inheritDoc} */
  @Override
  public List<D> getObjects(final Schema schema) {
    if (schema == null) {
      return List.of();
    }
    final FilterForSchema schemaFilter = new FilterForSchema(schema);
    final List<D> filteredObjects = new ArrayList<>();
    for (final D object : snapshot()) {
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
  public final boolean isEmpty() {
    return objects.isEmpty();
  }

  @Override
  public Iterator<D> iterator() {
    return getObjects().iterator();
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
    if (schema == null || name == null) {
      return Optional.empty();
    }
    final Comparator<D> comparator = Comparator.naturalOrder();
    return snapshot().stream()
        .filter(DatabaseObject.class::isInstance)
        .filter(
            namedObject ->
                namedObject.getSchema().equals(schema) && namedObject.getName().equals(name))
        .min(comparator);
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

  /** Access to storage map for subclasses. */
  Map<NamedObjectKey, D> internalMap() {
    return objects;
  }
}
