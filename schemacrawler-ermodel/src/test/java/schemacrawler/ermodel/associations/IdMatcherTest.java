package schemacrawler.ermodel.associations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import schemacrawler.schema.Column;
import schemacrawler.test.utility.crawl.LightColumn;
import schemacrawler.test.utility.crawl.LightTable;

public class IdMatcherTest {

  @Test
  public void invalid_order_item_id_to_orders_id() {
    final Column fkColumn = mockColumn("order_item", "id", true);
    final Column pkColumn = mockColumn("orders", "id", true);

    final IdMatcher matcher = new IdMatcher();
    assertThat(matcher.test(new ImplicitColumnReference(fkColumn, pkColumn)), is(false));
  }

  @Test
  public void invalid_order_item_order_id_to_orders_order() {
    final Column fkColumn = mockColumn("order_item", "order_id", false);
    final Column pkColumn = mockColumn("orders", "order", true);

    final IdMatcher matcher = new IdMatcher();
    assertThat(matcher.test(new ImplicitColumnReference(fkColumn, pkColumn)), is(false));
  }

  @Test
  public void invalid_order_item_order_id_to_orders_orderid_isPartOfPk() {
    // order_item.order_id → orders.orderid AND order_item.order_id is part of pk
    // NOTE: This test case from the issue description is interpreted as
    // testing the "possibly subentity" protection.
    final Column fkColumn = mockColumn("order_item", "order_id", true);
    final Column pkColumn = mockColumn("orders", "order_id", true);

    final IdMatcher matcher = new IdMatcher();
    assertThat(matcher.test(new ImplicitColumnReference(fkColumn, pkColumn)), is(false));
  }

  @Test
  public void valid_order_item_order_id_to_orders_id() {
    final Column fkColumn = mockColumn("order_item", "order_id", false);
    final Column pkColumn = mockColumn("orders", "id", true);

    final IdMatcher matcher = new IdMatcher();
    assertThat(matcher.test(new ImplicitColumnReference(fkColumn, pkColumn)), is(true));
  }

  @Test
  public void valid_order_item_order_id_to_orders_order_id() {
    final Column fkColumn = mockColumn("order_item", "order_id", false);
    final Column pkColumn = mockColumn("orders", "order_id", true);

    final IdMatcher matcher = new IdMatcher();
    assertThat(matcher.test(new ImplicitColumnReference(fkColumn, pkColumn)), is(true));
  }

  @Test
  public void valid_order_item_order_to_orders_id() {
    final Column fkColumn = mockColumn("order_item", "order", false);
    final Column pkColumn = mockColumn("orders", "id", true);

    final IdMatcher matcher = new IdMatcher();
    assertThat(matcher.test(new ImplicitColumnReference(fkColumn, pkColumn)), is(true));
  }

  @Test
  public void valid_order_item_order_to_orders_order_id() {
    final Column fkColumn = mockColumn("order_item", "order", false);
    final Column pkColumn = mockColumn("orders", "order_id", true);

    final IdMatcher matcher = new IdMatcher();
    assertThat(matcher.test(new ImplicitColumnReference(fkColumn, pkColumn)), is(true));
  }

  private Column mockColumn(
      final String tableName, final String columnName, final boolean partOfPrimaryKey) {
    LightColumn lightColumn = LightColumn.newColumn(new LightTable(tableName), columnName);
    final Column column = spy(lightColumn);
    when(column.isPartOfPrimaryKey()).thenReturn(partOfPrimaryKey);
    return column;
  }
}
