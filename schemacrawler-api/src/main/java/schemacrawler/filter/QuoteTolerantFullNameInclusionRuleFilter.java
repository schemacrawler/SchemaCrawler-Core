/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import java.util.regex.Pattern;
import schemacrawler.inclusionrule.InclusionRuleWithRegularExpression;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Routine;

/**
 * Matches regular-expression inclusion rules against displayed and unquoted full names.
 *
 * <p>Unlike {@link InclusionRuleFilter}, this filter evaluates the regular-expression patterns
 * directly so exclusions apply to both name representations.
 *
 * @param <N> named-object type
 */
public class QuoteTolerantFullNameInclusionRuleFilter<N extends NamedObject>
    implements NamedObjectFilter<N> {

  private static String unquotedFullName(final NamedObject namedObject) {
    NamedObjectKey key = namedObject.key();
    if (namedObject instanceof Routine) {
      key = key.withoutLast();
    }
    return key.join();
  }

  private final Pattern exclusionPattern;
  private final Pattern inclusionPattern;

  public QuoteTolerantFullNameInclusionRuleFilter(
      final InclusionRuleWithRegularExpression inclusionRule) {
    if (inclusionRule == null) {
      throw new NullPointerException("No inclusion rule provided");
    }
    inclusionPattern = inclusionRule.getInclusionPattern();
    exclusionPattern = inclusionRule.getExclusionPattern();
  }

  @Override
  public boolean test(final N namedObject) {
    if (namedObject == null) {
      return false;
    }
    final String fullName = namedObject.getFullName();
    if (fullName == null) {
      return false;
    }
    final String unquotedFullName = unquotedFullName(namedObject);
    // Test inclusion against both forms so an unquoted CLI regex can match a full
    // name that is quoted only for display.
    final boolean included =
        inclusionPattern.matcher(fullName).matches()
            || inclusionPattern.matcher(unquotedFullName).matches();
    // Test exclusion against both forms before accepting the object, so a match on
    // one form cannot bypass an exclusion on the other.
    final boolean excluded =
        exclusionPattern.matcher(fullName).matches()
            || exclusionPattern.matcher(unquotedFullName).matches();
    return included && !excluded;
  }

  @Override
  public String toString() {
    return "%s@%h {+/%s/ -/%s/}"
        .formatted(
            getClass().getSimpleName(),
            System.identityHashCode(this),
            inclusionPattern.pattern(),
            exclusionPattern.pattern());
  }
}
