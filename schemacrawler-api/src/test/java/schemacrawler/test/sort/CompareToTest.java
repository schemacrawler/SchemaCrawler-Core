/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.sort;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.number.OrderingComparison.comparesEqualTo;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static schemacrawler.crawl.SortTestUtility.newColumn;
import static schemacrawler.crawl.SortTestUtility.newColumnReference;
import static schemacrawler.crawl.SortTestUtility.newDatabaseObject;
import static schemacrawler.crawl.SortTestUtility.newDatabaseProperty;
import static schemacrawler.crawl.SortTestUtility.newForeignKey;
import static schemacrawler.crawl.SortTestUtility.newImplicitAssociation;
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
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import schemacrawler.namedobject.NamedObjectSort;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Index;
import schemacrawler.schema.IndexColumn;
import schemacrawler.schema.IndexType;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Procedure;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableReference;
import schemacrawler.schema.TableType;
import schemacrawler.schema.View;
import schemacrawler.schemacrawler.SchemaReference;
import us.fatehi.utility.property.Property;

public class CompareToTest {

  @Test
  public void naturalKeyTiesRetainAllIdentitiesAcrossPermutations() {
    final Schema schema = new SchemaReference("catalog", "schema");
    final Table child = newTable(schema, "CHILD");
    final Table parent = newTable(schema, "PARENT");
    final List<List<NamedObject>> families =
        List.of(
            List.of(
                newProcedure(schema, "a b", "SAME"),
                newProcedure(schema, "a-b", "SAME"),
                newProcedure(schema, "ab", "SAME")),
            List.of(newIndex(child, "a b"), newIndex(child, "a-b"), newIndex(child, "ab")),
            List.of(
                newForeignKey("a b", child, parent),
                newForeignKey("a-b", child, parent),
                newForeignKey("ab", child, parent)));
    final List<List<Integer>> permutations =
        List.of(
            List.of(0, 1, 2),
            List.of(0, 2, 1),
            List.of(1, 0, 2),
            List.of(1, 2, 0),
            List.of(2, 0, 1),
            List.of(2, 1, 0));

    for (final List<NamedObject> family : families) {
      for (final List<Integer> permutation : permutations) {
        final TreeSet<NamedObject> sorted = new TreeSet<>(NamedObjectSort.natural);
        permutation.forEach(index -> sorted.add(family.get(index)));
        assertThat(sorted.size(), is(3));
        assertThat(List.copyOf(sorted), is(family));
      }
    }
  }

  @Test
  public void nullableRoutineSpecificNamesSortFirstAfterEqualKeys() {
    final Schema schema = new SchemaReference();
    final Procedure known = newProcedure(schema, "ROUTINE", "SPECIFIC");
    final Procedure missing = spy(newProcedure(schema, "ROUTINE", "SPECIFIC"));
    doReturn(known.key()).when(missing).key();
    doReturn(null).when(missing).getSpecificName();
    assertThat(known.compareTo(missing), greaterThan(0));
    assertThat(missing.compareTo(known), lessThan(0));
  }

  @Test
  public void nullablePropertyFieldsSortFirst() {
    final Property unnamed = mock(Property.class);
    final Property named = mock(Property.class);
    when(named.getName()).thenReturn("name");
    assertThat(Property.NAME_COMPARATOR.compare(named, unnamed), greaterThan(0));
    assertThat(Property.NAME_COMPARATOR.compare(unnamed, named), lessThan(0));

    final Property described = newDatabaseProperty("getDatabaseVersion", "value");
    assertThat(described.compareTo(unnamed), greaterThan(0));
  }

  @Test
  public void nullableIndexTypesSortFirst() {
    final Table table = newTable(new SchemaReference(), "TABLE");
    final Index known = newIndex(table, "INDEX");
    final Index missingType = mock(Index.class);
    when(missingType.getColumns()).thenReturn(List.of());
    assertThat(known.compareTo(missingType), greaterThan(0));
  }

  @Test
  public void nullableColumnReferenceFieldsSortFirst() {
    final Column named = mock(Column.class);
    final Column unnamed = mock(Column.class);
    when(named.getFullName()).thenReturn("COLUMN");
    final ColumnReference known = newColumnReference(0, named, named);
    final ColumnReference missingForeignName = newColumnReference(0, unnamed, named);
    final ColumnReference missingPrimaryName = newColumnReference(0, named, unnamed);
    assertThat(known.compareTo(missingForeignName), greaterThan(0));
    assertThat(missingForeignName.compareTo(known), lessThan(0));
    assertThat(known.compareTo(missingPrimaryName), greaterThan(0));
    assertThat(missingPrimaryName.compareTo(known), lessThan(0));
    assertThat(known.compareTo(mock(ColumnReference.class)), greaterThan(0));
  }

  @Test
  public void nullableTypesSortFirstBeforeNames() {
    final Schema schema = new SchemaReference();
    final Table missingType = spy(newTable(schema, "A"));
    doReturn(null).when(missingType).getType();
    final Table knownType = newTable(schema, "Z");
    setTableType(knownType, new TableType("TABLE"));
    assertThat(knownType, greaterThan(missingType));
    assertThat(missingType, lessThan(knownType));
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
  public void indexComparisonDoesNotReadColumns() {
    final Table table = newTable(new SchemaReference(), "TABLE");
    final Index first = spy(newIndex(table, "A_INDEX"));
    final Index second = spy(newIndex(table, "Z_INDEX"));

    assertThat(first.compareTo(second), lessThan(0));
    assertThat(second.compareTo(first), greaterThan(0));
    verify(first, never()).getColumns();
    verify(second, never()).getColumns();
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

    assertThat(view, greaterThan(null));
    assertThat(tbl, greaterThan(null));

    assertThat(tbl, comparesEqualTo(tbl));
    assertThat(view, comparesEqualTo(view));

    assertThat(tbl, lessThan(view));
    assertThat(view, greaterThan(tbl));
  }

  @Test
  public void compareTablesAndDependantsByFamilyInBothDirections() {
    final Schema schema = new SchemaReference("catalog", "schema");
    final Table table = newTable(schema, "TABLE");
    final Column column = newColumn(table, "COLUMN");
    final Index index = newIndex(table, "INDEX");

    assertThat(schema.compareTo(table), lessThan(0));
    assertThat(table.compareTo(schema), greaterThan(0));
    assertThat(table.compareTo(column), lessThan(0));
    assertThat(column.compareTo(table), greaterThan(0));
    assertThat(table.compareTo(index), lessThan(0));
    assertThat(index.compareTo(table), greaterThan(0));

    final List<List<NamedObject>> permutations =
        List.of(
            List.of(schema, table, column),
            List.of(schema, column, table),
            List.of(table, schema, column),
            List.of(table, column, schema),
            List.of(column, schema, table),
            List.of(column, table, schema));
    for (final List<NamedObject> permutation : permutations) {
      assertThat(
          permutation.stream().sorted(NamedObjectSort.natural).toList(),
          is(List.of(schema, table, column)));
    }
  }

  @Test
  public void dependantComparisonUsesParentBeforeLocalPosition() {
    final Schema schema = new SchemaReference("catalog", "schema");
    final Table firstParent = newTable(schema, "Z_PARENT");
    final Table secondParent = newTable(schema, "A_PARENT");
    setSortIndex(firstParent, 0);
    setSortIndex(secondParent, 1);

    final Column firstColumn = newColumn(firstParent, "Z_COLUMN");
    final Column secondColumn = newColumn(secondParent, "A_COLUMN");
    setOrdinalPosition(firstColumn, 2);
    setOrdinalPosition(secondColumn, 1);

    assertThat(firstColumn.compareTo(secondColumn), lessThan(0));
    assertThat(secondColumn.compareTo(firstColumn), greaterThan(0));
  }

  @Test
  public void routineSpecificNamePrecedesTerminalKey() {
    final Schema schema = new SchemaReference("catalog", "schema");
    final Procedure laterSpecificName = spy(newProcedure(schema, "ROUTINE", "Z_SPECIFIC"));
    final Procedure earlierSpecificName = spy(newProcedure(schema, "ROUTINE", "A_SPECIFIC"));
    doReturn(new NamedObjectKey("a-key")).when(laterSpecificName).key();
    doReturn(new NamedObjectKey("z-key")).when(earlierSpecificName).key();

    assertThat(laterSpecificName.compareTo(earlierSpecificName), greaterThan(0));
    assertThat(earlierSpecificName.compareTo(laterSpecificName), lessThan(0));

    final Procedure sameSpecificNameFirst = spy(newProcedure(schema, "OTHER", "SAME"));
    final Procedure sameSpecificNameSecond = spy(newProcedure(schema, "OTHER", "SAME"));
    doReturn(new NamedObjectKey("a-key")).when(sameSpecificNameFirst).key();
    doReturn(new NamedObjectKey("b-key")).when(sameSpecificNameSecond).key();

    assertThat(sameSpecificNameFirst.compareTo(sameSpecificNameSecond), lessThan(0));
    assertThat(sameSpecificNameSecond.compareTo(sameSpecificNameFirst), greaterThan(0));
  }

  @Test
  public void tableReferenceComparisonDoesNotReadMappings() {
    final Schema schema = new SchemaReference("catalog", "schema");
    final Table child = newTable(schema, "CHILD");
    final Table parent = newTable(schema, "PARENT");
    final ForeignKey first = spy(newForeignKey("FK_A", child, parent, "Z_COLUMN"));
    final ForeignKey second = spy(newForeignKey("FK_Z", child, parent, "A_COLUMN"));

    assertThat(first.compareTo(second), lessThan(0));
    assertThat(second.compareTo(first), greaterThan(0));
    assertThat(NamedObjectSort.natural.compare(first, second), lessThan(0));
    verify(first, never()).getColumnReferences();
    verify(second, never()).getColumnReferences();
    verify(first, never()).getConstrainedColumns();
    verify(second, never()).getConstrainedColumns();
  }

  @Test
  public void implicitAssociationComparisonDoesNotReadMappings() {
    final Schema schema = new SchemaReference("catalog", "schema");
    final Table child = newTable(schema, "CHILD");
    final Table parent = newTable(schema, "PARENT");
    final TableReference first = spy(newImplicitAssociation("IA_A", child, parent, "Z_COLUMN"));
    final TableReference second = spy(newImplicitAssociation("IA_Z", child, parent, "A_COLUMN"));

    assertThat(first.compareTo(second), lessThan(0));
    assertThat(second.compareTo(first), greaterThan(0));
    assertThat(NamedObjectSort.natural.compare(first, second), lessThan(0));
    verify(first, never()).getColumnReferences();
    verify(second, never()).getColumnReferences();
    verify(first, never()).getConstrainedColumns();
    verify(second, never()).getConstrainedColumns();
  }

  @Test
  public void tableReferencesWithIdenticalMappingsSortByName() {
    final Table child = newTable(new SchemaReference("catalog", "schema"), "CHILD");
    final Table parent = newTable(new SchemaReference("catalog", "schema"), "PARENT");
    final ForeignKey first = newForeignKey("FK_A", child, parent);
    final ForeignKey second = newForeignKey("FK_B", child, parent);

    assertThat(first.getColumnReferences(), is(second.getColumnReferences()));
    assertThat(first.compareTo(second), lessThan(0));
    assertThat(second.compareTo(first), greaterThan(0));
  }

  @Test
  public void databaseObject() {
    final SchemaReference schema = new SchemaReference("catalog", "schema");
    final SchemaReference schema1 = new SchemaReference("catalog", "schema1");
    final DatabaseObject tstDbObj1 = newDatabaseObject(schema, "tstDbObj1");
    final DatabaseObject tstDbObj2 = newDatabaseObject(schema, "tstDbObj2");
    final DatabaseObject tstDbObj3 = newDatabaseObject(schema1, "tstDbObj1");

    assertThat(tstDbObj1, greaterThan(null));
    assertThat(tstDbObj2, greaterThan(null));
    assertThat(tstDbObj3, greaterThan(null));

    assertThat(tstDbObj1, lessThan(tstDbObj2));
    assertThat(tstDbObj2, greaterThan(tstDbObj1));

    assertThat(tstDbObj1, lessThan(tstDbObj3));
    assertThat(tstDbObj2, lessThan(tstDbObj3));
    assertThat(tstDbObj3, greaterThan(tstDbObj1));
    assertThat(tstDbObj3, greaterThan(tstDbObj2));

    assertThat(
        tstDbObj1,
        lessThan(
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
