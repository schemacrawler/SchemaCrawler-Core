package schemacrawler.ermodel.associations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.Column;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaReference;
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

    final Column fkColumn = mockColumn(fkTable, "USER_ID", false, true);
    final Column pkColumnTop1 = mockColumn(pkTop1, "USER_ID", true, false);
    final Column pkColumnTop2 = mockColumn(pkTop2, "USER_ID", true, false);
    final Column pkColumnLow = mockColumn(pkLow, "USER_ID", true, false);

    final Column fkColumnSchema2 = mockColumn(fkTableSchema2, "USER_ID", false, true);
    final Column pkColumnSchema2 = mockColumn(pkSchema2, "USER_ID", true, false);

    final ForeignKey fkToTop1A = mockForeignKey(pkTop1);
    final ForeignKey fkToTop1B = mockForeignKey(pkTop1);
    final ForeignKey fkToTop2A = mockForeignKey(pkTop2);
    final ForeignKey fkToTop2B = mockForeignKey(pkTop2);
    final ForeignKey fkToLow = mockForeignKey(pkLow);

    final Table ref1 = mockTableWithForeignKeys(schema1, "orders", List.of(fkToTop1A));
    final Table ref2 = mockTableWithForeignKeys(schema1, "audit", List.of(fkToTop1B));
    final Table ref3 = mockTableWithForeignKeys(schema1, "billing", List.of(fkToTop2A));
    final Table ref4 = mockTableWithForeignKeys(schema1, "sessions", List.of(fkToTop2B));
    final Table ref5 = mockTableWithForeignKeys(schema1, "logs", List.of(fkToLow));

    final ForeignKey fkToSchema2A = mockForeignKey(pkSchema2);
    final ForeignKey fkToSchema2B = mockForeignKey(pkSchema2);
    final ForeignKey fkToSchema2C = mockForeignKey(pkSchema2);
    final Table refSchema2 =
        mockTableWithForeignKeys(
            schema2, "s2_orders", List.of(fkToSchema2A, fkToSchema2B, fkToSchema2C));

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

    assertThat(matcher.test(new ImplicitColumnReference(fkColumn, pkColumnTop1)), is(true));
    assertThat(matcher.test(new ImplicitColumnReference(fkColumn, pkColumnTop2)), is(true));
    assertThat(matcher.test(new ImplicitColumnReference(fkColumn, pkColumnLow)), is(true));
    assertThat(
        matcher.test(new ImplicitColumnReference(fkColumnSchema2, pkColumnSchema2)), is(true));
  }

  private Column mockColumn(
      final Table parent,
      final String name,
      final boolean partOfPrimaryKey,
      final boolean partOfUniqueIndex) {
    final Column column = mock(Column.class);
    when(column.getParent()).thenReturn(parent);
    when(column.getName()).thenReturn(name);
    when(column.isPartOfPrimaryKey()).thenReturn(partOfPrimaryKey);
    when(column.isPartOfUniqueIndex()).thenReturn(partOfUniqueIndex);
    return column;
  }

  private ForeignKey mockForeignKey(final Table pkTable) {
    final ForeignKey foreignKey = mock(ForeignKey.class);
    when(foreignKey.getPrimaryKeyTable()).thenReturn(pkTable);
    return foreignKey;
  }

  private Table mockTableWithForeignKeys(
      final Schema schema, final String name, final List<ForeignKey> foreignKeys) {
    final Table table = mock(Table.class);
    when(table.getSchema()).thenReturn(schema);
    when(table.getName()).thenReturn(name);
    when(table.getForeignKeys()).thenReturn(foreignKeys);
    return table;
  }
}
