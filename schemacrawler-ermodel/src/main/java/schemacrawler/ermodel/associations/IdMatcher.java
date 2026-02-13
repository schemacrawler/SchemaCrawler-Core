/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.associations;

import static schemacrawler.ermodel.associations.ImplicitAssociationsUtility.normalizeColumnName;
import static schemacrawler.ermodel.associations.ImplicitAssociationsUtility.removeId;

import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;

/**
 * Matches implicit associations using naming conventions where a foreign key column identifies its
 * parent table by name and an {@code id} suffix.
 *
 * <p>This matcher implements several heuristics to ensure high-confidence matches in real-world
 * schemas:
 *
 * <ul>
 *   <li><b>Suffix Matching:</b> Recognizes common foreign key suffixes such as {@code _id}, {@code
 *       _key}, and {@code _keyid} (case-insensitive).
 *   <li><b>Generic ID Protection:</b> To prevent "God-tables" (generic tables like {@code UUIDs} or
 *       {@code Metadata} with a single {@code ID} column) from matching every foreign key in the
 *       database, this rule excludes primary keys named simply {@code id}, {@code key}, or {@code
 *       keyid} unless the foreign key name specifically includes the parent table's name.
 *   <li><b>Sub-entity Filtering:</b> Prevents misidentifying primary key columns in extension or
 *       sub-entity tables as implicit associations to a parent. If a column is part of its own
 *       table's primary key and shares the exact name of a potential parent's primary key, it is
 *       excluded to avoid circular or incorrect 1-to-1 mappings that are better handled by specific
 *       extension table rules.
 * </ul>
 *
 * <p>The matching logic operates on normalized names, stripping non-alphanumeric characters to
 * handle various snake_case, camelCase, or space-separated naming styles consistently.
 */
final class IdMatcher implements Predicate<ColumnReference> {

  private static final Logger LOGGER = Logger.getLogger(IdMatcher.class.getName());

  @Override
  public boolean test(final ColumnReference proposedAssociation) {
    if (proposedAssociation == null) {
      return false;
    }

    final Column fkColumn = proposedAssociation.getForeignKeyColumn();
    final Column pkColumn = proposedAssociation.getPrimaryKeyColumn();

    final String pkColumnName = normalizeColumnName(pkColumn);
    final String fkColumnName = normalizeColumnName(fkColumn);

    final String fkBaseName = removeId(fkColumn);
    final String pkBaseName = removeId(pkColumn);

    final boolean fkIsPartOfPk = fkColumn.isPartOfPrimaryKey();

    final Matcher pkMatcher1 = ImplicitAssociationsUtility.ID_PATTERN.matcher(pkColumnName);
    final boolean pkColNameHasId = pkMatcher1.find();
    // Check that the primary key column has a prefix, so that it is not equal to
    // something like simply "ID"
    final boolean pkColNameIsJustId = pkColNameHasId && pkMatcher1.start() == 0;

    final boolean isPossiblySubentity =
        pkColNameHasId && fkColumnName.equalsIgnoreCase(pkColumnName) && fkIsPartOfPk;

    final boolean matches =
        pkColNameHasId
            && (fkBaseName.equals(pkBaseName) || pkColNameIsJustId)
            && !isPossiblySubentity;
    if (matches && LOGGER.isLoggable(Level.FINER)) {
      LOGGER.log(
          Level.FINER,
          "Implicit association rule matched: IdMatcher for proposed association {0}",
          proposedAssociation);
    }
    return matches;
  }
}
