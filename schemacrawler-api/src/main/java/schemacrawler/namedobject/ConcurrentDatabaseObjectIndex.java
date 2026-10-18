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
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Schema;

/**
 * Provides a concurrent mutable index of named objects keyed by each object's complete key.
 *
 * <p>Adding an object replaces any existing object with its key. Individual map operations are
 * thread-safe, and snapshots are weakly consistent during concurrent membership changes. A snapshot
 * is not an atomic point-in-time view. Schema/name lookup filters the snapshot and selects the
 * natural-order first match; comparison ties have unspecified selection. Compound operations,
 * cross-index transactions, and object state are not synchronized. Objects are shared references;
 * comparison and identity keys must remain stable, and comparison state must remain stable while
 * values are sorted. Returned lists are detached and unmodifiable. The index is serializable when
 * its objects are serializable.
 *
 * @param <D> Named-object type
 */
public final class ConcurrentDatabaseObjectIndex<D extends DatabaseObject>
    extends AbstractDatabaseObjectIndex<D> implements BuildableIndex<D> {

  @Serial private static final long serialVersionUID = -1089958942591485649L;

  /** Creates an empty concurrent index. */
  public ConcurrentDatabaseObjectIndex() {
    super(new ConcurrentHashMap<>());
  }

  /** {@inheritDoc} */
  @Override
  public boolean add(final D namedObject) {
    if (namedObject == null) {
      return false;
    }
    internalMap().put(namedObject.key(), namedObject);
    return true;
  }

  /**
   * Allows lookup of dependent database objects from the parent database object.
   *
   * @param namedObject Parent database object (or schema)
   * @param name Dependent object name
   * @return Dependent object if found; empty if either argument is null or no object matches
   */
  public Optional<D> lookup(final NamedObject namedObject, final String name) {
    if (namedObject == null || name == null) {
      return Optional.empty();
    }
    if (namedObject instanceof final Schema schema) {
      return lookup(schema, name);
    }
    return lookup(namedObject.key().with(name));
  }

  /** {@inheritDoc} */
  @Override
  public Optional<D> remove(final D namedObject) {
    if (namedObject == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(internalMap().remove(namedObject.key()));
  }
}
