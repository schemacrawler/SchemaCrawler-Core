/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.comparesEqualTo;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.Index;
import schemacrawler.schema.IndexType;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;
import schemacrawler.schema.TableType;
import schemacrawler.schemacrawler.SchemaReference;
import us.fatehi.utility.property.Property;

public class CompareToTest {

  @Test
  public void nullableRoutineSpecificNamesSortLastAfterEqualKeysAndParameters() {
    final Schema schema = new SchemaReference();
    final MutableProcedure known = new MutableProcedure(schema, "ROUTINE", "SPECIFIC");
    final MutableProcedure missing = spy(new MutableProcedure(schema, "ROUTINE", "SPECIFIC"));
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

    final ImmutableDatabaseProperty described =
        new ImmutableDatabaseProperty("getDatabaseVersion", "value");
    assertThat(described.compareTo(unnamed), lessThan(0));
  }

  @Test
  public void nullableIndexTypesSortLast() {
    final MutableTable table = new MutableTable(new SchemaReference(), "TABLE");
    final MutableIndex known = new MutableIndex(table, "INDEX");
    final Index missingType = mock(Index.class);
    when(missingType.getColumns()).thenReturn(List.of());
    assertThat(known.compareTo(missingType), lessThan(0));
  }

  @Test
  public void nullableColumnReferenceFieldsSortLast() {
    final Column named = mock(Column.class);
    final Column unnamed = mock(Column.class);
    when(named.getFullName()).thenReturn("COLUMN");
    final ColumnReference known = new ImmutableColumnReference(0, named, named);
    final ColumnReference missingForeignName = new ImmutableColumnReference(0, unnamed, named);
    final ColumnReference missingPrimaryName = new ImmutableColumnReference(0, named, unnamed);
    assertThat(known.compareTo(missingForeignName), lessThan(0));
    assertThat(missingForeignName.compareTo(known), greaterThan(0));
    assertThat(known.compareTo(missingPrimaryName), lessThan(0));
    assertThat(missingPrimaryName.compareTo(known), greaterThan(0));
    assertThat(known.compareTo(mock(ColumnReference.class)), lessThan(0));
  }

  @Test
  public void nullableTypesSortLastBeforeNames() {
    final Schema schema = new SchemaReference();
    final MutableTable missingType = spy(new MutableTable(schema, "A"));
    doReturn(null).when(missingType).getType();
    final MutableTable knownType = new MutableTable(schema, "Z");
    knownType.setTableType(new TableType("TABLE"));
    assertThat(knownType, lessThan(missingType));
    assertThat(missingType, greaterThan(knownType));
  }

  @ParameterizedTest
  @CsvSource({"-2147483648,2147483647", "-2147483648,0", "-1,2147483647", "1,2"})
  public void numericComparisonsPreserveOrderWithoutOverflow(final int lower, final int higher) {
    final Schema schema = new SchemaReference("catalog", "schema");
    final MutableTable firstTable = new MutableTable(schema, "Z");
    final MutableTable secondTable = new MutableTable(schema, "A");
    firstTable.setSortIndex(lower);
    secondTable.setSortIndex(higher);
    assertThat(firstTable, lessThan(secondTable));
    assertThat(secondTable, greaterThan(firstTable));
    secondTable.setSortIndex(lower);
    assertThat(firstTable, greaterThan(secondTable));

    final MutableColumn firstColumn = new MutableColumn(firstTable, "Z");
    final MutableColumn secondColumn = new MutableColumn(firstTable, "A");
    firstColumn.setOrdinalPosition(lower);
    secondColumn.setOrdinalPosition(higher);
    assertThat(firstColumn, lessThan(secondColumn));
    assertThat(secondColumn, greaterThan(firstColumn));
    secondColumn.setOrdinalPosition(lower);
    assertThat(firstColumn, greaterThan(secondColumn));

    final MutableIndex index = new MutableIndex(firstTable, "INDEX");
    final MutableIndexColumn firstKeyColumn = new MutableIndexColumn(index, firstColumn);
    final MutableIndexColumn secondKeyColumn = new MutableIndexColumn(index, secondColumn);
    firstKeyColumn.setKeyOrdinalPosition(lower);
    secondKeyColumn.setKeyOrdinalPosition(higher);
    assertThat(firstKeyColumn, lessThan(secondKeyColumn));
    assertThat(secondKeyColumn, greaterThan(firstKeyColumn));
    secondKeyColumn.setKeyOrdinalPosition(lower);
    assertThat(firstKeyColumn, greaterThan(secondKeyColumn));

    final ImmutableColumnReference firstReference =
        new ImmutableColumnReference(lower, firstColumn, firstColumn);
    final ImmutableColumnReference secondReference =
        new ImmutableColumnReference(higher, secondColumn, secondColumn);
    assertThat(firstReference, lessThan(secondReference));
    assertThat(secondReference, greaterThan(firstReference));
    final ImmutableColumnReference tiedReference =
        new ImmutableColumnReference(lower, secondColumn, secondColumn);
    assertThat(firstReference, greaterThan(tiedReference));
    assertThat(firstReference, comparesEqualTo(firstReference));
  }

  @Test
  public void indexesRetainUniqueFirstOrderingAndTypeAndNameTieBreakers() {
    final MutableTable table = new MutableTable(new SchemaReference(), "TABLE");
    final MutableIndex first = new MutableIndex(table, "Z");
    final MutableIndex second = new MutableIndex(table, "A");
    first.setUnique(true);
    second.setUnique(false);
    first.setIndexType(IndexType.other);
    second.setIndexType(IndexType.clustered);
    assertThat(first, lessThan(second));
    assertThat(second, greaterThan(first));

    second.setUnique(true);
    assertThat(first, greaterThan(second));
    assertThat(second, lessThan(first));

    second.setIndexType(IndexType.other);
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
    final MutableTable tbl =
        new MutableTable(new SchemaReference(null, "public"), "booking_detail");
    tbl.setTableType(new TableType("table"));

    final MutableView view =
        new MutableView(new SchemaReference(null, "public"), "blog_monthly_stat_fa");
    view.setTableType(new TableType("materialized view"));

    assertThat(view, lessThan(null));
    assertThat(tbl, lessThan(null));

    assertThat(tbl, comparesEqualTo(tbl));
    assertThat(view, comparesEqualTo(view));

    assertThat(tbl, lessThan(view));
    assertThat(view, greaterThan(tbl));
  }

  @Test
  public void databaseObject() {
    class TestDatabaseObject extends AbstractDatabaseObject {

      /** */
      private static final long serialVersionUID = -7594540047157616727L;

      TestDatabaseObject(final Schema schema, final String name) {
        super(schema, name);
      }
    }

    final SchemaReference schema = new SchemaReference("catalog", "schema");
    final SchemaReference schema1 = new SchemaReference("catalog", "schema1");
    final TestDatabaseObject tstDbObj1 = new TestDatabaseObject(schema, "tstDbObj1");
    final TestDatabaseObject tstDbObj2 = new TestDatabaseObject(schema, "tstDbObj2");
    final TestDatabaseObject tstDbObj3 = new TestDatabaseObject(schema1, "tstDbObj1");

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
