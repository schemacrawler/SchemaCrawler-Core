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
 * Combines dependent-object reads with optional keyed membership mutation.
 *
 * <p>The lookup parent need not have the indexed child's type. Inherits mutation behavior from
 * {@link BuildableIndex}, without adding mutability or thread-safety guarantees.
 *
 * @param <D> Database-object type
 */
public interface BuildableDependantObjectIndex<D extends DatabaseObject>
    extends DependantObjectIndex<D>, BuildableDatabaseObjectIndex<D> {}
