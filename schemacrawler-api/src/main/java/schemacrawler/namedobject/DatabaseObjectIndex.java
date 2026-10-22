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
 * <p>Schema-scoped enumeration filters stored keys before sorting matching objects. Keys must be
 * stable hierarchical keys beginning with the schema key. Ordinary schema/name lookup uses the
 * complete key formed from the schema key and ordinary name; overload-aware indexes scan for that
 * prefix and select the natural minimum. Membership mutability and thread safety depend on the
 * implementation.
 *
 * <p>Schema/name lookup rejects null schemas and null or blank names. Inherited full-name lookup
 * also rejects null or blank names. Complete-key lookup does not apply name validation.
 *
 * @param <D> Database-object type
 */
public sealed interface DatabaseObjectIndex<D extends DatabaseObject> extends NamedObjectIndex<D>
    permits AbstractDatabaseObjectIndex, BuildableDatabaseObjectIndex, DependantObjectIndex {

  /**
   * Gets a detached, unmodifiable list of objects for a schema key in natural order. The operation
   * scans the index and sorts the matching objects.
   *
   * @param schema Schema whose structured key prefix must match
   * @return Naturally ordered objects for the schema, or an empty list if the schema is null
   */
  List<D> getObjects(Schema schema);

  /**
   * Gets detached, unmodifiable objects matching a schema key and exact ordinary name in natural
   * order. Ordinary indexes return at most one object; overload-aware indexes return all matches.
   *
   * @param schema Schema whose structured key must match
   * @param name Exact object name
   * @return Naturally ordered matches; empty if the schema is null, the name is null or blank, or
   *     no object matches
   */
  List<D> getObjects(Schema schema, String name);

  /**
   * Checks whether the index contains any objects for a schema key, without sorting or
   * natural-order comparisons.
   *
   * @param schema Schema whose structured key prefix must match
   * @return Whether any objects match; false if the schema is null
   */
  boolean hasObjects(Schema schema);

  /**
   * Looks up a database object by a schema key and exact object name.
   *
   * <p>Ordinary indexes form a complete key from the schema key and ordinary name. Overload-aware
   * indexes select the natural minimum among matching key prefixes; comparison ties have
   * unspecified selection. Indexed database objects are expected to have non-null schemas.
   *
   * @param schema Schema whose structured key must match
   * @param name Exact object name
   * @return The selected match, if present; empty if the schema is null, the name is null or blank,
   *     or no object matches
   */
  Optional<D> lookup(Schema schema, String name);
}
