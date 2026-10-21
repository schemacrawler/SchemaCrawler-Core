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
 * <p>Schema-scoped operations scan an unsorted snapshot before sorting only the matching objects.
 * Matching uses schema equality and exact, case-sensitive names; indexed objects must have non-null
 * schemas. Membership mutability and thread safety depend on the implementation.
 *
 * <p>Both schema/name lookup and inherited full-name lookup reject null or blank names.
 * Complete-key lookup does not apply this name validation.
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
   * Gets a detached, unmodifiable list matching an equal schema and exact ordinary name in natural
   * order, including all overloads.
   *
   * @param schema Schema to match using {@link Object#equals(Object)}
   * @param name Exact object name
   * @return Naturally ordered matches; empty if the schema is null or the name is null or blank
   */
  List<D> getObjects(Schema schema, String name);

  /**
   * Looks up a database object by an equal schema and exact object name.
   *
   * <p>This lookup is independent of the object's complete membership key. Blank names are
   * rejected. Implementations select the natural-order first matching object; natural comparison
   * ties have unspecified selection, and this operation does not identify a unique overload.
   * Indexed database objects are expected to have non-null schemas.
   *
   * @param schema Schema to match using {@link Object#equals(Object)}
   * @param name Exact object name
   * @return The selected match, if present; empty if the schema is null, the name is null or blank,
   *     or no object matches
   */
  Optional<D> lookup(Schema schema, String name);
}
