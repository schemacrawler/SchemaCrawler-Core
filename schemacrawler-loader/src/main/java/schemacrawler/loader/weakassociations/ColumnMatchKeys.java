/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.weakassociations;

import static java.util.Objects.requireNonNull;
import static schemacrawler.loader.weakassociations.WeakAssociationsAnalyzer.ID_PATTERN;
import static us.fatehi.utility.Utility.isBlank;

import java.util.List;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;
import us.fatehi.utility.Multimap;

/**
 * Maintains match keys for columns by normalizing names (lowercase) and removing a trailing id
 * suffix. The match keys are used to group likely foreign key columns and primary key columns.
 */
final class ColumnMatchKeys {

  private final Multimap<String, Column> columnsForMatchKey;
  private final Multimap<Column, String> matchKeysForColumn;

  ColumnMatchKeys(final List<Table> tables) {
    requireNonNull(tables, "No tables provided");
    columnsForMatchKey = new Multimap<>();
    matchKeysForColumn = new Multimap<>();

    for (final Table table : tables) {
      mapColumnNameMatches(table);
    }
  }

  public boolean containsKey(final Column column) {
    return matchKeysForColumn.containsKey(column);
  }

  public boolean containsKey(final String columnKey) {
    return columnsForMatchKey.containsKey(columnKey);
  }

  public List<String> get(final Column column) {
    return matchKeysForColumn.get(column);
  }

  public List<Column> get(final String matchKey) {
    return columnsForMatchKey.get(matchKey);
  }

  @Override
  public String toString() {
    return columnsForMatchKey.toString();
  }

  private void mapColumnNameMatches(final Table table) {
    for (final Column column : table.getColumns()) {
      final String columnName = column.getName().toLowerCase();
      final String matchColumnName = ID_PATTERN.matcher(columnName).replaceAll("");
      if (!isBlank(matchColumnName)) {
        columnsForMatchKey.add(matchColumnName, column);
        matchKeysForColumn.add(column, matchColumnName);
      }
    }
  }
}
