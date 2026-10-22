/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import java.util.Optional;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.DependantObject;

/**
 * Adds parent/name lookup for dependent database objects.
 *
 * <p>Parent/name lookup uses an exact key formed by appending the child name as one part of the
 * parent's complete key. It does not scan or sort objects, and rejects null or blank child names.
 * The inherited schema/name overload remains a separate exact-key operation, not a prefix search
 * for the parent's name. This interface does not specify mutability or thread safety.
 *
 * @param <DO> Dependent-object type
 * @param <D> Parent database-object type
 */
public sealed interface DependantObjectIndex<
        DO extends DependantObject<D>, D extends DatabaseObject>
    extends DatabaseObjectIndex<DO> permits BuildableDependantObjectIndex {

  /**
   * Looks up a dependent object by its parent's complete key and exact child name.
   *
   * @param parentObject Parent database object
   * @param name Exact nonblank dependent-object name
   * @return Dependent object if found; empty if the parent is null, the name is null or blank, or
   *     no object matches
   * @throws NullPointerException if the non-null parent's key is null
   */
  Optional<DO> lookup(D parentObject, String name);
}
