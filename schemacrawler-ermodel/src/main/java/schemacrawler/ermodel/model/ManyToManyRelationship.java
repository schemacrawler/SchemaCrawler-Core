/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.model;

import schemacrawler.schema.Table;

/** Conceptual many-to-many pattern: A <-> B via bridge table. */
public interface ManyToManyRelationship extends TableBacked, Relationship {

  default Table getBridgeTable() {
    return getTable();
  }
}
