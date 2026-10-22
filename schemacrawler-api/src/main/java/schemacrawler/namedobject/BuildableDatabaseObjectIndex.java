/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import schemacrawler.schema.DatabaseObject;

/**
 * Combines database-object reads with optional keyed membership mutation.
 *
 * <p>Reads follow {@link DatabaseObjectIndex}; mutations follow {@link BuildableIndex}. The
 * concrete implementation specifies membership mutability and thread safety.
 *
 * @param <D> Database-object type
 */
public sealed interface BuildableDatabaseObjectIndex<D extends DatabaseObject>
    extends DatabaseObjectIndex<D>, BuildableIndex<D>
    permits AbstractConcurrentIndex, BuildableDependantObjectIndex {}
