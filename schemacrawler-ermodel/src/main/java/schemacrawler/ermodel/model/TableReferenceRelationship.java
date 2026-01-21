/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.model;

import schemacrawler.schema.TableReference;

/** Conceptual relationship via foreign keys. */
public interface TableReferenceRelationship extends Relationship {

  TableReference getTableReference();
}
