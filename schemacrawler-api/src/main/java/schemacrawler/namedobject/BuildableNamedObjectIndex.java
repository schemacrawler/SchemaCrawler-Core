/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import schemacrawler.schema.NamedObject;

/**
 * Combines dependent-object reads with optional keyed membership mutation.
 *
 * <p>Parent/name lookup follows {@link DependantObjectIndex}; mutations follow {@link
 * BuildableIndex}. The concrete implementation specifies membership mutability and thread safety.
 *
 * @param <DO> Dependent-object type
 * @param <D> Parent database-object type
 */
public sealed interface BuildableNamedObjectIndex<N extends NamedObject>
    extends NamedObjectIndex<N>, BuildableIndex<N> permits MapBackedNamedObjectIndex {}
