/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.weakassociations;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Table;
import us.fatehi.utility.Multimap;

/**
 * Maintains a mapping between tables and their derived match keys.
 *
 * <p>A ranking system is used where tables with more incoming references are preferred as "targets"
 * for a match key. If multiple tables share a match key, only those with the highest incoming
 * reference count for that specific key are retained as valid candidates.
 */
final class TableMatchKeys {

  private static PrefixMatches analyzeTables(final List<Table> tables) {
    final List<String> tableNames = new ArrayList<>();
    for (final Table table : tables) {
      tableNames.add(table.getName());
    }
    return new PrefixMatches(tableNames, "_");
  }

  private final Multimap<Table, String> matchKeysForTable;
  private final Multimap<String, Table> tablesForMatchKey;

  TableMatchKeys(final List<Table> tables) {
    requireNonNull(tables, "No tables provided");

    matchKeysForTable = new Multimap<>();
    tablesForMatchKey = new Multimap<>();
    final Map<Table, Integer> incomingReferenceCounts = new HashMap<>();

    for (final Table table : tables) {
      for (final ForeignKey foreignKey : table.getForeignKeys()) {
        final Table pkTable = foreignKey.getPrimaryKeyTable();
        if (pkTable != null) {
          incomingReferenceCounts.merge(pkTable, 1, Integer::sum);
        }
      }
    }

    final PrefixMatches prefixMatches = analyzeTables(tables);
    final Map<String, Integer> maxCountsForKey = new HashMap<>();
    for (final Table table : tables) {
      final List<String> tableMatchKeys = prefixMatches.get(table.getName());
      if (tableMatchKeys == null) {
        continue;
      }
      final int tableCount = incomingReferenceCounts.getOrDefault(table, 0);
      for (final String matchKey : tableMatchKeys) {
        maxCountsForKey.merge(matchKey, tableCount, Math::max);
      }
    }

    for (final Table table : tables) {
      final List<String> tableMatchKeys = prefixMatches.get(table.getName());
      if (tableMatchKeys == null) {
        continue;
      }
      final int tableCount = incomingReferenceCounts.getOrDefault(table, 0);
      for (final String matchKey : tableMatchKeys) {
        if (tableCount == maxCountsForKey.getOrDefault(matchKey, 0)) {
          matchKeysForTable.add(table, matchKey);
          tablesForMatchKey.add(matchKey, table);
        }
      }
    }
  }

  public boolean containsKey(final String matchKey) {
    return tablesForMatchKey.containsKey(matchKey);
  }

  public boolean containsKey(final Table table) {
    return matchKeysForTable.containsKey(table);
  }

  public List<Table> get(final String matchKey) {
    return tablesForMatchKey.get(matchKey);
  }

  public List<String> get(final Table table) {
    return matchKeysForTable.get(table);
  }

  @Override
  public String toString() {
    return tablesForMatchKey.toString();
  }

  boolean isTopRankedCandidate(final Table table) {
    final List<String> tableMatchKeys = matchKeysForTable.get(table);
    if (tableMatchKeys == null || tableMatchKeys.isEmpty()) {
      return true;
    }

    boolean hasCandidates = false;
    for (final String matchKey : tableMatchKeys) {
      final List<Table> matchTables = tablesForMatchKey.get(matchKey);
      if (matchTables != null && !matchTables.isEmpty()) {
        hasCandidates = true;
        if (matchTables.contains(table)) {
          return true;
        }
      }
    }

    return !hasCandidates;
  }
}
