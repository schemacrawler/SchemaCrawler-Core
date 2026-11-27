package schemacrawler.ermodel.associations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.Column;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.test.utility.crawl.LightColumn;
import schemacrawler.test.utility.crawl.LightColumnReference;
import schemacrawler.test.utility.crawl.LightForeignKey;
import schemacrawler.test.utility.crawl.LightTable;

public class ExtensionTableMatcherTest {

  @Test
  public void ranksCandidatesBySchemaAndIncomingReferences() {
    final Schema schema1 = new SchemaReference("CAT", "S1");
    final Schema schema2 = new SchemaReference("CAT", "S2");

    final LightTable pkTop1 = new LightTable(schema1, "core_user");
    final LightTable pkTop2 = new LightTable(schema1, "app_user");
    final LightTable pkLow = new LightTable(schema1, "test_user");
    final LightTable fkTable = new LightTable(schema1, "zz_user_ext");

    final LightTable pkSchema2 = new LightTable(schema2, "core_user");
    final LightTable fkTableSchema2 = new LightTable(schema2, "zz_user_ext");

    final Column fkColumn = column(fkTable, "USER_ID", false, true);
    final Column pkColumnTop1 = column(pkTop1, "USER_ID", true, false);
    final Column pkColumnTop2 = column(pkTop2, "USER_ID", true, false);
    final Column pkColumnLow = column(pkLow, "USER_ID", true, false);

    final Column fkColumnSchema2 = column(fkTableSchema2, "USER_ID", false, true);
    final Column pkColumnSchema2 = column(pkSchema2, "USER_ID", true, false);

    final Table ref1 = tableWithForeignKeys(schema1, "orders", pkTop1);
    final Table ref2 = tableWithForeignKeys(schema1, "audit", pkTop1);
    final Table ref3 = tableWithForeignKeys(schema1, "billing", pkTop2);
    final Table ref4 = tableWithForeignKeys(schema1, "sessions", pkTop2);
    final Table ref5 = tableWithForeignKeys(schema1, "logs", pkLow);

    final Table refSchema2 =
        tableWithForeignKeys(schema2, "s2_orders", pkSchema2, pkSchema2, pkSchema2);

    final List<Table> tables =
        List.of(
            pkTop1,
            pkTop2,
            pkLow,
            fkTable,
            ref1,
            ref2,
            ref3,
            ref4,
            ref5,
            pkSchema2,
            fkTableSchema2,
            refSchema2);

    final ExtensionTableMatcher matcher = new ExtensionTableMatcher(new TableMatchKeys(tables));

    assertThat(matcher.test(new LightColumnReference(fkColumn, pkColumnTop1)), is(true));
    assertThat(matcher.test(new LightColumnReference(fkColumn, pkColumnTop2)), is(true));
    assertThat(matcher.test(new LightColumnReference(fkColumn, pkColumnLow)), is(true));
    assertThat(matcher.test(new LightColumnReference(fkColumnSchema2, pkColumnSchema2)), is(true));
  }

  private LightColumn column(
      final LightTable parent,
      final String name,
      final boolean partOfPrimaryKey,
      final boolean partOfUniqueIndex) {
    final LightColumn column = parent.addColumn(name);
    column.setPartOfPrimaryKey(partOfPrimaryKey);
    column.setPartOfUniqueIndex(partOfUniqueIndex);
    return column;
  }

  /** A table with one foreign key to each of the given tables. */
  private Table tableWithForeignKeys(
      final Schema schema, final String name, final Table... pkTables) {
    final LightTable table = new LightTable(schema, name);
    int count = 0;
    for (final Table pkTable : pkTables) {
      count++;
      table.addForeignKey(new LightForeignKey("FK_%s_%d".formatted(name, count), table, pkTable));
    }
    return table;
  }
}
