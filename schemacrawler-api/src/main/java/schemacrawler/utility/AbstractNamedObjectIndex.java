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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;

abstract class AbstractNamedObjectIndex<N extends NamedObject>
    implements NamedObjectIndex<N>, Serializable {

  @Override
  public List<N> getObjects(Schema schema) {
    // TODO Auto-generated method stub
    return null;
  }

  @Serial private static final long serialVersionUID = -6220925141708190854L;

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

  private final Map<NamedObjectKey, N> objects;

  /** Creates an index from a provided. */
  public AbstractNamedObjectIndex(final Map<NamedObjectKey, N> objects) {
    // NOTE: Base class stores the provided storage map directly without making a defensive copy
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
  public final boolean isEmpty() {
    return objects.isEmpty();
  }

  @Override
  public Iterator<N> iterator() {
    return getObjects().iterator();
  }

  /** {@inheritDoc} */
  @Override
  public final Optional<N> lookup(final NamedObjectKey key) {
    if (key == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(objects.get(key));
  }

  /** {@inheritDoc} */
  @Override
  public final Optional<N> lookup(final Schema schema, final String name) {
    if (schema == null || name == null) {
      return Optional.empty();
    }
    final Comparator<N> comparator = Comparator.naturalOrder();
    return snapshot().stream()
        .filter(DatabaseObject.class::isInstance)
        .filter(
            namedObject ->
                ((DatabaseObject) namedObject).getSchema().equals(schema)
                    && namedObject.getName().equals(name))
        .min(comparator);
  }

  /** {@inheritDoc} */
  @Override
  public final int size() {
    return objects.size();
  }

  /**
   * Gets an unmodifiable snapshot in original collection encounter order.
   *
   * @return Encounter-ordered objects
   */
  @Override
  public final List<N> snapshot() {
    return List.copyOf(objects.values());
  }

  /** {@inheritDoc} */
  @Override
  public final String toString() {
    return getObjects().stream().map(NamedObject::toString).collect(joining(", "));
  }

  /** {@inheritDoc} */
  @Override
  public final List<N> getObjects() {
    final List<N> sortedValues = new ArrayList<>(objects.values());
    Collections.sort(sortedValues);
    return List.copyOf(sortedValues);
  }

  // Access to storage map for subclasses
  Map<NamedObjectKey, N> internalMap() {
    return objects;
  }
}
