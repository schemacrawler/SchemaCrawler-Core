/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */
package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static schemacrawler.loader.utility.TableRowCountsUtility.TABLE_ROW_COUNT_KEY;
import static us.fatehi.test.utility.TestObjectUtility.returnEmpty;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Index;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.test.utility.crawl.LightColumn;
import schemacrawler.test.utility.crawl.LightColumnReference;
import schemacrawler.test.utility.crawl.LightTable;
import schemacrawler.test.utility.crawl.LightTrigger;
import schemacrawler.tools.utility.TableTraits;

public class TableTraitsTest {

  /**
   * Builds a bridge-candidate table: two imported FKs to distinct parent tables, covered by a
   * unique index over the combined FK columns.
   */
  private static Table bridgeCandidateTable() {
    final LightTable bridgeDelegate =
        new LightTable(new SchemaReference("PUBLIC", "BOOKS"), "BOOK_AUTHORS");
    final LightColumn bookId = bridgeDelegate.addColumn("BOOK_ID");
    final LightColumn authorId = bridgeDelegate.addColumn("AUTHOR_ID");

    final LightTable books = new LightTable(new SchemaReference("PUBLIC", "BOOKS"), "BOOKS");
    final LightColumn booksId = books.addColumn("ID");
    final LightTable authors = new LightTable(new SchemaReference("PUBLIC", "BOOKS"), "AUTHORS");
    final LightColumn authorsId = authors.addColumn("ID");

    final AtomicReference<Table> tableRef = new AtomicReference<>();
    final ForeignKey fkBooks = proxyForeignKey("FK_BOOK", tableRef, books, bookId, booksId);
    final ForeignKey fkAuthors =
        proxyForeignKey("FK_AUTHOR", tableRef, authors, authorId, authorsId);
    final List<ForeignKey> fks = List.of(fkBooks, fkAuthors);
    final List<Index> indexes = List.of(proxyUniqueIndex(bookId, authorId));

    final Table table =
        (Table)
            Proxy.newProxyInstance(
                Table.class.getClassLoader(),
                new Class<?>[] {Table.class},
                (proxy, method, args) ->
                    switch (method.getName()) {
                      case "getImportedForeignKeys" -> fks;
                      case "hasForeignKeys" -> true;
                      case "getIndexes" -> indexes;
                      case "hasIndexes" -> true;
                      case "hasPrimaryKey", "isSelfReferencing", "hasTriggers" -> false;
                      case "getPrimaryKey" -> null;
                      case "equals" -> proxy == (args != null && args.length > 0 ? args[0] : null);
                      case "hashCode" -> System.identityHashCode(proxy);
                      case "key" -> new NamedObjectKey(null, null, "BOOK_AUTHORS");
                      case "toString" -> "Table[BOOK_AUTHORS]";
                      default -> returnEmpty(method);
                    });
    tableRef.set(table);
    return table;
  }

  private static ForeignKey proxyForeignKey(
      final String name,
      final AtomicReference<Table> tableRef,
      final Table primaryKeyTable,
      final Column foreignKeyColumn,
      final Column primaryKeyColumn) {
    final ColumnReference colRef = new LightColumnReference(foreignKeyColumn, primaryKeyColumn);
    return (ForeignKey)
        Proxy.newProxyInstance(
            ForeignKey.class.getClassLoader(),
            new Class<?>[] {ForeignKey.class},
            (proxy, method, args) ->
                switch (method.getName()) {
                  case "key" -> new NamedObjectKey(name);
                  case "getForeignKeyTable", "getParent" -> tableRef.get();
                  case "getPrimaryKeyTable" -> primaryKeyTable;
                  case "getColumnReferences" -> List.of(colRef);
                  case "isSelfReferencing" -> false;
                  case "equals" -> proxy == (args != null && args.length > 0 ? args[0] : null);
                  case "hashCode" -> System.identityHashCode(proxy);
                  case "toString" -> "ForeignKey[" + name + "]";
                  default -> returnEmpty(method);
                });
  }

  private static Index proxyUniqueIndex(final Column... columns) {
    final List<Column> indexColumns = List.of(columns);
    return (Index)
        Proxy.newProxyInstance(
            Index.class.getClassLoader(),
            new Class<?>[] {Index.class},
            (proxy, method, args) ->
                switch (method.getName()) {
                  case "getColumns", "getConstrainedColumns" -> indexColumns;
                  case "isUnique" -> true;
                  case "key" -> new NamedObjectKey("UIDX_BOOK_AUTHORS");
                  case "toString" -> "Index[UIDX_BOOK_AUTHORS]";
                  default -> returnEmpty(method);
                });
  }

  @Test
  public void handlesNullTable() {
    final TableTraits attributes = TableTraits.from(null);

    assertThat(attributes.noPrimaryKey(), is(nullValue()));
    assertThat(attributes.noForeignKeys(), is(nullValue()));
    assertThat(attributes.noIndexes(), is(nullValue()));
    assertThat(attributes.selfReferencing(), is(nullValue()));
    assertThat(attributes.hasTriggers(), is(nullValue()));
    assertThat(attributes.emptyTable(), is(nullValue()));
    assertThat(attributes.bridgeTable(), is(nullValue()));
  }

  @Test
  public void derivesAttributesFromTable() {
    final LightTable table = new LightTable(new SchemaReference("PUBLIC", "BOOKS"), "BOOKS");
    table.addTrigger(new LightTrigger(table, "TRG_BOOKS"));
    table.setAttribute(TABLE_ROW_COUNT_KEY, 0L);

    final TableTraits attributes = TableTraits.from(table);

    assertThat(attributes.noPrimaryKey(), is(Boolean.TRUE));
    assertThat(attributes.noForeignKeys(), is(Boolean.TRUE));
    assertThat(attributes.noIndexes(), is(Boolean.TRUE));
    assertThat(attributes.selfReferencing(), is(nullValue()));
    assertThat(attributes.hasTriggers(), is(Boolean.TRUE));
    assertThat(attributes.emptyTable(), is(Boolean.TRUE));
    assertThat(attributes.bridgeTable(), is(nullValue()));
  }

  @Test
  public void doesNotMarkEmptyWhenRowCountUnavailable() {
    final LightTable table = new LightTable(new SchemaReference("PUBLIC", "BOOKS"), "BOOKS");

    final TableTraits attributes = TableTraits.from(table);

    assertThat(attributes.emptyTable(), is(nullValue()));
  }

  @Test
  public void doesNotMarkEmptyWhenRowCountIsNonZero() {
    final LightTable table = new LightTable(new SchemaReference("PUBLIC", "BOOKS"), "BOOKS");
    table.setAttribute(TABLE_ROW_COUNT_KEY, 7L);

    final TableTraits attributes = TableTraits.from(table);

    assertThat(attributes.emptyTable(), is(nullValue()));
  }

  @Test
  public void doesNotMarkBridgeTableWhenNotInferredAsBridge() {
    final Table table = new LightTable("A_TABLE");

    final TableTraits attributes = TableTraits.from(table);

    assertThat(attributes.bridgeTable(), is(nullValue()));
  }

  @Test
  public void marksBridgeTableWhenInferredAsBridge() {
    final Table table = bridgeCandidateTable();

    final TableTraits attributes = TableTraits.from(table);

    assertThat(attributes.bridgeTable(), is(Boolean.TRUE));
  }
}
