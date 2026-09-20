/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import schemacrawler.inclusionrule.InclusionRule;
import schemacrawler.inclusionrule.InclusionRuleWithRegularExpression;
import schemacrawler.schema.NamedObject;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
final class FullNameInclusionRuleFilters {

  static <N extends NamedObject> NamedObjectFilter<N> fullName(final InclusionRule inclusionRule) {
    if (inclusionRule instanceof InclusionRuleWithRegularExpression expression) {
      return new QuoteTolerantFullNameInclusionRuleFilter<>(expression);
    }
    return new InclusionRuleFilter<>(inclusionRule, false);
  }

  private FullNameInclusionRuleFilters() {
    // Prevent instantiation
  }
}
