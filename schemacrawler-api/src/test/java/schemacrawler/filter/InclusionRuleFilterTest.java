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
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;

class InclusionRuleFilterTest {

  @Test
  void testUnquotedPatternMatchesQuotedFullNameViaJoinedKey() {
    // The full name is quoted for display, but the underlying key parts (as originally
    // captured) are not.
    final Table table = table("books", "\"Celebrity Updates\"", "books", "Celebrity Updates");
    final InclusionRuleFilter<Table> filter =
        new InclusionRuleFilter<>(new RegularExpressionInclusionRule(".*Celebrity Updates$"), true);

    assertThat(filter.test(table), is(true));
  }

  @Test
  void testUnquotedPatternMatchesUnquotedFullName() {
    // Pre-existing behavior: an unquoted full name still matches directly, unchanged.
    final Table table = table("books", "sales", "books", "sales");
    final InclusionRuleFilter<Table> filter =
        new InclusionRuleFilter<>(new RegularExpressionInclusionRule(".*sales$"), true);

    assertThat(filter.test(table), is(true));
  }

  @Test
  void testPatternDifferingOnlyInCaseDoesNotMatch() {
    // Quote-tolerance is added without introducing case-insensitivity.
    final Table table = table("books", "\"Celebrity Updates\"", "books", "Celebrity Updates");
    final InclusionRuleFilter<Table> filter =
        new InclusionRuleFilter<>(new RegularExpressionInclusionRule(".*celebrity updates$"), true);

    assertThat(filter.test(table), is(false));
  }

  @Test
  void testNullNamedObjectIsExcluded() {
    final InclusionRuleFilter<Table> filter =
        new InclusionRuleFilter<>(new RegularExpressionInclusionRule(".*"), true);

    assertThat(filter.test(null), is(false));
  }

  @Test
  void testNullFullNameIsExcluded() {
    final Table table = mock(Table.class);
    when(table.getFullName()).thenReturn(null);
    final InclusionRuleFilter<Table> filter =
        new InclusionRuleFilter<>(new RegularExpressionInclusionRule(".*"), true);

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
