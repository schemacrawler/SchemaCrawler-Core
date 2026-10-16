/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;
import schemacrawler.utility.ConcurrentNamedObjectIndex;
import schemacrawler.utility.NamedObjectIndex;

/**
 * Ordered list of named objects, that can be searched associatively. NamedObjectList has the
 * ability to look up by dependant object which is not created yet. That is, by NamedObject +
 * String. Returns values sorted in natural sort order, and is iterable. The iterator does not allow
 * modifications to the underlying data structure.
 */
final class NamedObjectList<N extends NamedObject>
    implements Serializable, Iterable<N>, NamedObjectIndex<NamedObjectKey, N> {

  @Serial private static final long serialVersionUID = 3257847666804142128L;

  private static NamedObjectKey makeLookupKey(final NamedObject namedObject) {
    final NamedObjectKey key;
    if (namedObject == null) {
      key = null;
    } else {
      key = namedObject.key();
    }
    return key;
  }

  private static NamedObjectKey makeLookupKey(final NamedObject namedObject, final String name) {
    NamedObjectKey key = makeLookupKey(namedObject);
    if (key != null) {
      key = key.with(name);
    }
    return key;
  }

  private final ConcurrentNamedObjectIndex<N> objects = new ConcurrentNamedObjectIndex<>();

  /** {@inheritDoc} */
  @Override
  public boolean containsKey(final NamedObjectKey key) {
    return objects.containsKey(key);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isEmpty() {
    return objects.isEmpty();
  }

  @Override
  public Iterator<N> iterator() {
    return objects.values().iterator();
  }

  /**
   * Looks up a named object by lookup key.
   *
   * @param key Internal lookup key
   * @return Named object
   */
  @Override
  public Optional<N> lookup(final NamedObjectKey key) {
    return internalGet(key);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<N> lookup(final Schema schema, final String name) {
    return objects.lookup(schema, name);
  }

  /**
   * Returns the number of elements in this list.
   *
   * @return Number of elements in this list.
   */
  @Override
  public int size() {
    return objects.size();
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return objects.toString();
  }

  /**
   * Gets all named objects in the list, in sorted order.
   *
   * @return All named objects
   */
  @Override
  public List<N> values() {
    return objects.values();
  }

  /**
   * Add a named object to the list.
   *
   * @param namedObject Named object
   */
  boolean add(final N namedObject) {
    requireNonNull(namedObject, "Cannot add a null object to the list");
    return objects.add(namedObject);
  }

  boolean contains(final NamedObject namedObject) {
    return objects.containsKey(makeLookupKey(namedObject));
  }

  Optional<N> lookup(final NamedObject namedObject, final String name) {
    if (namedObject instanceof final Schema schema) {
      return lookup(schema, name);
    }
    final NamedObjectKey key = makeLookupKey(namedObject, name);
    return internalGet(key);
  }

  Optional<N> remove(final N namedObject) {
    return objects.remove(makeLookupKey(namedObject));
  }

  private Optional<N> internalGet(final NamedObjectKey key) {
    return objects.lookup(key);
  }
}
