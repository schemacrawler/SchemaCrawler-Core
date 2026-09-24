/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static schemacrawler.schemacrawler.DatabaseObjectRuleForInclusion.ruleForSchemaInclusion;

import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.inclusionrule.InclusionRule;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.Schema;
import schemacrawler.schemacrawler.DatabaseObjectRuleForInclusion;
import schemacrawler.schemacrawler.LimitOptions;

final class DatabaseObjectFilter<D extends DatabaseObject> implements NamedObjectFilter<D> {

  private final InclusionRule databaseObjectInclusionRule;
  private final InclusionRule schemaInclusionRule;

  DatabaseObjectFilter(
      final LimitOptions options,
      final DatabaseObjectRuleForInclusion databaseObjectRuleForInclusion) {
    if (options != null) {
      schemaInclusionRule = options.get(ruleForSchemaInclusion);
    } else {
      schemaInclusionRule = new IncludeAll();
    }

    if (databaseObjectRuleForInclusion != null) {
      databaseObjectInclusionRule = options.get(databaseObjectRuleForInclusion);
    } else {
      databaseObjectInclusionRule = new IncludeAll();
    }
  }

  /**
   * Check for database object limiting rules.
   *
   * <p>Matches against the schema's and the database object's full names in a quote-tolerant
   * manner, so a regular-expression inclusion rule can match whether or not it accounts for quoting
   * added around names that require it (such as names containing spaces).
   *
   * @param databaseObject Database object to check
   * @return Whether the table should be included
   */
  @Override
  public boolean test(final D databaseObject) {
    if (databaseObject == null) {
      return false;
    }

    boolean include = true;

    if (include && schemaInclusionRule != null) {
      include =
          FullNameInclusionRuleFilters.<Schema>fullName(schemaInclusionRule)
              .test(databaseObject.getSchema());
    }
    if (include && databaseObjectInclusionRule != null) {
      include =
          FullNameInclusionRuleFilters.<D>fullName(databaseObjectInclusionRule)
              .test(databaseObject);
    }

    return include;
  }
}
