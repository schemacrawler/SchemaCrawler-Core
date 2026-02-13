package schemacrawler.ermodel.associations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static schemacrawler.test.utility.crawl.LightColumnDataTypeUtility.columnDataType;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.PrimaryKey;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableConstraintColumn;

public class ImplicitAssociationsAnalyzerFkTest {

  @Test
  public void implicitAssociationAddedIfFkDoesNotExist() {
    // Table A (orders) with primary key "id"
    final Table tableA = mockTable("orders");
    final Column pkColumn = mockColumn(tableA, "id", true, false);
    mockPrimaryKey(tableA, pkColumn);

    // Table B (order_items) with column "order_id"
    final Table tableB = mockTable("order_items");
    final Column fkColumn = mockColumn(tableB, "order_id", false, false);
    when(tableB.getColumns()).thenReturn(List.of(fkColumn));

    // Analyzer setup
    final List<Table> tables = List.of(tableA, tableB);
    final TableMatchKeys tableMatchKeys = new TableMatchKeys(tables);
    final ImplicitAssociationsAnalyzer analyzer =
        new ImplicitAssociationsAnalyzer(tableMatchKeys, new IdMatcher());

    // Execute
    final Collection<ImplicitColumnReference> implicitAssociations = analyzer.analyzeTables();

    // Verify
    assertThat("Should have one implicit association", implicitAssociations, hasSize(1));
  }

  @Test
  public void implicitAssociationNotAddedIfFkExists() {
    // Table A (orders) with primary key "id"
    final Table tableA = mockTable("orders");
    final Column pkColumn = mockColumn(tableA, "id", true, false);
    mockPrimaryKey(tableA, pkColumn);

    // Table B (order_items) with column "order_id"
    final Table tableB = mockTable("order_items");
    final Column fkColumn = mockColumn(tableB, "order_id", false, true);
    when(tableB.getColumns()).thenReturn(List.of(fkColumn));

    // Existing ForeignKey on Table B (order_id)
    final ForeignKey foreignKey = mock(ForeignKey.class);
    final ColumnReference fkColRef = mock(ColumnReference.class);
    when(fkColRef.getForeignKeyColumn()).thenReturn(fkColumn);
    when(foreignKey.getColumnReferences()).thenReturn(List.of(fkColRef));
    when(tableB.getImportedForeignKeys()).thenReturn(List.of(foreignKey));
    when(tableB.getForeignKeys()).thenReturn(List.of(foreignKey)); // Added this

    // Analyzer setup
    final List<Table> tables = List.of(tableA, tableB);
    final TableMatchKeys tableMatchKeys = new TableMatchKeys(tables);
    final ImplicitAssociationsAnalyzer analyzer =
        new ImplicitAssociationsAnalyzer(tableMatchKeys, new IdMatcher());

    // Execute
    final Collection<ImplicitColumnReference> implicitAssociations = analyzer.analyzeTables();

    // Verify
    assertThat(
        "Should not have implicit associations because a real FK exists",
        implicitAssociations,
        empty());
  }

  private Column mockColumn(
      final Table parent, final String name, final boolean isPk, final boolean isFk) {
    final String parentName = parent.getName();
    final Column column;
    if (isPk) {
      column = mock(TableConstraintColumn.class, name);
    } else {
      column = mock(Column.class, name);
    }
    when(column.isPartOfPrimaryKey()).thenReturn(isPk);
    when(column.isPartOfForeignKey()).thenReturn(isFk);
    when(column.getParent()).thenReturn(parent);
    when(column.getName()).thenReturn(name);
    when(column.getFullName()).thenReturn(parentName + "." + name);
    when(column.isColumnDataTypeKnown()).thenReturn(true);
    when(column.isPartOfPrimaryKey()).thenReturn(isPk);
    when(column.getColumnDataType()).thenReturn(columnDataType("INTEGER"));

    // Simulate NamedObjectKey
    final NamedObjectKey key = new NamedObjectKey(null, null, parentName).with(name);
    when(column.key()).thenReturn(key);

    return column;
  }

  private void mockPrimaryKey(final Table table, final Column pkColumn) {
    final PrimaryKey primaryKey = mock(PrimaryKey.class, "PK_" + table.getName());
    when(primaryKey.getConstrainedColumns()).thenReturn(List.of((TableConstraintColumn) pkColumn));
    when(table.getPrimaryKey()).thenReturn(primaryKey);
  }

  private Table mockTable(final String name) {
    final Table table = mock(Table.class, name);
    when(table.getName()).thenReturn(name);
    when(table.getFullName()).thenReturn(name);
    when(table.key()).thenReturn(new NamedObjectKey(name));
    when(table.getForeignKeys()).thenReturn(List.of());
    when(table.getImportedForeignKeys()).thenReturn(List.of());
    when(table.getExportedForeignKeys()).thenReturn(List.of());
    when(table.getIndexes()).thenReturn(List.of());
    when(table.getColumns()).thenReturn(List.of());
    return table;
  }
}
