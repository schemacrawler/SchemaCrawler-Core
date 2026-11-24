/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.associations;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.Column;
import schemacrawler.test.utility.crawl.LightTable;

public class ColumnMatchKeysMapTest {

  @Test
  public void columnMatchKeysMap_mixed_suffixes() {

    final LightTable table1 = new LightTable("Table1");
    table1.addColumn("EntityId");
    final LightTable table2 = new LightTable("Table2");
    table2.addColumn("Entity_Id");
    final LightTable table3 = new LightTable("Table3");
    table3.addColumn("Entity_ID");
    final LightTable table4 = new LightTable("Table4");
    table4.addColumn("EntityID");
    final LightTable table5 = new LightTable("Table5");
    table5.addColumn("NonEntity");

    final ColumnMatchKeys columnMatchKeysMap =
        new ColumnMatchKeys(List.of(table1, table2, table3, table4, table5));

    assertThat(
        columnMatchKeysMap.toString(),
        is(
            "{non_entity=[Table5.NonEntity], entity=[Table1.EntityId, Table2.Entity_Id,"
                + " Table3.Entity_ID, Table4.EntityID]}"));

    assertThat(columnMatchKeysMap.containsKey("entity"), is(true));
    assertThat(
        columnMatchKeysMap.get("entity").stream()
            .map(Column::getFullName)
            .collect(Collectors.toSet()),
        containsInAnyOrder(
            "Table1.EntityId", "Table2.Entity_Id", "Table3.Entity_ID", "Table4.EntityID"));

    final Column column = table4.addColumn("EntityID");
    assertThat(columnMatchKeysMap.containsKey(column), is(true));
    assertThat(
        columnMatchKeysMap.get(column).stream().collect(Collectors.toSet()),
        containsInAnyOrder("entity"));
  }

  @Test
  public void columnMatchKeysMap_naming_styles() {

    final LightTable table = new LightTable("Table1");
    final Column pascal = table.addColumn("CustomerOrderId");
    final Column acronym = table.addColumn("CustomerID");
    final Column camel = table.addColumn("customerOrderId");
    final Column snake = table.addColumn("customer_order_id");
    final Column idOnly = table.addColumn("Id");

    final ColumnMatchKeys columnMatchKeysMap = new ColumnMatchKeys(List.of(table));

    assertThat(columnMatchKeysMap.get(pascal), containsInAnyOrder("customer_order"));
    assertThat(columnMatchKeysMap.get(acronym), containsInAnyOrder("customer"));
    assertThat(columnMatchKeysMap.get(camel), containsInAnyOrder("customer_order"));
    assertThat(columnMatchKeysMap.get(snake), containsInAnyOrder("customer_order"));
    assertThat(columnMatchKeysMap.containsKey(idOnly), is(false));
    assertThat(columnMatchKeysMap.get("customer_order").size(), is(3));
  }

  @Test
  public void columnMatchKeysMap_plural_names_are_not_singularized() {

    final LightTable table = new LightTable("Table1");
    final Column snake = table.addColumn("order_details_id");
    final Column pascal = table.addColumn("OrderDetailsId");

    final ColumnMatchKeys columnMatchKeysMap = new ColumnMatchKeys(List.of(table));

    assertThat(columnMatchKeysMap.get(snake), containsInAnyOrder("order_details"));
    assertThat(columnMatchKeysMap.get(pascal), containsInAnyOrder("order_details"));
  }

  @Test
  public void id() {

    final LightTable table1 = new LightTable("Table1");
    table1.addColumn("id");
    final LightTable table2 = new LightTable("Table2");
    table2.addColumn("_id");
    final LightTable table3 = new LightTable("Table3");
    table3.addColumn("ID");
    final LightTable table4 = new LightTable("Table4");
    table4.addColumn("_ID");

    final ColumnMatchKeys columnMatchKeysMap =
        new ColumnMatchKeys(List.of(table1, table2, table3, table4));

    assertThat(columnMatchKeysMap.toString(), is("{}"));

    assertThat(columnMatchKeysMap.containsKey("entity"), is(false));
    assertThat(columnMatchKeysMap.containsKey(""), is(false));

    final Column column = table4.addColumn("EntityID");
    assertThat(columnMatchKeysMap.containsKey(column), is(false));
  }
}
