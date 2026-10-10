/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static java.util.Objects.requireNonNull;
import static schemacrawler.utility.MetaDataUtility.isPartial;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableRelationshipType;

/** Selects tables related to the original seeds within a bounded table collection. */
final class RelatedTableFilter implements NamedObjectFilter<Table> {

  private final Set<NamedObjectKey> seedTableKeys;
  private final Set<NamedObjectKey> parentTableKeys;
  private final Set<NamedObjectKey> childTableKeys;

  RelatedTableFilter(
      final Collection<Table> eligibleTables,
      final Collection<Table> seedTables,
      final int parentDepth,
      final int childDepth) {
    requireNonNull(eligibleTables, "No eligible tables provided");
    requireNonNull(seedTables, "No seed tables provided");

    final Set<NamedObjectKey> eligibleTableKeys = keys(eligibleTables);
    seedTableKeys = Set.copyOf(keys(seedTables));
    parentTableKeys =
        Set.copyOf(
            keys(
                includeRelatedTables(
                    seedTables, eligibleTableKeys, TableRelationshipType.parent, parentDepth)));
    childTableKeys =
        Set.copyOf(
            keys(
                includeRelatedTables(
                    seedTables, eligibleTableKeys, TableRelationshipType.child, childDepth)));
  }

  private static HashSet<NamedObjectKey> keys(final Collection<Table> tables) {
    final HashSet<NamedObjectKey> tableKeys = new HashSet<>();
    tables.stream().map(NamedObject::key).forEach(tableKeys::add);
    return tableKeys;
  }

  private static Set<Table> includeRelatedTables(
      final Collection<Table> seedTables,
      final Set<NamedObjectKey> eligibleTableKeys,
      final TableRelationshipType relationshipType,
      final int depth) {
    final Set<NamedObjectKey> visited = keys(seedTables);
    // Mark the seeds visited so cycles cannot add them back into the result
    final Set<Table> relatedTables = new LinkedHashSet<>();
    Set<Table> frontier = new LinkedHashSet<>(seedTables);
    // Each iteration follows one relationship hop from the current frontier
    for (int currentDepth = 0; currentDepth < depth; currentDepth++) {
      final Set<Table> nextFrontier = new LinkedHashSet<>();
      for (final Table table : frontier) {
        for (final Table relatedTable : table.getRelatedTables(relationshipType)) {
          // Only traverse eligible, complete tables, and add each table once
          if (relatedTable != null
              && eligibleTableKeys.contains(relatedTable.key())
              && !isPartial(relatedTable)
              && visited.add(relatedTable.key())) {
            nextFrontier.add(relatedTable);
            relatedTables.add(relatedTable);
          }
        }
      }
      frontier = nextFrontier;
    }
    return relatedTables;
  }

  @Override
  public boolean test(final Table table) {
    if (table == null) {
      return false;
    }
    final NamedObjectKey tableKey = table.key();
    return seedTableKeys.contains(tableKey)
        || parentTableKeys.contains(tableKey)
        || childTableKeys.contains(tableKey);
  }
}
