package schemacrawler.ermodel.weakassociations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;
import schemacrawler.test.utility.crawl.LightTable;

public class IdMatcherTest {

  @Test
  public void valid_order_item_order_id_to_orders_order_id() {
    final Table fkTable = new LightTable("order_item");
    final Table pkTable = new LightTable("orders");

    final Column fkColumn = mockColumn(fkTable, "order_id", false);
    final Column pkColumn = mockColumn(pkTable, "order_id", true);

    final IdMatcher matcher = new IdMatcher();
    assertThat(matcher.test(new WeakColumnReference(fkColumn, pkColumn)), is(true));
  }

  @Test
  public void valid_order_item_order_id_to_orders_id() {
    final Table fkTable = new LightTable("order_item");
    final Table pkTable = new LightTable("orders");

    final Column fkColumn = mockColumn(fkTable, "order_id", false);
    final Column pkColumn = mockColumn(pkTable, "id", true);

    final IdMatcher matcher = new IdMatcher();
    assertThat(matcher.test(new WeakColumnReference(fkColumn, pkColumn)), is(true));
  }

  @Test
  public void valid_order_item_order_to_orders_order_id() {
    final Table fkTable = new LightTable("order_item");
    final Table pkTable = new LightTable("orders");

    final Column fkColumn = mockColumn(fkTable, "order", false);
    final Column pkColumn = mockColumn(pkTable, "order_id", true);

    final IdMatcher matcher = new IdMatcher();
    assertThat(matcher.test(new WeakColumnReference(fkColumn, pkColumn)), is(true));
  }

  @Test
  public void valid_order_item_order_to_orders_id() {
    final Table fkTable = new LightTable("order_item");
    final Table pkTable = new LightTable("orders");

    final Column fkColumn = mockColumn(fkTable, "order", false);
    final Column pkColumn = mockColumn(pkTable, "id", true);

    final IdMatcher matcher = new IdMatcher();
    assertThat(matcher.test(new WeakColumnReference(fkColumn, pkColumn)), is(true));
  }

  @Test
  public void invalid_order_item_id_to_orders_id() {
    final Table fkTable = new LightTable("order_item");
    final Table pkTable = new LightTable("orders");

    final Column fkColumn = mockColumn(fkTable, "id", true);
    final Column pkColumn = mockColumn(pkTable, "id", true);

    final IdMatcher matcher = new IdMatcher();
    assertThat(matcher.test(new WeakColumnReference(fkColumn, pkColumn)), is(false));
  }

  @Test
  public void invalid_order_item_order_id_to_orders_order() {
    final Table fkTable = new LightTable("order_item");
    final Table pkTable = new LightTable("orders");

    final Column fkColumn = mockColumn(fkTable, "order_id", false);
    final Column pkColumn = mockColumn(pkTable, "order", true);

    final IdMatcher matcher = new IdMatcher();
    assertThat(matcher.test(new WeakColumnReference(fkColumn, pkColumn)), is(false));
  }

  @Test
  public void invalid_order_item_order_id_to_orders_orderid_isPartOfPk() {
    final Table fkTable = new LightTable("order_item");
    final Table pkTable = new LightTable("orders");

    // order_item.order_id → orders.orderid AND order_item.order_id is part of pk
    // NOTE: This test case from the issue description is interpreted as
    // testing the "possibly subentity" protection.
    final Column fkColumn = mockColumn(fkTable, "order_id", true);
    final Column pkColumn = mockColumn(pkTable, "order_id", true);

    final IdMatcher matcher = new IdMatcher();
    assertThat(matcher.test(new WeakColumnReference(fkColumn, pkColumn)), is(false));
  }

  private Column mockColumn(final Table parent, final String name, final boolean partOfPrimaryKey) {
    final Column column = mock(Column.class);
    when(column.getParent()).thenReturn(parent);
    when(column.getName()).thenReturn(name);
    when(column.isPartOfPrimaryKey()).thenReturn(partOfPrimaryKey);
    return column;
  }
}
