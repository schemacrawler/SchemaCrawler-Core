/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.utility;

import static schemacrawler.loader.utility.TableRowCountsUtility.getRowCount;
import static schemacrawler.loader.utility.TableRowCountsUtility.hasRowCount;

import java.util.List;
import schemacrawler.loader.utility.TableRowCountsUtility;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public class TableImportanceUtility {

  public static TableCounts tableCountsfrom(final Table table) {
    if (table == null) {
      return null;
    }

    final Long rowCount =
        TableRowCountsUtility.hasRowCount(table) ? TableRowCountsUtility.getRowCount(table) : null;
    final List<Column> columns = table.getColumns();
    final TableCounts tableCounts =
        new TableCounts(
            (int) columns.stream().filter(Column::isAttribute).count(),
            columns.size(),
            table.getReferencedTables().size(),
            table.getIndexes().size(),
            table.getTriggers().size(),
            rowCount);

    return tableCounts;
  }

  public static TableTraits tableTraitsfrom(final Table table) {
    if (table == null) {
      return new TableTraits();
    }

    final TableTraits tableTraits =
        new TableTraits(
            !table.hasPrimaryKey(),
            !table.hasForeignKeys(),
            !table.hasIndexes(),
            table.isSelfReferencing(),
            table.hasTriggers(),
            hasRowCount(table) && getRowCount(table) == 0,
            EntityModelType.from(table));

    return tableTraits;
  }

  private TableImportanceUtility() {
    // Prevent instantiation
  }
}
