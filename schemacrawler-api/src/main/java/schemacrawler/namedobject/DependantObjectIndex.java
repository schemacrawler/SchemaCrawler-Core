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
 * parent's complete key. It does not scan or sort objects, and accepts blank child names. The
 * inherited schema/name overload remains a separate operation with blank-name rejection and
 * natural-order overload selection. This interface does not specify mutability or thread safety.
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
   * @param name Exact dependent-object name; may be blank
   * @return Dependent object if found; empty if either argument is null or no object matches
   * @throws NullPointerException if the non-null parent's key is null
   */
  Optional<DO> lookup(D parentObject, String name);
}
