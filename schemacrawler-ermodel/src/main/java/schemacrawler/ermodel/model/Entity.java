/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.model;

import schemacrawler.schema.TypedObject;

/** Conceptual entity backed by a SchemaCrawler table. */
public interface Entity extends TableBacked, TypedObject<EntityType> {}
