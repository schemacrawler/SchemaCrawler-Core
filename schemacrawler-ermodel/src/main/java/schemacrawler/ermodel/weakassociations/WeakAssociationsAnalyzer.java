/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.weakassociations;

import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.Table;
import us.fatehi.utility.string.StringFormat;

/**
 * Analyzes tables to infer weak associations using naming heuristics.
 *
 * <p>Flow:
 *
 * <ol>
 *   <li>Collect candidate key columns (primary key or single-column unique index).
 *   <li>Generate match keys by normalizing column names and stripping {@code _id} suffixes.
 *   <li>Find candidate foreign key columns that share match keys with primary key columns.
 *   <li>Validate proposed pairs and apply configured match rules.
 * </ol>
 */
public final class WeakAssociationsAnalyzer {

  private static final Logger LOGGER = Logger.getLogger(WeakAssociationsAnalyzer.class.getName());

  static final Pattern ID_PATTERN = Pattern.compile("_?(id|key|keyid)$", CASE_INSENSITIVE);

  private final TableMatchKeys tableMatchKeys;
  private final Predicate<ColumnReference> weakAssociationRule;
  private final Collection<ColumnReference> weakAssociations;

  public WeakAssociationsAnalyzer(
      final TableMatchKeys matchKeys, final Predicate<ColumnReference> weakAssociationRule) {
    tableMatchKeys = requireNonNull(matchKeys, "No table match keys provided");

    this.weakAssociationRule = requireNonNull(weakAssociationRule, "No rules provided");

    weakAssociations = new ArrayList<>();
  }

  public Collection<ColumnReference> analyzeTables() {
    if (tableMatchKeys.getTables().size() < 2) {
      return Collections.emptySet();
    }
    findWeakAssociations();
    return weakAssociations;
  }

  private void findWeakAssociations() {
    LOGGER.log(Level.INFO, "Finding weak associations");
    final List<Table> tables = tableMatchKeys.getTables();
    final ColumnMatchKeys columnMatchKeys = new ColumnMatchKeys(tables);

    if (LOGGER.isLoggable(Level.FINER)) {
      LOGGER.log(Level.FINER, new StringFormat("Column match keys <%s>", columnMatchKeys));
      LOGGER.log(Level.FINER, new StringFormat("Table match keys <%s>", tableMatchKeys));
    }
    for (final Table table : tables) {
      final TableCandidateKeys tableCandidateKeys = new TableCandidateKeys(table);
      LOGGER.log(Level.FINER, new StringFormat("Table candidate keys <%s>", tableCandidateKeys));
      for (final Column pkColumn : tableCandidateKeys) {
        final Set<String> fkColumnMatchKeys = new HashSet<>();
        // Look for all columns matching this table match key
        if (pkColumn.isPartOfPrimaryKey() && tableMatchKeys.containsKey(table)) {
          fkColumnMatchKeys.addAll(tableMatchKeys.get(table));
        }
        // Look for all columns matching this column match key
        if (columnMatchKeys.containsKey(pkColumn)) {
          fkColumnMatchKeys.addAll(columnMatchKeys.get(pkColumn));
        }

        final Set<Column> fkColumns = new HashSet<>();
        for (final String fkColumnMatchKey : fkColumnMatchKeys) {
          if (columnMatchKeys.containsKey(fkColumnMatchKey)) {
            fkColumns.addAll(columnMatchKeys.get(fkColumnMatchKey));
          }
        }

        for (final Column fkColumn : fkColumns) {
          final WeakAssociationColumnReference proposedWeakAssociation =
              new WeakAssociationColumnReference(fkColumn, pkColumn);
          if (proposedWeakAssociation.isValid()
              && weakAssociationRule.test(proposedWeakAssociation)) {
            LOGGER.log(
                Level.FINE,
                new StringFormat("Found weak association <%s>", proposedWeakAssociation));
            weakAssociations.add(proposedWeakAssociation);
          }
        }
      }
    }
  }
}
