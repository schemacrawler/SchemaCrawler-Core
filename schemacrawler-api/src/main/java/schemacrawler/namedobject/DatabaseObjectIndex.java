/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import java.util.List;
import java.util.Optional;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.Schema;

/**
 * Adds schema-scoped enumeration and ordinary-name lookup to a named-object index.
 *
 * <p>The interface does not specify whether membership is immutable or thread-safe. Values are
 * detached, unmodifiable lists in natural order and sorting them takes O(n log n) comparisons.
 * Snapshots are detached and unmodifiable, take O(n) time, and have unspecified iteration order.
 * Elements are shared references.
 *
 * @param <D> Database-object type
 */
public interface DatabaseObjectIndex<D extends DatabaseObject> extends NamedObjectIndex<D> {

  /**
   * Gets a detached, unmodifiable list of objects for an equal schema in natural order. The
   * operation scans the index and sorts the matching objects.
   *
   * @param schema Schema to match using {@link Object#equals(Object)}
   * @return Naturally ordered objects for the schema, or an empty list if the schema is null
   */
  List<D> getObjects(Schema schema);

  /**
   * Looks up a database object by an equal schema and exact object name.
   *
   * <p>This lookup is independent of the object's complete membership key. Blank names are matched
   * literally. Implementations select the natural-order first matching object; natural comparison
   * ties have unspecified selection, and this operation does not identify a unique overload.
   * Indexed database objects are expected to have non-null schemas.
   *
   * @param schema Schema to match using {@link Object#equals(Object)}
   * @param name Exact object name
   * @return The selected matching object, if present; empty if a query argument is null
   */
  Optional<D> lookup(Schema schema, String name);
}
