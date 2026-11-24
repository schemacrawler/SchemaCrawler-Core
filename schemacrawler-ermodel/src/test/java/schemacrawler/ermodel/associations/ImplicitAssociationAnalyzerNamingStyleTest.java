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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.Table;
import schemacrawler.test.utility.crawl.LightPrimaryKey;
import schemacrawler.test.utility.crawl.LightTable;

/** Tests that implicit association inference does not depend on the naming style. */
public class ImplicitAssociationAnalyzerNamingStyleTest {

  private static final class TableDefinition {
    private final String tableName;
    private final String pkColumnName;
    private final String fkColumnName;
    private final String fkColumnType;

    private TableDefinition(
        final String tableName,
        final String pkColumnName,
        final String fkColumnName,
        final String fkColumnType) {
      this.tableName = tableName;
      this.pkColumnName = pkColumnName;
      this.fkColumnName = fkColumnName;
      this.fkColumnType = fkColumnType;
    }
  }

  private static final List<TableDefinition> SNAKE_CASE_FIXTURE =
      List.of(
          new TableDefinition("customers", "id", null, null),
          new TableDefinition("orders", "id", "customer_id", "INTEGER"),
          new TableDefinition("products", "id", null, null),
          new TableDefinition("order_items", null, "order_id", "INTEGER"),
          new TableDefinition("order_details", "id", null, null),
          new TableDefinition("order_detail_notes", null, "order_details_id", "INTEGER"));

  @Test
  public void pascalCaseFixtureGivesSameAssociationsAsSnakeCase() {
    final List<String> snakeCase = associations(SNAKE_CASE_FIXTURE, ImplicitStyle.SNAKE);
    final List<String> pascalCase = associations(SNAKE_CASE_FIXTURE, ImplicitStyle.PASCAL);
    final List<String> camelCase = associations(SNAKE_CASE_FIXTURE, ImplicitStyle.CAMEL);
    final List<String> spaced = associations(SNAKE_CASE_FIXTURE, ImplicitStyle.SPACED);

    assertThat(snakeCase, hasSize(3));
    assertThat(
        snakeCase,
        containsInAnyOrder(
            "orders.customer_id->customers.id",
            "order_items.order_id->orders.id",
            "order_detail_notes.order_details_id->order_details.id"));
    assertThat(normalize(pascalCase), containsInAnyOrder(snakeCase.toArray()));
    assertThat(normalize(camelCase), containsInAnyOrder(snakeCase.toArray()));
    assertThat(normalize(spaced), containsInAnyOrder(snakeCase.toArray()));
  }

  @Test
  public void pascalCasePrefixIsStripped() {
    final LightTable customer = table("AppCustomer", "Id");
    final LightTable order = table("AppOrder", "Id");
    order.addColumn("CustomerId");
    final LightTable product = table("AppProduct", "Id");

    final Collection<ColumnReference> associations =
        analyze(List.<Table>of(customer, order, product));

    assertThat(describe(associations), containsInAnyOrder("AppOrder.CustomerId->AppCustomer.Id"));
  }

  @Test
  public void spacedNamesAreMatched() {
    final LightTable details = table("Order Details", "Id");
    final LightTable notes = table("Notes", "Id");
    notes.addColumn("OrderDetailsId");

    final Collection<ColumnReference> associations = analyze(List.<Table>of(details, notes));

    assertThat(
        describe(associations), containsInAnyOrder("Notes.OrderDetailsId->Order Details.Id"));
  }

  @Test
  public void incompatibleDataTypeIsStillRejected() {
    final LightTable customer = table("Customer", "Id");
    final LightTable order = table("Order", "Id");
    order.addDataColumn("CustomerId", "VARCHAR");

    final Collection<ColumnReference> associations = analyze(List.<Table>of(customer, order));

    assertThat(associations, is(empty()));
  }

  private enum ImplicitStyle {
    SNAKE,
    PASCAL,
    CAMEL,
    SPACED
  }

  private static String restyle(final String name, final ImplicitStyle style) {
    if (name == null) {
      return null;
    }
    final String[] words = name.split("_");
    final StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < words.length; i++) {
      final String word = words[i];
      switch (style) {
        case SNAKE:
          buffer.append(i > 0 ? "_" : "").append(word);
          break;
        case SPACED:
          buffer.append(i > 0 ? " " : "").append(capitalize(word));
          break;
        case CAMEL:
          buffer.append(i > 0 ? capitalize(word) : word);
          break;
        case PASCAL:
        default:
          buffer.append(capitalize(word));
          break;
      }
    }
    return buffer.toString();
  }

  private static String capitalize(final String word) {
    return word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1);
  }

  private List<String> associations(
      final List<TableDefinition> definitions, final ImplicitStyle style) {
    final List<Table> tables = new ArrayList<>();
    for (final TableDefinition definition : definitions) {
      final LightTable table =
          table(restyle(definition.tableName, style), restyle(definition.pkColumnName, style));
      if (definition.fkColumnName != null) {
        table.addDataColumn(restyle(definition.fkColumnName, style), definition.fkColumnType);
      }
      tables.add(table);
    }
    return describe(analyze(tables));
  }

  private Collection<ColumnReference> analyze(final List<Table> tables) {
    final TableMatchKeys tableMatchKeys = new TableMatchKeys(tables);
    return new ImplicitAssociationAnalyzer(tableMatchKeys, new IdMatcher()).analyzeTables();
  }

  private List<String> describe(final Collection<ColumnReference> associations) {
    final List<String> descriptions = new ArrayList<>();
    for (final ColumnReference association : associations) {
      descriptions.add(
          columnName(association.getForeignKeyColumn())
              + "->"
              + columnName(association.getPrimaryKeyColumn()));
    }
    return descriptions;
  }

  private String columnName(final Column column) {
    return column.getParent().getName() + "." + column.getName();
  }

  /** Normalizes each name part of the descriptions to snake case, for comparison across styles. */
  private List<String> normalize(final List<String> descriptions) {
    final List<String> normalized = new ArrayList<>();
    for (final String description : descriptions) {
      final StringBuilder buffer = new StringBuilder();
      for (final String side : description.split("->")) {
        if (buffer.length() > 0) {
          buffer.append("->");
        }
        final int dot = side.lastIndexOf('.');
        buffer
            .append(ImplicitAssociationsUtility.normalizeMatchKeyName(side.substring(0, dot)))
            .append('.')
            .append(ImplicitAssociationsUtility.normalizeMatchKeyName(side.substring(dot + 1)));
      }
      normalized.add(buffer.toString());
    }
    return normalized;
  }

  /** Creates a table with a single-column primary key, if a primary key column name is given. */
  private LightTable table(final String name, final String pkColumnName) {
    final LightTable table = new LightTable(name);
    if (pkColumnName != null) {
      final Column pkColumn = table.addColumn(pkColumnName);
      table.setPrimaryKey(new LightPrimaryKey(pkColumn));
    }
    return table;
  }
}
