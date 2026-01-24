/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.weakassociations;

import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;

/**
 * Matches weak associations for extension tables that share a normalized primary key name with the
 * referenced table. This rule is optionally enabled via the {@code infer-extension-tables} option
 * and requires the foreign key column to be unique in the extension table.
 */
public final class ExtensionTableMatcher implements Predicate<ProposedWeakAssociation> {

  private static final Logger LOGGER = Logger.getLogger(ExtensionTableMatcher.class.getName());
  private final boolean inferExtensionTables;

  public ExtensionTableMatcher(final boolean inferExtensionTables) {
    this.inferExtensionTables = inferExtensionTables;
  }

  @Override
  public boolean test(final ProposedWeakAssociation proposedWeakAssociation) {

    if (!inferExtensionTables || (proposedWeakAssociation == null)) {
      return false;
    }

    final Column foreignKeyColumn = proposedWeakAssociation.getForeignKeyColumn();
    final Column primaryKeyColumn = proposedWeakAssociation.getPrimaryKeyColumn();

    final String pkColumnName =
        primaryKeyColumn.getName().replaceAll("[^\\p{L}\\{d}]", "").toLowerCase();
    final String fkColumnName =
        foreignKeyColumn.getName().replaceAll("[^\\p{L}\\{d}]", "").toLowerCase();
    if (pkColumnName.equals(fkColumnName)) {
      final Table pkTable = primaryKeyColumn.getParent();
      final Table fkTable = foreignKeyColumn.getParent();
      final boolean fkIsUnique =
          foreignKeyColumn.isPartOfPrimaryKey() || foreignKeyColumn.isPartOfUniqueIndex();
      final boolean pkTableSortsFirst = pkTable.compareTo(fkTable) < 0;
      final boolean matches = fkIsUnique && pkTableSortsFirst;
      if (matches && LOGGER.isLoggable(Level.FINER)) {
        LOGGER.log(
            Level.FINER,
            "Weak association rule matched: ExtensionTableMatcher for proposed association {0}",
            proposedWeakAssociation);
      }
      return matches;
    }
    return false;
  }
}
