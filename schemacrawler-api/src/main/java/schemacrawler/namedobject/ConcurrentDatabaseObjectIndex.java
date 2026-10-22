/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import java.io.Serial;
import schemacrawler.schema.DatabaseObject;

/**
 * Provides a concurrent mutable index of database objects keyed by each object's complete key.
 *
 * <p>Inherits the mutation, snapshot, and thread-safety behavior documented by {@link
 * AbstractConcurrentIndex}. Schema-scoped reads follow {@link DatabaseObjectIndex}.
 *
 * @param <D> Database-object type
 */
public final class ConcurrentDatabaseObjectIndex<D extends DatabaseObject>
    extends AbstractConcurrentIndex<D> {

  @Serial private static final long serialVersionUID = -1089958942591485649L;

  /** Creates an empty concurrent index. */
  public ConcurrentDatabaseObjectIndex() {}
}
