package schemacrawler.ermodel.associations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.Table;
import schemacrawler.test.utility.crawl.LightColumn;
import schemacrawler.test.utility.crawl.LightForeignKey;
import schemacrawler.test.utility.crawl.LightPrimaryKey;
import schemacrawler.test.utility.crawl.LightTable;

public class ImplicitAssociationAnalyzerFkTest {

  @Test
  public void implicitAssociationAddedIfFkDoesNotExist() {
    // Table A (orders) with primary key "id"
    final LightTable tableA = new LightTable("orders");
    final LightColumn pkColumn = column(tableA, "id", true, false);
    tableA.setPrimaryKey(new LightPrimaryKey(pkColumn));

    // Table B (order_items) with column "order_id"
    final LightTable tableB = new LightTable("order_items");
    column(tableB, "order_id", false, false);

    // Analyzer setup
    final List<Table> tables = List.of(tableA, tableB);
    final TableMatchKeys tableMatchKeys = new TableMatchKeys(tables);
    final ImplicitAssociationAnalyzer analyzer =
        new ImplicitAssociationAnalyzer(tableMatchKeys, new IdMatcher());

    // Execute
    final Collection<ColumnReference> implicitAssociations = analyzer.analyzeTables();

    // Verify
    assertThat("Should have one implicit association", implicitAssociations, hasSize(1));
  }

  @Test
  public void implicitAssociationNotAddedIfFkExists() {
    // Table A (orders) with primary key "id"
    final LightTable tableA = new LightTable("orders");
    final LightColumn pkColumn = column(tableA, "id", true, false);
    tableA.setPrimaryKey(new LightPrimaryKey(pkColumn));

    // Table B (order_items) with column "order_id"
    final LightTable tableB = new LightTable("order_items");
    final LightColumn fkColumn = column(tableB, "order_id", false, true);

    // Existing ForeignKey on Table B (order_id)
    final LightForeignKey foreignKey = new LightForeignKey("FK_ORDER_ITEMS", fkColumn, pkColumn);
    tableB.addImportedForeignKey(foreignKey);
    tableB.addForeignKey(foreignKey);

    // Analyzer setup
    final List<Table> tables = List.of(tableA, tableB);
    final TableMatchKeys tableMatchKeys = new TableMatchKeys(tables);
    final ImplicitAssociationAnalyzer analyzer =
        new ImplicitAssociationAnalyzer(tableMatchKeys, new IdMatcher());

    // Execute
    final Collection<ColumnReference> implicitAssociations = analyzer.analyzeTables();

    // Verify
    assertThat(
        "Should not have implicit associations because a real FK exists",
        implicitAssociations,
        empty());
  }

  @Test
  public void singleTableReturnsEmpty() {
    final Table table = new LightTable("orders");
    final List<Table> tables = List.of(table);
    final TableMatchKeys tableMatchKeys = new TableMatchKeys(tables);
    final ImplicitAssociationAnalyzer analyzer =
        new ImplicitAssociationAnalyzer(tableMatchKeys, new IdMatcher());

    assertThat(
        "Single table should yield no implicit associations", analyzer.analyzeTables(), empty());
  }

  private LightColumn column(
      final LightTable parent, final String name, final boolean isPk, final boolean isFk) {
    final LightColumn column = parent.addColumn(name);
    column.setPartOfPrimaryKey(isPk);
    column.setPartOfForeignKey(isFk);
    column.setPartOfUniqueIndex(false);
    return column;
  }
}
