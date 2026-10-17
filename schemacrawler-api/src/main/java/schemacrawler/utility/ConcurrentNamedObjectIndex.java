/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.utility;

import java.io.Serial;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import schemacrawler.schema.NamedObject;

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
 * @param <N> Named-object type
 */
public final class ConcurrentNamedObjectIndex<N extends NamedObject>
    extends AbstractNamedObjectIndex<N> {

  @Serial private static final long serialVersionUID = -1089958942591485649L;

  /** Creates an empty concurrent index. */
  public ConcurrentNamedObjectIndex() {
    super(new ConcurrentHashMap<>());
  }

  /**
   * Adds or replaces an object by its complete key.
   *
   * @param namedObject Object to add
   * @return Always true after a successful insertion
   * @throws NullPointerException if the object or its key is null
   */
  public boolean add(final N namedObject) {
    if (namedObject == null) {
      return false;
    }
    getObjects().put(namedObject.key(), namedObject);
    return true;
  }

  /**
   * Removes and returns the object mapped to a complete key.
   *
   * @param key Complete lookup key
   * @return The removed object, if present
   * @throws NullPointerException if the key is null
   */
  public Optional<N> remove(final N namedObject) {
    if (namedObject == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(getObjects().remove(namedObject.key()));
  }
}
