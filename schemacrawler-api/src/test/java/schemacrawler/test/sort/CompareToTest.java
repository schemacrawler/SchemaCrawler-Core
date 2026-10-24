/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.sort;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.comparesEqualTo;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static schemacrawler.crawl.SortTestUtility.newColumn;
import static schemacrawler.crawl.SortTestUtility.newColumnReference;
import static schemacrawler.crawl.SortTestUtility.newDatabaseObject;
import static schemacrawler.crawl.SortTestUtility.newDatabaseProperty;
import static schemacrawler.crawl.SortTestUtility.newIndex;
import static schemacrawler.crawl.SortTestUtility.newIndexColumn;
import static schemacrawler.crawl.SortTestUtility.newProcedure;
import static schemacrawler.crawl.SortTestUtility.newTable;
import static schemacrawler.crawl.SortTestUtility.newView;
import static schemacrawler.crawl.SortTestUtility.setIndexType;
import static schemacrawler.crawl.SortTestUtility.setKeyOrdinalPosition;
import static schemacrawler.crawl.SortTestUtility.setOrdinalPosition;
import static schemacrawler.crawl.SortTestUtility.setSortIndex;
import static schemacrawler.crawl.SortTestUtility.setTableType;
import static schemacrawler.crawl.SortTestUtility.setUnique;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.Index;
import schemacrawler.schema.IndexColumn;
import schemacrawler.schema.IndexType;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Procedure;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableType;
import schemacrawler.schema.View;
import schemacrawler.schemacrawler.SchemaReference;
import us.fatehi.utility.property.Property;

public class CompareToTest {

  @Test
  public void nullableRoutineSpecificNamesSortLastAfterEqualKeysAndParameters() {
    final Schema schema = new SchemaReference();
    final Procedure known = newProcedure(schema, "ROUTINE", "SPECIFIC");
    final Procedure missing = spy(newProcedure(schema, "ROUTINE", "SPECIFIC"));
    doReturn(known.key()).when(missing).key();
    doReturn(null).when(missing).getSpecificName();
    assertThat(known.compareTo(missing), lessThan(0));
    assertThat(missing.compareTo(known), greaterThan(0));
  }

  @Test
  public void nullablePropertyFieldsSortLast() {
    final Property unnamed = mock(Property.class);
    final Property named = mock(Property.class);
    when(named.getName()).thenReturn("name");
    assertThat(Property.comparator.compare(named, unnamed), lessThan(0));
    assertThat(Property.comparator.compare(unnamed, named), greaterThan(0));

    final Property described = newDatabaseProperty("getDatabaseVersion", "value");
    assertThat(described.compareTo(unnamed), lessThan(0));
  }

  @Test
  public void nullableIndexTypesSortLast() {
    final Table table = newTable(new SchemaReference(), "TABLE");
    final Index known = newIndex(table, "INDEX");
    final Index missingType = mock(Index.class);
    when(missingType.getColumns()).thenReturn(List.of());
    assertThat(known.compareTo(missingType), lessThan(0));
  }

  @Test
  public void nullableColumnReferenceFieldsSortLast() {
    final Column named = mock(Column.class);
    final Column unnamed = mock(Column.class);
    when(named.getFullName()).thenReturn("COLUMN");
    final ColumnReference known = newColumnReference(0, named, named);
    final ColumnReference missingForeignName = newColumnReference(0, unnamed, named);
    final ColumnReference missingPrimaryName = newColumnReference(0, named, unnamed);
    assertThat(known.compareTo(missingForeignName), lessThan(0));
    assertThat(missingForeignName.compareTo(known), greaterThan(0));
    assertThat(known.compareTo(missingPrimaryName), lessThan(0));
    assertThat(missingPrimaryName.compareTo(known), greaterThan(0));
    assertThat(known.compareTo(mock(ColumnReference.class)), lessThan(0));
  }

  @Test
  public void nullableTypesSortLastBeforeNames() {
    final Schema schema = new SchemaReference();
    final Table missingType = spy(newTable(schema, "A"));
    doReturn(null).when(missingType).getType();
    final Table knownType = newTable(schema, "Z");
    setTableType(knownType, new TableType("TABLE"));
    assertThat(knownType, lessThan(missingType));
    assertThat(missingType, greaterThan(knownType));
  }

  @ParameterizedTest
  @CsvSource({"-2147483648,2147483647", "-2147483648,0", "-1,2147483647", "1,2"})
  public void numericComparisonsPreserveOrderWithoutOverflow(final int lower, final int higher) {
    final Schema schema = new SchemaReference("catalog", "schema");
    final Table firstTable = newTable(schema, "Z");
    final Table secondTable = newTable(schema, "A");
    setSortIndex(firstTable, lower);
    setSortIndex(secondTable, higher);
    assertThat(firstTable, lessThan(secondTable));
    assertThat(secondTable, greaterThan(firstTable));
    setSortIndex(secondTable, lower);
    assertThat(firstTable, greaterThan(secondTable));

    final Column firstColumn = newColumn(firstTable, "Z");
    final Column secondColumn = newColumn(firstTable, "A");
    setOrdinalPosition(firstColumn, lower);
    setOrdinalPosition(secondColumn, higher);
    assertThat(firstColumn, lessThan(secondColumn));
    assertThat(secondColumn, greaterThan(firstColumn));
    setOrdinalPosition(secondColumn, lower);
    assertThat(firstColumn, greaterThan(secondColumn));

    final Index index = newIndex(firstTable, "INDEX");
    final IndexColumn firstKeyColumn = newIndexColumn(index, firstColumn);
    final IndexColumn secondKeyColumn = newIndexColumn(index, secondColumn);
    setKeyOrdinalPosition(firstKeyColumn, lower);
    setKeyOrdinalPosition(secondKeyColumn, higher);
    assertThat(firstKeyColumn, lessThan(secondKeyColumn));
    assertThat(secondKeyColumn, greaterThan(firstKeyColumn));
    setKeyOrdinalPosition(secondKeyColumn, lower);
    assertThat(firstKeyColumn, greaterThan(secondKeyColumn));

    final ColumnReference firstReference = newColumnReference(lower, firstColumn, firstColumn);
    final ColumnReference secondReference = newColumnReference(higher, secondColumn, secondColumn);
    assertThat(firstReference, lessThan(secondReference));
    assertThat(secondReference, greaterThan(firstReference));
    final ColumnReference tiedReference = newColumnReference(lower, secondColumn, secondColumn);
    assertThat(firstReference, greaterThan(tiedReference));
    assertThat(firstReference, comparesEqualTo(firstReference));
  }

  @Test
  public void indexesRetainUniqueFirstOrderingAndTypeAndNameTieBreakers() {
    final Table table = newTable(new SchemaReference(), "TABLE");
    final Index first = newIndex(table, "Z");
    final Index second = newIndex(table, "A");
    setUnique(first, true);
    setUnique(second, false);
    setIndexType(first, IndexType.other);
    setIndexType(second, IndexType.clustered);
    assertThat(first, lessThan(second));
    assertThat(second, greaterThan(first));

    setUnique(second, true);
    assertThat(first, greaterThan(second));
    assertThat(second, lessThan(first));

    setIndexType(second, IndexType.other);
    assertThat(first, greaterThan(second));
    assertThat(second, lessThan(first));
    assertThat(first, comparesEqualTo(first));
  }

  /**
   * See: <a href= "https://github.com/schemacrawler/SchemaCrawler/issues/228">Inconsistent table
   * comparison</a>
   */
  @Test
  public void compareTables() {
    final Table tbl = newTable(new SchemaReference(null, "public"), "booking_detail");
    setTableType(tbl, new TableType("table"));

    final View view = newView(new SchemaReference(null, "public"), "blog_monthly_stat_fa");
    setTableType(view, new TableType("materialized view"));

    assertThat(view, lessThan(null));
    assertThat(tbl, lessThan(null));

    assertThat(tbl, comparesEqualTo(tbl));
    assertThat(view, comparesEqualTo(view));

    assertThat(tbl, lessThan(view));
    assertThat(view, greaterThan(tbl));
  }

  @Test
  public void databaseObject() {
    final SchemaReference schema = new SchemaReference("catalog", "schema");
    final SchemaReference schema1 = new SchemaReference("catalog", "schema1");
    final DatabaseObject tstDbObj1 = newDatabaseObject(schema, "tstDbObj1");
    final DatabaseObject tstDbObj2 = newDatabaseObject(schema, "tstDbObj2");
    final DatabaseObject tstDbObj3 = newDatabaseObject(schema1, "tstDbObj1");

    assertThat(tstDbObj1, lessThan(null));
    assertThat(tstDbObj2, lessThan(null));
    assertThat(tstDbObj3, lessThan(null));

    assertThat(tstDbObj1, lessThan(tstDbObj2));
    assertThat(tstDbObj2, greaterThan(tstDbObj1));

    assertThat(tstDbObj1, lessThan(tstDbObj3));
    assertThat(tstDbObj2, lessThan(tstDbObj3));
    assertThat(tstDbObj3, greaterThan(tstDbObj1));
    assertThat(tstDbObj3, greaterThan(tstDbObj2));

    assertThat(
        tstDbObj1,
        greaterThan(
            new NamedObject() {
              /** */
              private static final long serialVersionUID = -1308483158535248447L;

              @Override
              public int compareTo(final NamedObject o) {
                return 0;
              }

              @Override
              public String getFullName() {
                return "";
              }

              @Override
              public String getName() {
                return "";
              }

              @Override
              public NamedObjectKey key() {
                return null;
              }
            }));
  }
}
