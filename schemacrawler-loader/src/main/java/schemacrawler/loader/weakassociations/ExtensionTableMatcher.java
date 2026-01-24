/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.weakassociations;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;
import us.fatehi.utility.string.StringFormat;

/**
 * Matches weak associations for extension tables.
 *
 * <p>An extension table relationship (1-to-1 or 1-to-0..1) is inferred when:
 * <ul>
 *   <li>The {@code infer-extension-tables} option is enabled.
 *   <li>The foreign key column shares a normalized name with the referenced table's primary key.
 *   <li>The foreign key column is unique (part of its own primary key or a unique index).
 *   <li>The referenced table is considered a top-ranked candidate for the match key.
 * </ul>
 */
public final class ExtensionTableMatcher implements Predicate<ProposedWeakAssociation> {

  private static final Logger LOGGER = Logger.getLogger(ExtensionTableMatcher.class.getName());

  private final boolean inferExtensionTables;
  private final TableMatchKeys matchKeys;

  public ExtensionTableMatcher(final boolean inferExtensionTables) {
    this(inferExtensionTables, Collections.emptyList());
  }

  public ExtensionTableMatcher(final boolean inferExtensionTables, final Collection<Table> tables) {
    this.inferExtensionTables = inferExtensionTables;
    requireNonNull(tables, "No tables provided");
    matchKeys = new TableMatchKeys(List.copyOf(tables));
  }

  @Override
  public boolean test(final ProposedWeakAssociation proposedWeakAssociation) {

    if (!inferExtensionTables || proposedWeakAssociation == null) {
      return false;
    }

    final Column foreignKeyColumn = proposedWeakAssociation.getForeignKeyColumn();
    final Column primaryKeyColumn = proposedWeakAssociation.getPrimaryKeyColumn();

    final String pkColumnName = foreignKeyColumn.getName().replaceAll("[^\\p{L}\\d]", "").toLowerCase();
    final String fkColumnName =
        foreignKeyColumn.getName().replaceAll("[^\\p{L}\\d]", "").toLowerCase();
    if (pkColumnName.equals(fkColumnName)) {
      final Table pkTable = primaryKeyColumn.getParent();
      final boolean fkIsUnique =
          foreignKeyColumn.isPartOfPrimaryKey() || foreignKeyColumn.isPartOfUniqueIndex();
      final boolean matches = fkIsUnique && matchKeys.isTopRankedCandidate(pkTable);
      if (!matches) {
        return false;
      }
      LOGGER.log(
          Level.FINE,
          new StringFormat("ExtensionTableMatcher proposed <%s>", proposedWeakAssociation));
      return matches;
    }
    return false;
  }
}
