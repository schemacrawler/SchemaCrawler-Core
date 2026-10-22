/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import java.io.Serial;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import schemacrawler.schema.DatabaseObject;

/**
 * Shares concurrent mutable storage and keyed mutation between database-object indexes.
 *
 * <p>Adding an object replaces any existing object with its key. Individual map operations are
 * thread-safe, and snapshots are weakly consistent during concurrent membership changes. A snapshot
 * is not an atomic point-in-time view. Schema/name lookup filters the snapshot and selects the
 * natural-order first match; comparison ties have unspecified selection. Compound operations,
 * cross-index transactions, and object state are not synchronized. Objects are shared references;
 * complete keys must remain stable, and comparison state must remain stable while values are
 * sorted. Returned lists are detached and unmodifiable. The index is serializable when its objects
 * are serializable.
 *
 * @param <D> Database-object type
 */
public abstract sealed class AbstractConcurrentIndex<D extends DatabaseObject>
    extends AbstractDatabaseObjectIndex<D> implements BuildableDatabaseObjectIndex<D>
    permits ConcurrentDatabaseObjectIndex, ConcurrentRoutineIndex, ConcurrentDependantObjectIndex {

  @Serial private static final long serialVersionUID = -1089958942591485649L;

  /** Creates an empty concurrent index. */
  public AbstractConcurrentIndex() {
    super(new ConcurrentHashMap<>());
  }

  /** {@inheritDoc} */
  @Override
  public boolean add(final D object) {
    if (object == null) {
      return false;
    }
    internalMap().put(object.key(), object);
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public Optional<D> remove(final D object) {
    if (object == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(internalMap().remove(object.key()));
  }
}
