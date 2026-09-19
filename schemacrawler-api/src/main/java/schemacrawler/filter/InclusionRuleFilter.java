/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static java.util.Objects.requireNonNull;

import schemacrawler.inclusionrule.ExcludeAll;
import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.inclusionrule.InclusionRule;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Routine;

public class InclusionRuleFilter<N extends NamedObject> implements NamedObjectFilter<N> {

  /**
   * Returns the identifier key to use for qualified-name matching. For a {@link Routine}, this is
   * its key with the trailing specific-name-disambiguating component dropped, since that component
   * is never part of a routine's full name. For every other kind of named object, this is simply
   * the object's own key, unchanged.
   *
   * @param namedObject named object whose qualified-name key is required; must not be null
   * @return the qualified-name key for the given named object
   */
  private static String unquotedFullName(final NamedObject namedObject) {
    requireNonNull(namedObject, "No named object provided");
    NamedObjectKey key = namedObject.key();
    if (namedObject instanceof Routine) {
      key = key.withoutLast();
    }
    return key.join();
  }

  private final InclusionRule inclusionRule;

  public InclusionRuleFilter(final InclusionRule inclusionRule, final boolean inclusive) {
    if (inclusionRule != null) {
      this.inclusionRule = inclusionRule;
    } else if (inclusive) {
      this.inclusionRule = new IncludeAll();
    } else {
      this.inclusionRule = new ExcludeAll();
    }
  }

  public boolean isExcludeAll() {
    return inclusionRule instanceof ExcludeAll;
  }

  @Override
  public boolean test(final N namedObject) {
    if (namedObject == null) {
      return false;
    }
    final String fullName = namedObject.getFullName();
    // IMPORTANT: Schema names may be blank. Do not do a blank check at this point.
    if (fullName == null) {
      return false;
    }
    // Also test the qualified-name key (schema, table, etc. parts joined with "."),
    // exactly as captured when the object was created. This lets an unquoted regular expression
    // match a full name that includes quoting added purely for display, without making the match
    // case-insensitive.
    final String unquotedFullName = unquotedFullName(namedObject);
    return inclusionRule.test(fullName) || inclusionRule.test(unquotedFullName);
  }

  @Override
  public String toString() {
    return inclusionRule.toString();
  }
}
