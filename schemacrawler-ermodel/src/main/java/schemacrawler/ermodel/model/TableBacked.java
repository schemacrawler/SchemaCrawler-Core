/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.model;

import java.util.Collection;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;

public interface TableBacked extends DatabaseObjectBacked<Table> {

  Collection<Column> getAttributeColumns();

  Table getTable();
}
