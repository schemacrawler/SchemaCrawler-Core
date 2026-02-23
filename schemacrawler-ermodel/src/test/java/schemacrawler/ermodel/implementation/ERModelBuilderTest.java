package schemacrawler.ermodel.implementation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.PrimaryKey;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableConstraintColumn;
import schemacrawler.test.utility.crawl.LightTable;

public class ERModelBuilderTest {

  @Test
  public void testConcurrentModificationException() {
    final Table superTable = spy(new LightTable("SUPER_TABLE"));
    when(superTable.hasPrimaryKey()).thenReturn(true);
    when(superTable.getImportedForeignKeys()).thenReturn(Collections.emptyList());

    final PrimaryKey superPk = mock(PrimaryKey.class);
    final TableConstraintColumn superPkCol = mock(TableConstraintColumn.class);
    when(superPk.getConstrainedColumns()).thenReturn(List.of(superPkCol));
    when(superTable.getPrimaryKey()).thenReturn(superPk);

    final Table subTable = spy(new LightTable("SUB_TABLE"));
    when(subTable.hasPrimaryKey()).thenReturn(true);

    final TableConstraintColumn subPkCol = mock(TableConstraintColumn.class);
    final PrimaryKey subPk = mock(PrimaryKey.class);
    when(subPk.getConstrainedColumns()).thenReturn(List.of(subPkCol));
    when(subTable.getPrimaryKey()).thenReturn(subPk);

    final ForeignKey fk = spy(ForeignKey.class);
    final NamedObjectKey fkKey = mock(NamedObjectKey.class);
    when(fk.key()).thenReturn(fkKey);
    when(fk.getPrimaryKeyTable()).thenReturn(superTable);
    when(fk.getForeignKeyTable()).thenReturn(subTable);

    final ColumnReference colRef = mock(ColumnReference.class);
    when(colRef.getPrimaryKeyColumn()).thenReturn(superPkCol);
    when(colRef.getForeignKeyColumn()).thenReturn(subPkCol);
    when(fk.getColumnReferences()).thenReturn(List.of(colRef));

    when(subTable.getImportedForeignKeys()).thenReturn(List.of(fk));
    // TableEntityModelInferrer needs foreign keys too
    when(subTable.getForeignKeys()).thenReturn(Collections.emptyList());
    when(superTable.getForeignKeys()).thenReturn(Collections.emptyList());

    final Catalog catalog = mock(Catalog.class);
    when(catalog.getTables()).thenReturn(Arrays.asList(subTable, superTable));

    final ERModelBuilder builder = new ERModelBuilder(catalog);
    final ERModel erModel = assertDoesNotThrow(() -> builder.build());
    assertNotNull(erModel);
  }
}
