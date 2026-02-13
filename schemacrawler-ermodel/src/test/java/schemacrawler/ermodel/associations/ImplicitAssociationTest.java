package schemacrawler.ermodel.associations;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableConstraintColumn;
import schemacrawler.schema.TableConstraintType;

public class ImplicitAssociationTest {

  @Test
  public void testImplicitAssociation() {
    final Table fkTable = mock(Table.class);
    when(fkTable.getName()).thenReturn("FK_TABLE");
    final Table pkTable = mock(Table.class);
    when(pkTable.getName()).thenReturn("PK_TABLE");

    final Column fkColumn = mock(Column.class);
    when(fkColumn.getName()).thenReturn("FK_COL");
    when(fkColumn.getFullName()).thenReturn("FK_COL");
    when(fkColumn.getParent()).thenReturn(fkTable);

    final Column pkColumn = mock(Column.class);
    when(pkColumn.getName()).thenReturn("PK_COL");
    when(pkColumn.getFullName()).thenReturn("PK_COL");
    when(pkColumn.getParent()).thenReturn(pkTable);

    final ImplicitColumnReference columnRef = new ImplicitColumnReference(fkColumn, pkColumn);
    final ImplicitAssociation implicitAssociation = new ImplicitAssociation(columnRef);

    assertThat(implicitAssociation.getName(), startsWith("SCHCRWLR_"));
    assertThat(implicitAssociation.getType(), is(TableConstraintType.unknown));
    assertThat(implicitAssociation.getForeignKeyTable(), is(fkTable));
    assertThat(implicitAssociation.getPrimaryKeyTable(), is(pkTable));
    assertThat(implicitAssociation.getColumnReferences().size(), is(1));
    assertThat(implicitAssociation.getColumnReferences().get(0), is(columnRef));

    final TableConstraintColumn tcc = implicitAssociation.getConstrainedColumns().get(0);
    assertThat(tcc, notNullValue());
    assertThat(tcc.getTableConstraint(), is(implicitAssociation));
    assertThat(tcc.getTableConstraintOrdinalPosition(), is(1));
    assertThat(tcc.getName(), is("FK_COL"));
  }
}
