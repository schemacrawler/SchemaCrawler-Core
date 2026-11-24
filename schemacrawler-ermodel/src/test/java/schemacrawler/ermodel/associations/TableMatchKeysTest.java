/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.associations;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;
import schemacrawler.test.utility.crawl.LightTable;

public class TableMatchKeysTest {

  @Test
  public void tableMatchKeys_boundaries() {
    TableMatchKeys matchkeys;
    List<String> withoutPrefix;

    // 1.
    assertThrows(NullPointerException.class, () -> new TableMatchKeys(null));

    // 2.
    matchkeys = new TableMatchKeys(List.of());
    assertThat(matchkeys.toString(), is("{}"));

    withoutPrefix = matchkeys.get(new LightTable("table0"));
    assertThat(withoutPrefix, is(nullValue()));

    // 3.
    matchkeys = new TableMatchKeys(tables("table1"));
    assertThat(matchkeys.toString(), is("{table1=[table1]}"));

    withoutPrefix = matchkeys.get(new LightTable("table0"));
    assertThat(withoutPrefix, is(nullValue()));

    withoutPrefix = matchkeys.get(new LightTable("table1"));
    assertThat(withoutPrefix, containsInAnyOrder("table1"));

    withoutPrefix = matchkeys.get((Table) null);
    assertThat(withoutPrefix, is(nullValue()));
  }

  @Test
  public void tableMatchKeys_mixed_prefixes() {
    List<String> withoutPrefix;

    final TableMatchKeys matchkeys =
        new TableMatchKeys(tables("xyz_old_table1", "xyz_old_table2", "xyz_table3"));

    withoutPrefix = matchkeys.get(new LightTable("table0"));
    assertThat(withoutPrefix, is(nullValue()));

    withoutPrefix = matchkeys.get(new LightTable("xyz_old_table1"));
    assertThat(withoutPrefix, containsInAnyOrder("table1", "old_table1", "xyz_old_table1"));

    withoutPrefix = matchkeys.get(new LightTable("xyz_old_table2"));
    assertThat(withoutPrefix, containsInAnyOrder("table2", "old_table2", "xyz_old_table2"));

    withoutPrefix = matchkeys.get(new LightTable("xyz_table3"));
    assertThat(withoutPrefix, containsInAnyOrder("table3", "xyz_table3"));
  }

  @Test
  public void tableMatchKeys_pascal_case_and_spaced() {
    final LightTable pascal = new LightTable("CustomerOrder");
    // The first word is also stripped as a prefix, as for snake_case names
    assertThat(
        new TableMatchKeys(List.of(pascal)).get(pascal),
        containsInAnyOrder("customer_order", "order"));

    final LightTable spaced = new LightTable("Order Details");
    assertThat(
        new TableMatchKeys(List.of(spaced)).get(spaced),
        containsInAnyOrder("order_detail", "detail", "order_details", "details"));
  }

  @Test
  public void tableMatchKeys_plural_table_keeps_exact_key() {
    final LightTable book = new LightTable("BOOK");
    final LightTable books = new LightTable("BOOKS");
    final TableMatchKeys matchkeys = new TableMatchKeys(List.of(book, books));

    // A singular column key matches both tables, as before
    assertThat(matchkeys.get("book"), containsInAnyOrder(book, books));
    // A plural column key matches only the plural table
    assertThat(matchkeys.get("books"), containsInAnyOrder(books));
  }

  @Test
  public void tableMatchKeys_unicode_boundaries() {
    final LightTable table = new LightTable("KotŻółty");
    final LightTable other = new LightTable("Other");
    final Column column = other.addColumn("KotŻółtyId");

    final TableMatchKeys tableKeys = new TableMatchKeys(List.of(table));
    final ColumnMatchKeys columnKeys = new ColumnMatchKeys(List.of(other));

    assertThat(tableKeys.get(table), hasItem("kot_żółty"));
    assertThat(columnKeys.get(column), containsInAnyOrder("kot_żółty"));
  }

  /** Table key of X and column key of XId must be equal in every naming style. */
  @Test
  public void tableMatchKeys_symmetry_with_column_keys() {
    final String[][] namePairs = {
      {"customer_order", "customer_order_id"},
      {"customerOrder", "customerOrderId"},
      {"CustomerOrder", "CustomerOrderId"},
      {"Customer Order", "Customer Order Id"},
      {"CUSTOMER_ORDER", "CUSTOMER_ORDER_ID"},
      {"CUSTOMER_ORDER", "CUSTOMER_ORDERID"}
    };

    for (final String[] pair : namePairs) {
      final LightTable table = new LightTable(pair[0]);
      final LightTable other = new LightTable("Other");
      final Column column = other.addColumn(pair[1]);

      final TableMatchKeys tableKeys = new TableMatchKeys(List.of(table));
      final ColumnMatchKeys columnKeys = new ColumnMatchKeys(List.of(other));

      assertThat(pair[0], tableKeys.get(table), hasItem("customer_order"));
      assertThat(pair[1], columnKeys.get(column), containsInAnyOrder("customer_order"));
    }
  }

  private List<Table> tables(final String... tableNames) {
    requireNonNull(tableNames);

    final List<Table> tables = new ArrayList<>();
    for (final String tableName : tableNames) {
      tables.add(new LightTable(tableName));
    }
    return tables;
  }
}
