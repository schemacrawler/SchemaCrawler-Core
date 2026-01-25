/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.weakassociations;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import schemacrawler.schema.Column;

/**
 * Matches weak associations based on conventional naming rules for foreign keys ending with an id
 * suffix.
 *
 * <p>To prevent "God-table" false positives, this rule excludes generic primary keys named only
 * {@code id}, {@code key}, or {@code keyid}. Such generic names are common in many tables and would
 * otherwise incorrectly match every foreign key that ends with the same suffix.
 */
public final class IdMatcher implements Predicate<ProposedWeakAssociation> {

  private static final Logger LOGGER = Logger.getLogger(IdMatcher.class.getName());

  private static final Pattern ID_PATTERN = Pattern.compile("_?(id|key|keyid)$", CASE_INSENSITIVE);

  @Override
  public boolean test(final ProposedWeakAssociation proposedWeakAssociation) {
    if (proposedWeakAssociation == null) {
      return false;
    }

    final Column foreignKeyColumn = proposedWeakAssociation.getForeignKeyColumn();
    final Column primaryKeyColumn = proposedWeakAssociation.getPrimaryKeyColumn();

    final boolean fkColEndsWithId = ID_PATTERN.matcher(foreignKeyColumn.getName()).find();
    final Matcher pkMatcher = ID_PATTERN.matcher(primaryKeyColumn.getName());
    // Check that the primary key column has a prefix, so that it is not equal to something like
    // simply "ID"
    final boolean pkColEndsWithId = pkMatcher.find() && pkMatcher.start() > 0;

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
