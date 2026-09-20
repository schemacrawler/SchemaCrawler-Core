/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import schemacrawler.inclusionrule.RegularExpressionRule;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Table;

class RegularExpressionInclusionRuleFilterTest {

  @Test
  void testUnquotedPatternMatchesQuotedFullNameViaJoinedKey() {
    final Table table = table("books", "\"Celebrity Updates\"", "books", "Celebrity Updates");
    final RegularExpressionInclusionRuleFilter<Table> filter =
        new RegularExpressionInclusionRuleFilter<>(
            new RegularExpressionRule(".*Celebrity Updates$", null));

    assertThat(filter.test(table), is(true));
  }

  @Test
  void testPatternDifferingOnlyInCaseDoesNotMatch() {
    final Table table = table("books", "\"Celebrity Updates\"", "books", "Celebrity Updates");
    final RegularExpressionInclusionRuleFilter<Table> filter =
        new RegularExpressionInclusionRuleFilter<>(
            new RegularExpressionRule(".*celebrity updates$", null));

    assertThat(filter.test(table), is(false));
  }

  @Test
  void testExclusionMatchingEitherCandidateWins() {
    final Table table = table("books", "\"Celebrity Updates\"", "books", "Celebrity Updates");
    final RegularExpressionInclusionRuleFilter<Table> filter =
        new RegularExpressionInclusionRuleFilter<>(
            new RegularExpressionRule(".*Celebrity Updates$", ".*\"Celebrity Updates\"$"));

    assertThat(filter.test(table), is(false));
  }

  @Test
  void testUnquotedExclusionAlsoWins() {
    final Table table = table("books", "\"Celebrity Updates\"", "books", "Celebrity Updates");
    final RegularExpressionInclusionRuleFilter<Table> filter =
        new RegularExpressionInclusionRuleFilter<>(
            new RegularExpressionRule(".*Celebrity Updates$", ".*Celebrity Updates$"));

    assertThat(filter.test(table), is(false));
  }

  @Test
  void testRoutineKeyDropsSpecificName() {
    final Routine routine = mock(Routine.class);
    when(routine.getFullName()).thenReturn("sales_schema.list_sales");
    when(routine.key())
        .thenReturn(new NamedObjectKey("sales_schema", "list_sales", "list_sales_17"));
    final RegularExpressionInclusionRuleFilter<Routine> filter =
        new RegularExpressionInclusionRuleFilter<>(
            new RegularExpressionRule(".*sales_schema\\.list_sales$", null));

    assertThat(filter.test(routine), is(true));
  }

  @Test
  void testNullNamedObjectAndFullNameAreExcluded() {
    final RegularExpressionInclusionRuleFilter<Table> filter =
        new RegularExpressionInclusionRuleFilter<>(new RegularExpressionRule(".*", null));
    final Table table = mock(Table.class);
    when(table.getFullName()).thenReturn(null);

    assertThat(filter.test(null), is(false));
    assertThat(filter.test(table), is(false));
  }

  private static Table table(
      final String schemaName,
      final String fullNameSuffix,
      final String schemaKeyPart,
      final String nameKeyPart) {
    final Table table = mock(Table.class);
    when(table.getFullName()).thenReturn(schemaName + "." + fullNameSuffix);
    when(table.key()).thenReturn(new NamedObjectKey(schemaKeyPart, nameKeyPart));
    return table;
  }
}
