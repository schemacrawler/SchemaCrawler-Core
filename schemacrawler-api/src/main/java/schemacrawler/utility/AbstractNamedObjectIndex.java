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
import java.util.Map;
import java.util.Optional;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;

abstract class AbstractNamedObjectIndex<N extends NamedObject>
    implements NamedObjectIndex<NamedObjectKey, N>, Serializable {

  @Serial private static final long serialVersionUID = -6220925141708190854L;

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

  // Access to storage map for subclasses
  Map<NamedObjectKey, N> getObjects() {
    return objects;
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
    return values().stream().map(NamedObject::toString).collect(joining(", "));
  }

  /** {@inheritDoc} */
  @Override
  public final List<N> values() {
    final List<N> sortedValues = new ArrayList<>(objects.values());
    Collections.sort(sortedValues);
    return List.copyOf(sortedValues);
  }
}
