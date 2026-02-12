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

public class WeakAssociationTest {

  @Test
  public void testWeakAssociation() {
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

    final WeakColumnReference columnRef = new WeakColumnReference(fkColumn, pkColumn);
    final WeakAssociation weakAssociation = new WeakAssociation(columnRef);

    assertThat(weakAssociation.getName(), startsWith("SCHCRWLR_"));
    assertThat(weakAssociation.getType(), is(TableConstraintType.unknown));
    assertThat(weakAssociation.getForeignKeyTable(), is(fkTable));
    assertThat(weakAssociation.getPrimaryKeyTable(), is(pkTable));
    assertThat(weakAssociation.getColumnReferences().size(), is(1));
    assertThat(weakAssociation.getColumnReferences().get(0), is(columnRef));

    final TableConstraintColumn tcc = weakAssociation.getConstrainedColumns().get(0);
    assertThat(tcc, notNullValue());
    assertThat(tcc.getTableConstraint(), is(weakAssociation));
    assertThat(tcc.getTableConstraintOrdinalPosition(), is(1));
    assertThat(tcc.getName(), is("FK_COL"));
  }
}
