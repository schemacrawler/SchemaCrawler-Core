/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import static us.fatehi.utility.Utility.isBlank;

import java.io.Serial;
import java.util.Optional;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.DependantObject;

/**
 * Provides a concurrent mutable index of dependent database objects with parent/name lookup.
 *
 * <p>Inherits the mutation, snapshot, and thread-safety behavior documented by {@link
 * AbstractConcurrentIndex}. Parent/name lookup follows {@link DependantObjectIndex} and uses a
 * single keyed read without sorting. Inherited schema/name lookup remains a separate operation.
 *
 * @param <DO> Dependent-object type
 * @param <D> Parent database-object type
 */
final class ConcurrentDependantObjectIndex<DO extends DependantObject<D>, D extends DatabaseObject>
    extends AbstractConcurrentIndex<DO> implements BuildableDependantObjectIndex<DO, D> {

  @Serial private static final long serialVersionUID = -1089958942591485649L;

  /** Creates an empty concurrent index of dependent objects. */
  ConcurrentDependantObjectIndex() {}

  /** {@inheritDoc} */
  @Override
  public Optional<DO> lookup(final D object, final String name) {
    if (object == null || isBlank(name)) {
      return Optional.empty();
    }
    return lookup(object.key().with(name));
  }
}
