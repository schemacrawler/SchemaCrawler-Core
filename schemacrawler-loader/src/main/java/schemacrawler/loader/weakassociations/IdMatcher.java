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
import java.util.regex.Pattern;
import schemacrawler.schema.Column;

/**
 * Matches weak associations based on conventional naming rules for foreign keys.
 *
 * <p>A match is found if the foreign key column name ends with a conventional suffix ({@code _id},
 * {@code _key}, or {@code _keyid}), and the primary key column is not a generic "id" column. This
 * prevents generic primary keys from incorrectly matching every potential foreign key.
 */
public final class IdMatcher implements Predicate<ProposedWeakAssociation> {

  private static final Logger LOGGER = Logger.getLogger(IdMatcher.class.getName());

  private static final Pattern endsWithIdPattern = Pattern.compile("(?i).*_?(id|key|keyid)$");
  private static final Pattern isIdPattern = Pattern.compile("(?i)_?(id|key|keyid)$");

  @Override
  public boolean test(final ProposedWeakAssociation proposedWeakAssociation) {
    if (proposedWeakAssociation == null) {
      return false;
    }

    final Column foreignKeyColumn = proposedWeakAssociation.getForeignKeyColumn();
    final Column primaryKeyColumn = proposedWeakAssociation.getPrimaryKeyColumn();

    final boolean fkColEndsWithId = endsWithIdPattern.matcher(foreignKeyColumn.getName()).matches();
    // PK column must have the suffix, but it must not be ONLY the suffix (generic name)
    final boolean pkColEndsWithId =
        endsWithIdPattern.matcher(primaryKeyColumn.getName()).matches()
            && !isIdPattern.matcher(primaryKeyColumn.getName()).matches();

    final boolean matches = fkColEndsWithId && !pkColEndsWithId;
    if (matches && LOGGER.isLoggable(Level.FINER)) {
      LOGGER.log(
          Level.FINER,
          "Weak association rule matched: IdMatcher for proposed association {0}",
          proposedWeakAssociation);
    }
    return matches;
  }
}
