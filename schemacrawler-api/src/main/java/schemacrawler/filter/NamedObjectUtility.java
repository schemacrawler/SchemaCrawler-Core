/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static java.util.Objects.requireNonNull;

import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Routine;
import us.fatehi.utility.UtilityMarker;

/**
 * Package-private helper shared by {@link NamedObjectFilters} and {@link InclusionRuleFilter} for
 * deriving the identifier key to use when matching a named object's qualified (schema-scoped) name.
 */
@UtilityMarker
final class NamedObjectUtility {

  /**
   * Returns the identifier key to use for qualified-name matching. For a {@link Routine}, this is
   * its key with the trailing specific-name-disambiguating component dropped, since that component
   * is never part of a routine's full name. For every other kind of named object, this is simply
   * the object's own key, unchanged.
   *
   * @param namedObject named object whose qualified-name key is required; must not be null
   * @return the qualified-name key for the given named object
   */
  static String unquotedFullName(final NamedObject namedObject) {
    requireNonNull(namedObject, "No named object provided");
    NamedObjectKey key = namedObject.key();
    if (namedObject instanceof Routine) {
      key = key.withoutLast();
    }
    return key.join();
  }

  private NamedObjectUtility() {
    // Prevent instantiation
  }
}
