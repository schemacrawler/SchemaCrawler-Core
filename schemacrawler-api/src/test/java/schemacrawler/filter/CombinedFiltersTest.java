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

import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import schemacrawler.filter.NamedObjectFilters.SimpleTableType;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableType;
import schemacrawler.test.utility.crawl.LightTable;

class CombinedFiltersTest {

  @Test
  void testNegatedFilter() {
    final LightTable sales = new LightTable("sales");
    final LightTable inventory = new LightTable("inventory");
    final Predicate<schemacrawler.schema.NamedObject> notSales =
        NamedObjectFilters.nameRegex("sales").negate();

    assertThat(notSales.test(sales), is(false));
    assertThat(notSales.test(inventory), is(true));
  }

  @Test
  void testComplexFilterCombiningNameAndTableType() {
    final Table salesView = viewTypeTable("sales_view");
    final LightTable salesTable = new LightTable("sales_table");
    final Table inventoryView = viewTypeTable("inventory_view");

    final Predicate<Table> salesViewsOnly =
        NamedObjectFilters.tableTypes(SimpleTableType.view)
            .and(NamedObjectFilters.nameRegex(".*sales.*"));

    assertThat(salesViewsOnly.test(salesView), is(true));
    assertThat(salesViewsOnly.test(salesTable), is(false));
    assertThat(salesViewsOnly.test(inventoryView), is(false));
  }

  private static Table viewTypeTable(final String name) {
    final Table table = mock(Table.class);
    when(table.getName()).thenReturn(name);
    when(table.getFullName()).thenReturn(name);
    when(table.getTableType()).thenReturn(new TableType("view"));
    return table;
  }
}
