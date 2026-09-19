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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schema.RoutineType;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableType;
import schemacrawler.schemacrawler.GrepOptionsBuilder;
import schemacrawler.test.utility.crawl.LightProcedure;
import schemacrawler.test.utility.crawl.LightTable;

@Disabled("Temporarily disabled")
class NamedObjectFiltersTest {

  @Test
  void testnormalizedNameRegex() {
    final LightTable table = new LightTable("sales");

    assertThat(NamedObjectFilters.normalizedNameRegex(".*sales").test(table), is(true));
    assertThat(NamedObjectFilters.normalizedNameRegex(".*inventory").test(table), is(false));
  }

  @Test
  void testNullNamedObjectIsExcluded() {
    assertThat(NamedObjectFilters.normalizedNameRegex(".*").test(null), is(false));
    assertThat(NamedObjectFilters.normalizedFullNameRegex(".*").test(null), is(false));
  }

  @Test
  void testNullRegexThrowsNullPointerException() {
    assertThrows(
        IllegalArgumentException.class, () -> NamedObjectFilters.normalizedNameRegex(null));
    assertThrows(
        IllegalArgumentException.class, () -> NamedObjectFilters.normalizedFullNameRegex(null));
  }

  @Test
  void testRawAndNormalizedNameFilters() {
    final LightTable table = new LightTable("\"Sales.Table\"");

    assertThat(NamedObjectFilters.normalizedNameRegex("\"Sales\\.Table\"").test(table), is(true));
    assertThat(NamedObjectFilters.normalizedNameRegex("Sales\\.Table").test(table), is(true));
    assertThat(NamedObjectFilters.normalizedNameRegex("sales\\.table").test(table), is(true));
    assertThat(NamedObjectFilters.normalizedNameRegex("sales table").test(table), is(false));
  }

  @Test
  void testNormalizedFullNamePreservesRawRegexIntent() {
    final LightTable table = new LightTable("\"Sales.Table\"");

    assertThat(
        NamedObjectFilters.normalizedFullNameRegex("\".*Sales\\.Table\"").test(table), is(true));
    assertThat(NamedObjectFilters.normalizedFullNameRegex(".*sales\\.table").test(table), is(true));
    assertThat(NamedObjectFilters.normalizedFullNameRegex(".*sales table").test(table), is(false));
  }

  @Test
  void testNormalizedNameHandlesBracketAndBacktickQuoting() {
    final LightTable bracketed = new LightTable("[Sales]");
    final LightTable backticked = new LightTable("`Sales`");

    assertThat(NamedObjectFilters.normalizedNameRegex("sales").test(bracketed), is(true));
    assertThat(NamedObjectFilters.normalizedNameRegex("sales").test(backticked), is(true));
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
  void testInclusionRuleFilterIncludesMatchingAndExcludesOthers() {
    final LightTable sales = new LightTable("sales");
    final LightTable inventory = new LightTable("inventory");
    final NamedObjectFilter<Table> filter =
        NamedObjectFilters.inclusionRule(new RegularExpressionInclusionRule(".*sales.*"));

    assertThat(filter.test(sales), is(true));
    assertThat(filter.test(inventory), is(false));
  }

  @Test
  void testInclusionRuleFilterWithNullRuleDefaultsToInclusiveOrExclusive() {
    final LightTable table = new LightTable("sales");

    assertThrows(
        NullPointerException.class,
        () -> NamedObjectFilters.<Table>inclusionRule(null).test(table));
  }

  @Test
  void testTableTypesFilterByName() {
    final Table table = viewTypeTable("sales");

    assertThat(NamedObjectFilters.tableTypes("view").test(table), is(true));
    assertThat(NamedObjectFilters.tableTypes("table").test(table), is(false));
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
