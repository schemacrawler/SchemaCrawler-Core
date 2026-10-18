/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import java.io.Serial;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import schemacrawler.namedobject.ConcurrentDatabaseObjectIndex;
import schemacrawler.namedobject.DatabaseObjectIndex;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;

/**
 * Ordered list of named objects, that can be searched associatively. NamedObjectList has the
 * ability to look up by dependant object which is not created yet. That is, by NamedObject +
 * String. Returns values sorted in natural sort order, and is iterable. The iterator does not allow
 * modifications to the underlying data structure.
 */
final class NamedObjectList<D extends DatabaseObject> implements DatabaseObjectIndex<D> {

  @Serial private static final long serialVersionUID = 3257847666804142128L;

  private final ConcurrentDatabaseObjectIndex<D> objects = new ConcurrentDatabaseObjectIndex<>();

  @Override
  public boolean containsKey(final NamedObjectKey key) {
    return objects.containsKey(key);
  }

  /**
   * Gets all named objects in the list, in sorted order.
   *
   * @return All named objects
   */
  @Override
  public List<D> getObjects() {
    return objects.getObjects();
  }

  @Override
  public List<D> getObjects(Schema schema) {
    return objects.getObjects(schema);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isEmpty() {
    return objects.isEmpty();
  }

  @Override
  public Iterator<D> iterator() {
    return objects.iterator();
  }

  /**
   * Looks up a named object by lookup key.
   *
   * @param key Internal lookup key
   * @return Named object
   */
  @Override
  public Optional<D> lookup(final NamedObjectKey key) {
    return objects.lookup(key);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<D> lookup(final Schema schema, final String name) {
    return objects.lookup(schema, name);
  }

  @Override
  public Optional<D> lookup(String fullName) {
    return objects.lookup(fullName);
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

  @Override
  public Collection<D> snapshot() {
    return objects.snapshot();
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return objects.toString();
  }

  /**
   * Add a named object to the list.
   *
   * @param namedObject Named object
   */
  boolean add(final D namedObject) {
    return objects.add(namedObject);
  }

  Optional<D> lookup(final NamedObject namedObject, final String name) {
    return objects.lookup(namedObject, name);
  }

  Optional<D> remove(final D namedObject) {
    return objects.remove(namedObject);
  }

  List<D> values() {
    return getObjects();
  }
}
