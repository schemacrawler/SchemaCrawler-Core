/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.associations;

import static java.util.Objects.requireNonNull;
import static schemacrawler.utility.MetaDataUtility.isPartial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.KeyColumn;
import schemacrawler.schema.Table;
import us.fatehi.utility.string.StringFormat;

/**
 * Analyzes tables to infer implicit associations using naming heuristics.
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
public final class ImplicitAssociationAnalyzer {

  private static final Logger LOGGER =
      Logger.getLogger(ImplicitAssociationAnalyzer.class.getName());

  private final TableMatchKeys tableMatchKeys;
  private final Predicate<ColumnReference> implicitAssociationRule;

  ImplicitAssociationAnalyzer(
      final TableMatchKeys matchKeys, final Predicate<ColumnReference> implicitAssociationRule) {
    tableMatchKeys = requireNonNull(matchKeys, "No table match keys provided");
    this.implicitAssociationRule = requireNonNull(implicitAssociationRule, "No rules provided");
  }

  public Collection<ColumnReference> analyzeTables() {
    if (tableMatchKeys.getTables().size() < 2) {
      return List.of();
    }

    LOGGER.log(Level.INFO, "Finding implicit associations");

    final List<ColumnReference> proposedReferences = new ArrayList<>();

    final List<Table> tables = tableMatchKeys.getTables();
    final ColumnMatchKeys columnMatchKeys = new ColumnMatchKeys(tables);

    if (LOGGER.isLoggable(Level.FINER)) {
      LOGGER.log(Level.FINER, new StringFormat("Column match keys <%s>", columnMatchKeys));
      LOGGER.log(Level.FINER, new StringFormat("Table match keys <%s>", tableMatchKeys));
    }
    for (final Table table : tables) {
      final TableCandidateKeys candidateKeys = new TableCandidateKeys(table);
      LOGGER.log(Level.FINER, new StringFormat("Table candidate keys <%s>", candidateKeys));
      for (final KeyColumn candidateKey : candidateKeys.getCandidateKeys()) {
        final Set<String> fkColumnMatchKeys = new HashSet<>();
        // Look for all columns matching this table match key
        if (!candidateKey.isPartial()
            && candidateKey.isPartOfPrimaryKey()
            && tableMatchKeys.containsKey(table)) {
          fkColumnMatchKeys.addAll(tableMatchKeys.get(table));
        }
        // Look for all columns matching this column match key
        if (columnMatchKeys.containsKey(candidateKey)) {
          fkColumnMatchKeys.addAll(columnMatchKeys.get(candidateKey));
        }

        final Set<Column> fkColumns = new HashSet<>();
        for (final String fkColumnMatchKey : fkColumnMatchKeys) {
          if (columnMatchKeys.containsKey(fkColumnMatchKey)) {
            fkColumns.addAll(columnMatchKeys.get(fkColumnMatchKey));
          }
        }

        addProposedAssociations(fkColumns, candidateKey, proposedReferences);
      }
    }

    return proposedReferences;
  }

  private void addProposedAssociations(
      final Set<Column> fkColumns,
      final KeyColumn candidateKey,
      final List<ColumnReference> proposedReferences) {
    for (final Column fkColumn : fkColumns) {
      if (!isValid(fkColumn, candidateKey)) {
        continue;
      }
      final ProposedAssociation proposedAssociation =
          new ProposedAssociation(fkColumn, candidateKey);
      if (implicitAssociationRule.test(proposedAssociation)) {
        LOGGER.log(
            Level.FINE, new StringFormat("Found implicit association <%s>", proposedAssociation));
        proposedReferences.add(proposedAssociation);
      }
    }
  }

  /**
   * Validates a proposed association based on identity, partiality, and standard data type
   * compatibility.
   *
   * @return true if the association should be considered for matching rules
   */
  private boolean isValid(final Column fkColumn, final Column pkColumn) {

    if (fkColumn == null
        || pkColumn == null
        || pkColumn.equals(fkColumn)
        || fkColumn.isPartOfForeignKey()) {
      return false;
    }

    final boolean isPkColumnPartial = isPartial(pkColumn);
    final boolean isFkColumnPartial = isPartial(fkColumn);
    if (isFkColumnPartial && isPkColumnPartial
        || !pkColumn.isColumnDataTypeKnown()
        || !fkColumn.isColumnDataTypeKnown()) {
      return false;
    }

    final ColumnDataType fkColumnType = fkColumn.getColumnDataType();
    final ColumnDataType pkColumnType = pkColumn.getColumnDataType();
    final boolean isValid =
        fkColumnType.getStandardTypeName().equals(pkColumnType.getStandardTypeName());
    return isValid;
  }
}
