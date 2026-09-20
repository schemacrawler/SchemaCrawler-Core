/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static java.util.Objects.requireNonNull;

import schemacrawler.schema.Table;
import schemacrawler.schema.TableTypes;
import schemacrawler.schemacrawler.LimitOptions;

class TableTypesFilter implements NamedObjectFilter<Table> {

  private final TableTypes tableTypes;

  TableTypesFilter(final LimitOptions options) {
    requireNonNull(options, "No limit options provided");
    tableTypes = options.tableTypes();
  }

  /**
   * Check for table limiting rules.
   *
   * @param table Table to check
   * @return Whether the table should be included
   */
  @Override
  public boolean test(final Table table) {
    return tableTypes.lookupTableType(table.getTableType().getName()).isPresent();
  }
}
