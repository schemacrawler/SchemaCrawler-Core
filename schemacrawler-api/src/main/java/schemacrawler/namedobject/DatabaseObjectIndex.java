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
 * <p>Schema-scoped enumeration scans an unsorted snapshot before sorting matching objects.
 * Schema/name lookup uses the complete key formed from the schema key and ordinary name.
 * Implementations for object types with overloads may override these operations to scan all
 * matching objects. Membership mutability and thread safety depend on the implementation.
 *
 * <p>Schema/name lookup rejects null arguments. Its default exact-key implementation may match a
 * blank name; specializations may define different name boundaries. Inherited full-name lookup
 * rejects null or blank names. Complete-key lookup does not apply name validation.
 *
 * @param <D> Database-object type
 */
public sealed interface DatabaseObjectIndex<D extends DatabaseObject> extends NamedObjectIndex<D>
    permits AbstractDatabaseObjectIndex, BuildableDatabaseObjectIndex, DependantObjectIndex {

  /**
   * Gets a detached, unmodifiable list of objects for an equal schema in natural order. The
   * operation scans the index and sorts the matching objects.
   *
   * @param schema Schema to match using {@link Object#equals(Object)}
   * @return Naturally ordered objects for the schema, or an empty list if the schema is null
   */
  List<D> getObjects(Schema schema);

  /**
   * Gets detached, unmodifiable objects matching an equal schema and exact ordinary name in natural
   * order. The default keyed implementation returns at most one object; implementations supporting
   * overloaded names may return all matches.
   *
   * @param schema Schema to match using {@link Object#equals(Object)}
   * @param name Exact object name
   * @return Naturally ordered matches; empty if either argument is null or no object matches
   */
  List<D> getObjects(Schema schema, String name);

  /**
   * Checks whether the index contains any objects for an equal schema, without sorting or
   * natural-order comparisons.
   *
   * @param schema Schema to match using {@link Object#equals(Object)}
   * @return Whether any objects match; false if the schema is null
   */
  boolean hasObjects(Schema schema);

  /**
   * Looks up a database object by an equal schema and exact object name.
   *
   * <p>The default keyed implementation forms a key from the schema key and ordinary name. Types
   * with overloaded names may override this operation to select a matching object. Indexed database
   * objects are expected to have non-null schemas.
   *
   * @param schema Schema to match using {@link Object#equals(Object)}
   * @param name Exact object name
   * @return The selected match, if present; empty if either argument is null or no object matches
   */
  Optional<D> lookup(Schema schema, String name);
}
