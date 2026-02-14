package schemacrawler.ermodel.associations;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;
import schemacrawler.schema.TableConstraintColumn;
import schemacrawler.schema.TableConstraintType;
import schemacrawler.test.utility.crawl.LightColumn;
import schemacrawler.test.utility.crawl.LightTable;

public class ImplicitAssociationTest {

  @Test
  public void testImplicitAssociation() {
    final LightTable fkTable = new LightTable("FK_TABLE");
    final LightTable pkTable = new LightTable("PK_TABLE");

    final LightColumn fkColumn = fkTable.addColumn("FK_COL");
    final LightColumn pkColumn = pkTable.addColumn("PK_COL");

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
