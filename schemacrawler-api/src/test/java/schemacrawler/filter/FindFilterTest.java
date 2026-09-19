/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static schemacrawler.test.utility.crawl.LightColumnDataTypeUtility.columnDataType;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schemacrawler.GrepOptionsBuilder;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.test.utility.crawl.LightProcedure;
import schemacrawler.test.utility.crawl.LightTable;

class FindFilterTest {

  private Catalog catalog;
  private LightTable sales;
  private LightTable inventory;

  @BeforeEach
  void setUp() {
    sales = new LightTable("sales");
    sales.addColumn("amount");
    sales.addHiddenColumn("internal_id");
    inventory = new LightTable("inventory");
    inventory.addColumn("quantity");

    catalog = mock(Catalog.class);
    when(catalog.getTables()).thenReturn(List.of(sales, inventory));
  }

  @Test
  void testFindColumnDataTypes() {
    final ColumnDataType integerType = columnDataType("INTEGER");
    final ColumnDataType varcharType = columnDataType("VARCHAR");
    when(catalog.getColumnDataTypes()).thenReturn(List.of(integerType, varcharType));

    final CatalogSearcher searcher = CatalogSearcher.search(catalog);

    assertThat(
        searcher.findColumnDataTypes(NamedObjectFilters.nameRegex("INTEGER")),
        contains(integerType));
    assertThat(searcher.findColumnDataTypes(NamedObjectFilters.nameRegex("MISSING")), hasSize(0));
  }

  @Test
  void testFindRoutines() {
    final LightProcedure listSales = new LightProcedure("list_sales");
    final LightProcedure archiveSales = new LightProcedure("archive_sales");
    when(catalog.getRoutines()).thenReturn(List.of(listSales, archiveSales));

    final CatalogSearcher searcher = CatalogSearcher.search(catalog);

    assertThat(
        searcher.findRoutines(NamedObjectFilters.nameRegex("list_sales")), contains(listSales));
    assertThat(searcher.findRoutines(NamedObjectFilters.nameRegex("missing")), hasSize(0));
  }

  @Test
  void testFindSchemas() {
    final Schema salesSchema = new SchemaReference(null, "sales_schema");
    final Schema inventorySchema = new SchemaReference(null, "inventory_schema");
    when(catalog.getSchemas()).thenReturn(List.of(salesSchema, inventorySchema));

    final CatalogSearcher searcher = CatalogSearcher.search(catalog);

    assertThat(
        searcher.findSchemas(NamedObjectFilters.nameRegex("sales_schema")), contains(salesSchema));
    assertThat(searcher.findSchemas(NamedObjectFilters.nameRegex("missing")), hasSize(0));
  }

  @Test
  void testFindSequences() {
    final Sequence salesSequence = mock(Sequence.class);
    when(salesSequence.getName()).thenReturn("sales_seq");
    when(salesSequence.getFullName()).thenReturn("sales_seq");
    final Sequence inventorySequence = mock(Sequence.class);
    when(inventorySequence.getName()).thenReturn("inventory_seq");
    when(inventorySequence.getFullName()).thenReturn("inventory_seq");
    when(catalog.getSequences()).thenReturn(List.of(salesSequence, inventorySequence));

    final CatalogSearcher searcher = CatalogSearcher.search(catalog);

    assertThat(
        searcher.findSequences(NamedObjectFilters.nameRegex("sales_seq")), contains(salesSequence));
    assertThat(searcher.findSequences(NamedObjectFilters.nameRegex("missing")), hasSize(0));
  }

  @Test
  void testFindSynonyms() {
    final Synonym salesSynonym = mock(Synonym.class);
    when(salesSynonym.getName()).thenReturn("sales_syn");
    when(salesSynonym.getFullName()).thenReturn("sales_syn");
    final Synonym inventorySynonym = mock(Synonym.class);
    when(inventorySynonym.getName()).thenReturn("inventory_syn");
    when(inventorySynonym.getFullName()).thenReturn("inventory_syn");
    when(catalog.getSynonyms()).thenReturn(List.of(salesSynonym, inventorySynonym));

    final CatalogSearcher searcher = CatalogSearcher.search(catalog);

    assertThat(
        searcher.findSynonyms(NamedObjectFilters.nameRegex("sales_syn")), contains(salesSynonym));
    assertThat(searcher.findSynonyms(NamedObjectFilters.nameRegex("missing")), hasSize(0));
  }

  @Test
  void testFindTables() {
    final CatalogSearcher searcher = CatalogSearcher.search(catalog);

    assertThat(searcher.findTables(NamedObjectFilters.nameRegex("sales")), contains(sales));
    assertThat(searcher.findColumns(NamedObjectFilters.fullNameRegex(".*\\.amount")), hasSize(1));
    assertThat(searcher.findColumns(NamedObjectFilters.fullNameRegex(".*internal_id")), hasSize(0));
    assertThat(searcher.findTables(NamedObjectFilters.nameRegex("missing")), hasSize(0));
    assertThat(
        searcher.findTables(
            NamedObjectFilters.tableGrep(
                GrepOptionsBuilder.builder()
                    .includeGreppedTables(
                        new schemacrawler.inclusionrule.RegularExpressionInclusionRule("inventory"))
                    .toOptions())),
        contains(inventory));
    assertThat(searcher.findTables(NamedObjectFilters.nameRegex("sales")), contains(sales));
  }
}
