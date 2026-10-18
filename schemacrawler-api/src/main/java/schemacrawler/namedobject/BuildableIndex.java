/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import java.util.Optional;
import schemacrawler.schema.NamedObject;

public interface BuildableIndex<N extends NamedObject> {

  /**
   * Adds or replaces an object by its complete key.
   *
   * @param namedObject Object to add
   * @return Whether the object was added; false if the object is null
   * @throws NullPointerException if the object's key is null
   */
  boolean add(N namedObject);

  /**
   * Removes and returns the object mapped to the supplied object's complete key.
   *
   * @param namedObject Object whose complete key identifies the mapping to remove
   * @return The removed object, if present; empty if the object is null or absent
   */
  Optional<N> remove(N namedObject);
}
