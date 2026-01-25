/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.weakassociations;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
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

  private static final Pattern NOT_ALPHANUMERIC_PATTERN = Pattern.compile("[^\\p{L}\\d]");

  private final TableMatchKeys tableMatchKeys;

  public ExtensionTableMatcher(final TableMatchKeys tableMatchKeys) {
    this.tableMatchKeys = requireNonNull(tableMatchKeys, "No table match keys provided");
  }

  @Override
  public boolean test(final ColumnReference proposedWeakAssociation) {

    if (proposedWeakAssociation == null) {
      return false;
    }

    final Column foreignKeyColumn = proposedWeakAssociation.getForeignKeyColumn();
    final Column primaryKeyColumn = proposedWeakAssociation.getPrimaryKeyColumn();

    final String pkColumnName =
        NOT_ALPHANUMERIC_PATTERN.matcher(primaryKeyColumn.getName()).replaceAll("").toLowerCase();
    final String fkColumnName =
        NOT_ALPHANUMERIC_PATTERN.matcher(foreignKeyColumn.getName()).replaceAll("").toLowerCase();
    if (pkColumnName.equals(fkColumnName)) {
      final Table pkTable = primaryKeyColumn.getParent();
      final boolean fkIsUnique =
          foreignKeyColumn.isPartOfPrimaryKey() || foreignKeyColumn.isPartOfUniqueIndex();
      final boolean matches = fkIsUnique && tableMatchKeys.isTopRankedCandidate(pkTable);
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
