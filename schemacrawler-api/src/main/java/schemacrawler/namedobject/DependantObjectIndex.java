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
import schemacrawler.schema.NamedObject;

/**
 * Adds parent/name lookup for dependent database objects.
 *
 * <p>Schema parents use schema/name matching, including blank-name rejection and natural-order
 * overload selection. Other parents use an exact key formed by appending the child name to the
 * parent's key. This interface does not specify mutability or thread safety.
 *
 * @param <D> Database-object type
 */
public interface DependantObjectIndex<D extends DatabaseObject> extends DatabaseObjectIndex<D> {

  /**
   * Allows lookup of dependent database objects from the parent database object.
   *
   * @param namedObject Parent named object (or schema)
   * @param name Dependent object name
   * @return Dependent object if found; empty if either argument is null or no object matches
   */
  Optional<D> lookup(NamedObject namedObject, String name);
}
