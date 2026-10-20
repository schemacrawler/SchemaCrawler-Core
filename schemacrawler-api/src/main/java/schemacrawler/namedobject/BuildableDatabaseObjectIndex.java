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
 * <p>Inherits mutation behavior from {@link BuildableIndex}, without adding mutability or
 * thread-safety guarantees.
 *
 * @param <D> Database-object type
 */
public interface BuildableDatabaseObjectIndex<D extends DatabaseObject>
    extends DatabaseObjectIndex<D>, BuildableIndex<D> {}
