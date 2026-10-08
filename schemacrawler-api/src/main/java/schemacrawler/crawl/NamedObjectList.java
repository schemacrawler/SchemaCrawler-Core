/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static java.util.Comparator.naturalOrder;
import static java.util.Objects.requireNonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;

/**
 * Ordered list of named objects, that can be searched associatively. NamedObjectList has the
 * ability to look up by dependant object which is not created yet. That is, by NamedObject +
 * String. Returns values sorted in natural sort order, and is iterable. The iterator does not allow
 * modifications to the underlying data structure.
 */
final class NamedObjectList<N extends NamedObject> implements Serializable, Iterable<N> {

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

  private final Map<NamedObjectKey, N> objects = new ConcurrentHashMap<>();

  @Override
  public Iterator<N> iterator() {
    final class UnmodifiableIterator implements Iterator<N> {

      private final Iterator<N> iterator;

      UnmodifiableIterator(final Iterator<N> iterator) {
        this.iterator = iterator;
      }

      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public N next() {
        return iterator.next();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    }

    return new UnmodifiableIterator(values().iterator());
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return values().stream().map(NamedObject::toString).collect(Collectors.joining(", "));
  }

  /**
   * Add a named object to the list.
   *
   * @param namedObject Named object
   */
  boolean add(final N namedObject) {
    requireNonNull(namedObject, "Cannot add a null object to the list");
    final NamedObjectKey key = makeLookupKey(namedObject);
    objects.put(key, namedObject);
    return true;
  }

  boolean contains(final NamedObject namedObject) {
    return objects.containsKey(makeLookupKey(namedObject));
  }

  boolean isEmpty() {
    return objects.isEmpty();
  }

  Optional<N> lookup(final NamedObject namedObject, final String name) {
    final NamedObjectKey key = makeLookupKey(namedObject, name);
    return internalGet(key);
  }

  /**
   * Looks up a named object by lookup key.
   *
   * @param key Internal lookup key
   * @return Named object
   */
  Optional<N> lookup(final NamedObjectKey key) {
    return internalGet(key);
  }

  N remove(final N namedObject) {
    return objects.remove(makeLookupKey(namedObject));
  }

  /**
   * Returns the number of elements in this list.
   *
   * @return Number of elements in this list.
   */
  int size() {
    return objects.size();
  }

  /**
   * Gets all named objects in the list, in sorted order.
   *
   * @return All named objects
   */
  List<N> values() {
    final List<N> all = new ArrayList<>(objects.values());
    all.sort(naturalOrder());
    return all;
  }

  private Optional<N> internalGet(final NamedObjectKey key) {
    return Optional.ofNullable(objects.get(key));
  }
}
