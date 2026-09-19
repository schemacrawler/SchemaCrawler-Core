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
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schema.RoutineType;
import schemacrawler.schema.Schema;
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
  void testNameRegexStripsQuoting() {
    final LightTable table = new LightTable("\"Sales.Table\"");

    // The quoted name is normalized (quotes stripped, lower-cased) before matching, so the
    // regex is matched against the unquoted, lower-case value.
    assertThat(NamedObjectFilters.nameRegex("Sales\\.Table").test(table), is(true));
    assertThat(NamedObjectFilters.nameRegex("sales\\.table").test(table), is(true));
    assertThat(NamedObjectFilters.nameRegex("sales table").test(table), is(false));
  }

  @Test
  void testNameRegexAlsoAcceptsQuotedRawRegexIntent() {
    final LightTable table = new LightTable("\"Sales.Table\"");

    // The pattern is also matched against the object's original (raw, still-quoted) name, so a
    // regex written to look like the raw, quoted identifier matches too - not just a regex
    // written for the normalized, unquoted value.
    assertThat(NamedObjectFilters.nameRegex("\"Sales\\.Table\"").test(table), is(true));
  }

  @Test
  void testFullNameRegexStripsQuoting() {
    final LightTable table = new LightTable("\"Sales.Table\"");

    assertThat(NamedObjectFilters.fullNameRegex(".*sales\\.table").test(table), is(true));
    assertThat(NamedObjectFilters.fullNameRegex(".*sales table").test(table), is(false));
  }

  @Test
  void testFullNameRegexAlsoAcceptsQuotedRawRegexIntent() {
    final LightTable table = new LightTable("\"Sales.Table\"");

    // Matched against the raw, still-quoted full name, in addition to the normalized full name.
    assertThat(NamedObjectFilters.fullNameRegex(".*\"Sales\\.Table\"").test(table), is(true));
    assertThat(NamedObjectFilters.fullNameRegex(".*\".*\\.Table\"").test(table), is(true));
  }

  @Test
  void testFullNameRegexPreservesLiteralDotsInsideQuotedParts() {
    // Each part of the schema-qualified name is quoted, and itself contains a literal dot.
    // Naively stripping quotes from the whole concatenated full name (or splitting on dots
    // before un-quoting) would corrupt the value. Normalizing each key part individually,
    // before joining, keeps the literal dots intact as content, and only the join adds a
    // genuine separator.
    final Schema schema = new SchemaReference(null, "\"My.Schema\"");
    final LightTable table = new LightTable(schema, "\"My.Table\"");

    assertThat(NamedObjectFilters.fullNameRegex("my\\.schema\\.my\\.table").test(table), is(true));
    // A pattern that treats the whole qualified name as a single quoted identifier does not
    // match, since the raw full name actually consists of two separately-quoted parts (each
    // with its own surrounding quotes), not one quoted string spanning both parts; and the
    // normalized full name has no quote characters at all.
    assertThat(
        NamedObjectFilters.fullNameRegex("\"my\\.schema\\.my\\.table\"").test(table), is(false));
  }

  @Test
  void testNameRegexHandlesBracketAndBacktickQuoting() {
    final LightTable bracketed = new LightTable("[Sales]");
    final LightTable backticked = new LightTable("`Sales`");

    assertThat(NamedObjectFilters.nameRegex("sales").test(bracketed), is(true));
    assertThat(NamedObjectFilters.nameRegex("sales").test(backticked), is(true));
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
