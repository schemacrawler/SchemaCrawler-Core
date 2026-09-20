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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import schemacrawler.inclusionrule.InclusionRule;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Routine;
import schemacrawler.schema.RoutineType;
import schemacrawler.schema.Schema;
import schemacrawler.schema.SimpleTableType;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableType;
import schemacrawler.schemacrawler.GrepOptionsBuilder;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.test.utility.crawl.LightProcedure;
import schemacrawler.test.utility.crawl.LightTable;

class NamedObjectFiltersTest {

  @Test
  void testNameRegex() {
    final LightTable table = new LightTable("sales");

    assertThat(NamedObjectFilters.nameRegex(".*sales").test(table), is(true));
    assertThat(NamedObjectFilters.nameRegex(".*inventory").test(table), is(false));
  }

  @Test
  void testNullNamedObjectIsExcluded() {
    assertThat(NamedObjectFilters.nameRegex(".*").test(null), is(false));
    assertThat(NamedObjectFilters.fullNameRegex(".*").test(null), is(false));
  }

  @Test
  void testNullRegexThrowsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> NamedObjectFilters.nameRegex(null));
    assertThrows(IllegalArgumentException.class, () -> NamedObjectFilters.fullNameRegex(null));
  }

  @Test
  void testNameRegexMatchesRawNameOnly() {
    final LightTable table = new LightTable("\"Sales.Table\"");

    // There is no dequoting or lower-casing transform, so a regex must match the raw name,
    // quote characters and all, to succeed. Matching is still case-insensitive via the
    // pattern's own compilation flags.
    assertThat(NamedObjectFilters.nameRegex("\"Sales\\.Table\"").test(table), is(true));
    assertThat(NamedObjectFilters.nameRegex("\"sales\\.table\"").test(table), is(true));
    // A regex written for the unquoted, dequoted value no longer matches, since the name is
    // never dequoted before matching.
    assertThat(NamedObjectFilters.nameRegex("Sales\\.Table").test(table), is(false));
  }

  @Test
  void testFullNameRegexMatchesRawFullNameAndJoinedKey() {
    final LightTable table = new LightTable("sales");

    assertThat(NamedObjectFilters.fullNameRegex(".*sales").test(table), is(true));
    assertThat(NamedObjectFilters.fullNameRegex(".*inventory").test(table), is(false));
  }

  @Test
  void testFullNameFactoryKeepsNonRegexRulesOnDisplayedFullName() {
    final Table table = mock(Table.class);
    when(table.getFullName()).thenReturn("books.\"Celebrity Updates\"");
    when(table.key()).thenReturn(new NamedObjectKey("books", "Celebrity Updates"));
    final InclusionRule rule = text -> text.endsWith("Celebrity Updates");

    assertThat(NamedObjectFilters.<Table>fullName(rule).test(table), is(false));
  }

  @Test
  void testFullNameFactoryUsesQuoteTolerantFilterForRegexRules() {
    final Table table = mock(Table.class);
    when(table.getFullName()).thenReturn("books.\"Celebrity Updates\"");
    when(table.key()).thenReturn(new NamedObjectKey("books", "Celebrity Updates"));

    assertThat(
        NamedObjectFilters.<Table>fullName(
                new RegularExpressionInclusionRule(".*Celebrity Updates$"))
            .test(table),
        is(true));
  }

  @Test
  void testFullNameRegexAlsoAcceptsQuotedRawRegexIntent() {
    final LightTable table = new LightTable("\"Sales.Table\"");

    // Matched against the raw, still-quoted full name and the joined key parts, both of which
    // retain the literal quote characters, since no dequoting transform is applied.
    assertThat(NamedObjectFilters.fullNameRegex(".*\"Sales\\.Table\"").test(table), is(true));
    assertThat(NamedObjectFilters.fullNameRegex(".*\".*\\.Table\"").test(table), is(true));
  }

  @Test
  void testFullNameRegexPreservesLiteralDotsInsideQuotedParts() {
    // Each part of the schema-qualified name is quoted, and itself contains a literal dot.
    // The key is built from separately-captured parts (never by concatenating then splitting a
    // single string), so joining them keeps the literal dots intact as content, and only the
    // join adds a genuine separator.
    final Schema schema = new SchemaReference(null, "\"My.Schema\"");
    final LightTable table = new LightTable(schema, "\"My.Table\"");

    assertThat(
        NamedObjectFilters.fullNameRegex(".*\"My\\.Schema\"\\.\"My\\.Table\"").test(table),
        is(true));
  }

  @Test
  void testNameRegexHandlesBracketAndBacktickQuoting() {
    final LightTable bracketed = new LightTable("[Sales]");
    final LightTable backticked = new LightTable("`Sales`");

    // Names are matched raw (with brackets/backticks intact); the pattern is still
    // case-insensitive via the pattern's own compilation flags.
    assertThat(NamedObjectFilters.nameRegex("\\[Sales\\]").test(bracketed), is(true));
    assertThat(NamedObjectFilters.nameRegex("`Sales`").test(backticked), is(true));
  }

  @Test
  void testFullNameRegexForRoutineExcludesSpecificNameFromJoinedKey() {
    // A routine's identifier key carries a trailing specific-name-disambiguating component that
    // its full name never has. fullNameRegex must derive the joined-key candidate from the
    // qualified name (without that trailing component), not the raw key.
    final Routine procedure = mock(Routine.class);
    when(procedure.getFullName()).thenReturn("sales_schema.list_sales");
    when(procedure.key())
        .thenReturn(new NamedObjectKey("sales_schema", "list_sales", "list_sales_17"));

    assertThat(
        NamedObjectFilters.fullNameRegex(".*sales_schema\\.list_sales").test(procedure), is(true));
    // The specific name is never part of the routine's full name, so a regex requiring it as a
    // trailing joined-key segment must not match.
    assertThat(
        NamedObjectFilters.fullNameRegex(".*sales_schema\\.list_sales\\.list_sales_17")
            .test(procedure),
        is(false));
  }

  @Test
  void testTableGrepWithNullOptionsThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> NamedObjectFilters.tableGrep(null));
  }

  @Test
  void testTableGrepIncludesMatchingTableAndExcludesOthers() {
    final LightTable sales = new LightTable("sales");
    final LightTable inventory = new LightTable("inventory");
    final NamedObjectFilter<schemacrawler.schema.Table> filter =
        NamedObjectFilters.tableGrep(
            GrepOptionsBuilder.builder()
                .includeGreppedTables(new RegularExpressionInclusionRule(".*sales.*"))
                .toOptions());

    assertThat(filter.test(sales), is(true));
    assertThat(filter.test(inventory), is(false));
  }

  @Test
  void testTableGrepWithNoInclusionRulesIncludesEverything() {
    final LightTable table = new LightTable("sales");
    final NamedObjectFilter<schemacrawler.schema.Table> filter =
        NamedObjectFilters.tableGrep(GrepOptionsBuilder.builder().toOptions());

    assertThat(filter.test(table), is(true));
  }

  @Test
  void testRoutineGrepWithNullOptionsThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> NamedObjectFilters.routineGrep(null));
  }

  @Test
  void testRoutineGrepIncludesMatchingRoutineDefinitionAndExcludesOthers() {
    final LightProcedure listSales = new LightProcedure("list_sales");
    listSales.setDefinition("select * from sales");
    final LightProcedure archiveInventory = new LightProcedure("archive_inventory");
    archiveInventory.setDefinition("delete from inventory");
    final NamedObjectFilter<schemacrawler.schema.Routine> filter =
        NamedObjectFilters.routineGrep(
            GrepOptionsBuilder.builder()
                .includeGreppedDefinitions(new RegularExpressionInclusionRule(".*sales.*"))
                .toOptions());

    assertThat(filter.test(listSales), is(true));
    assertThat(filter.test(archiveInventory), is(false));
  }

  @Test
  void testRoutineGrepWithNoInclusionRulesIncludesEverything() {
    final LightProcedure procedure = new LightProcedure("list_sales");
    final NamedObjectFilter<schemacrawler.schema.Routine> filter =
        NamedObjectFilters.routineGrep(GrepOptionsBuilder.builder().toOptions());

    assertThat(filter.test(procedure), is(true));
  }

  @Test
  void testTableTypesFilterByName() {
    final Table table = viewTypeTable("sales");

    assertThat(NamedObjectFilters.tableTypes(SimpleTableType.view).test(table), is(true));
    assertThat(NamedObjectFilters.tableTypes(SimpleTableType.table).test(table), is(false));
  }

  @Test
  void testRoutineTypesFilterByLimitOptions() {
    final LightProcedure procedure = new LightProcedure("list_sales");
    final NamedObjectFilter<schemacrawler.schema.Routine> proceduresOnly =
        NamedObjectFilters.routineTypes(RoutineType.procedure);
    final NamedObjectFilter<schemacrawler.schema.Routine> functionsOnly =
        NamedObjectFilters.routineTypes(RoutineType.function);

    assertThat(proceduresOnly.test(procedure), is(true));
    assertThat(functionsOnly.test(procedure), is(false));
  }

  @Test
  void testRoutineTypesFilterWithNullLimitOptionsThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> NamedObjectFilters.routineTypes(null));
  }

  private static Table viewTypeTable(final String name) {
    final Table table = mock(Table.class);
    when(table.getName()).thenReturn(name);
    when(table.getFullName()).thenReturn(name);
    when(table.getTableType()).thenReturn(new TableType("view"));
    return table;
  }
}
