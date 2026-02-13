/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.associations;

import static java.util.Objects.requireNonNull;
import static schemacrawler.ermodel.associations.ImplicitAssociationsUtility.normalizeColumnName;

import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.Table;
import us.fatehi.utility.string.StringFormat;

/**
 * Matches weak associations for extension tables that share a normalized primary key name with the
 * referenced table. Extension tables are tables where the foreign key column is also a primary key
 * or part of a unique index, representing a 1-to-1 or 1-to-0..1 relationship.
 *
 * <p>This rule is optionally enabled via the {@code infer-extension-tables} option and requires the
 * foreign key column to be unique in the extension table.
 */
final class ExtensionTableMatcher implements Predicate<ColumnReference> {

  private static final Logger LOGGER = Logger.getLogger(ExtensionTableMatcher.class.getName());

  private final TableMatchKeys tableMatchKeys;

  public ExtensionTableMatcher(final TableMatchKeys tableMatchKeys) {
    this.tableMatchKeys = requireNonNull(tableMatchKeys, "No table match keys provided");
  }

  @Override
  public boolean test(final ColumnReference proposedWeakAssociation) {

    if (proposedWeakAssociation == null) {
      return false;
    }

    final Column fkColumn = proposedWeakAssociation.getForeignKeyColumn();
    final Column pkColumn = proposedWeakAssociation.getPrimaryKeyColumn();

    final String pkColumnName = normalizeColumnName(pkColumn);
    final String fkColumnName = normalizeColumnName(fkColumn);

    if (pkColumnName.equals(fkColumnName)) {
      final Table pkTable = pkColumn.getParent();
      final boolean fkIsUnique = fkColumn.isPartOfPrimaryKey() || fkColumn.isPartOfUniqueIndex();
      final boolean matches = fkIsUnique && tableMatchKeys.isTopRankedCandidate(pkTable);
      if (matches) {
        LOGGER.log(
            Level.FINE,
            new StringFormat("ExtensionTableMatcher proposed <%s>", proposedWeakAssociation));
      }
      return matches;
    }
    return false;
  }
}
