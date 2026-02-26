package schemacrawler.ermodel.associations;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.lang.reflect.Proxy;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import schemacrawler.schema.Column;
import schemacrawler.schema.Identifiers;
import schemacrawler.schema.PartialDatabaseObject;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableConstraintColumn;
import schemacrawler.schema.TableConstraintType;
import schemacrawler.test.utility.crawl.LightColumn;
import schemacrawler.test.utility.crawl.LightTable;

@TestInstance(Lifecycle.PER_CLASS)
public class ImplicitAssociationTest {

  private LightTable fkTable;
  private LightTable pkTable;
  private LightColumn fkColumn;
  private LightColumn pkColumn;
  private ImplicitColumnReference columnRef;
  private ImplicitAssociation implicitAssociation;

  @BeforeAll
  public void setUp() {
    fkTable = new LightTable("FK_TABLE");
    pkTable = new LightTable("PK_TABLE");
    fkColumn = fkTable.addColumn("FK_COL");
    pkColumn = pkTable.addColumn("PK_COL");
    columnRef = new ImplicitColumnReference(fkColumn, pkColumn);
    implicitAssociation = new ImplicitAssociation(columnRef);
  }

  @Test
  public void testImplicitAssociation() {
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

  @Test
  public void testImplicitAssociationAttributedObjectMethods() {
    assertThat(implicitAssociation.hasAttribute("testAttr"), is(false));
    implicitAssociation.setAttribute("testAttr", "testValue");
    assertThat(implicitAssociation.hasAttribute("testAttr"), is(true));
    assertThat(implicitAssociation.getAttribute("testAttr"), is("testValue"));
    assertThat(implicitAssociation.getAttribute("testAttr", "default"), is("testValue"));
    assertThat(implicitAssociation.getAttribute("nonExistent", "default"), is("default"));
    assertThat(implicitAssociation.lookupAttribute("testAttr"), is(Optional.of("testValue")));
    assertThat(implicitAssociation.getAttributes().get("testAttr"), is("testValue"));

    implicitAssociation.removeAttribute("testAttr");
    assertThat(implicitAssociation.hasAttribute("testAttr"), is(false));

    implicitAssociation.setAttribute("nullAttr", null);
    assertThat(implicitAssociation.hasAttribute("nullAttr"), is(false));

    assertThat(implicitAssociation.lookupAttribute(null), is(Optional.empty()));
  }

  @Test
  public void testImplicitAssociationCompareTo() {
    final LightTable fkTable2 = new LightTable("FK_TABLE2");
    final LightTable pkTable2 = new LightTable("PK_TABLE2");
    final LightColumn fkColumn2 = fkTable2.addColumn("FK_COL2");
    final LightColumn pkColumn2 = pkTable2.addColumn("PK_COL2");
    final ImplicitColumnReference columnRef2 = new ImplicitColumnReference(fkColumn2, pkColumn2);
    final ImplicitAssociation implicitAssociation2 = new ImplicitAssociation(columnRef2);

    assertThat(implicitAssociation.compareTo(implicitAssociation2), not(0));
    assertThat(implicitAssociation.compareTo(implicitAssociation), is(0));
  }

  @Test
  public void testImplicitAssociationDescribedObjectMethods() {
    assertThat(implicitAssociation.hasRemarks(), is(false));
    assertThat(implicitAssociation.getRemarks(), is(""));

    implicitAssociation.setRemarks("Test remarks");
    assertThat(implicitAssociation.hasRemarks(), is(true));
    assertThat(implicitAssociation.getRemarks(), is("Test remarks"));

    implicitAssociation.setRemarks(null);
    assertThat(implicitAssociation.getRemarks(), is(""));
    assertThat(implicitAssociation.hasRemarks(), is(false));
  }

  @Test
  public void testImplicitAssociationFlags() {
    // Default: not optional (fkColumn.isNullable() is false)
    assertThat(implicitAssociation.isOptional(), is(false));

    // Optional: need a nullable column
    final Column nullableFkColumn =
        (Column)
            Proxy.newProxyInstance(
                Column.class.getClassLoader(),
                new Class[] {Column.class},
                (proxy, method, args) -> {
                  if (method.getName().equals("equals")) {
                    return proxy == args[0];
                  }
                  if (method.getName().equals("hashCode")) {
                    return System.identityHashCode(proxy);
                  }
                  if (method.getName().equals("isNullable")) {
                    return true;
                  }
                  if (method.getName().equals("getParent")) {
                    return fkTable;
                  }
                  if (method.getName().equals("getFullName")) {
                    return "FK_TABLE.FK_COL";
                  }
                  if (method.getName().equals("getSchema")) {
                    return fkTable.getSchema();
                  }
                  if (method.getName().equals("getName")) {
                    return "FK_COL";
                  }
                  return null;
                });
    final ImplicitColumnReference optionalColumnRef =
        new ImplicitColumnReference(nullableFkColumn, pkColumn);
    final ImplicitAssociation optionalImplicitAssociation =
        new ImplicitAssociation(optionalColumnRef);
    assertThat(optionalImplicitAssociation.isOptional(), is(true));

    // Self-referencing
    final LightColumn selfFkColumn = pkTable.addColumn("SELF_FK");
    final ImplicitColumnReference selfColumnRef =
        new ImplicitColumnReference(selfFkColumn, pkColumn);
    final ImplicitAssociation selfImplicitAssociation = new ImplicitAssociation(selfColumnRef);
    assertThat(selfImplicitAssociation.isSelfReferencing(), is(true));
  }

  @Test
  public void testImplicitAssociationNamedObjectMethods() {
    assertThat(implicitAssociation.getName(), startsWith("SCHCRWLR_"));
    assertThat(implicitAssociation.getShortName(), is(implicitAssociation.getName()));
    assertThat(
        implicitAssociation.getFullName(),
        is(Identifiers.STANDARD.quoteFullName(implicitAssociation)));
    assertThat(implicitAssociation.key(), notNullValue());
    assertThat(implicitAssociation.toString(), is(implicitAssociation.getFullName()));

    final ImplicitAssociation same = new ImplicitAssociation(columnRef);
    assertThat(implicitAssociation.compareTo(same), is(0));
    assertThat(implicitAssociation.compareTo(null), is(-1));

    // Test with quoting
    implicitAssociation.withQuoting(Identifiers.STANDARD);
    assertThat(
        implicitAssociation.getFullName(),
        is(Identifiers.STANDARD.quoteFullName(implicitAssociation)));

    // Test compareTo with non-TableReference NamedObject
    assertThat(implicitAssociation.compareTo(fkTable), not(0));
  }

  @Test
  public void testImplicitAssociationOtherMethods() {
    assertThat(implicitAssociation.getDefinition(), is(""));
    assertThat(implicitAssociation.hasDefinition(), is(false));
    assertThat(implicitAssociation.isDeferrable(), is(false));
    assertThat(implicitAssociation.isInitiallyDeferred(), is(false));
    assertThat(implicitAssociation.isParentPartial(), is(false));
    assertThat(implicitAssociation.iterator().hasNext(), is(true));
    assertThat(implicitAssociation.iterator().next(), is(columnRef));
    assertThat(implicitAssociation.getParent(), is(fkTable));
    assertThat(implicitAssociation.getSchema(), is(fkTable.getSchema()));

    assertThat(implicitAssociation.hashCode(), is(columnRef.hashCode()));
  }

  @Test
  public void testIsParentPartial() {
    final Table partialTable =
        (Table)
            Proxy.newProxyInstance(
                Table.class.getClassLoader(),
                new Class[] {Table.class, PartialDatabaseObject.class},
                (proxy, method, args) -> {
                  if (method.getName().equals("equals")) {
                    return proxy == args[0];
                  }
                  if (method.getName().equals("hashCode")) {
                    return System.identityHashCode(proxy);
                  }
                  if (method.getName().equals("getSchema")) {
                    return new LightTable("PARTIAL_TABLE").getSchema();
                  }
                  if (method.getName().equals("getFullName")) {
                    return "PARTIAL_TABLE";
                  }
                  return null;
                });
    final LightTable pkTable = new LightTable("PK_TABLE");
    final LightColumn pkColumn = pkTable.addColumn("PK_COL");

    final Column partialColumn =
        (Column)
            Proxy.newProxyInstance(
                Column.class.getClassLoader(),
                new Class[] {Column.class},
                (proxy, method, args) -> {
                  if (method.getName().equals("equals")) {
                    return proxy == args[0];
                  }
                  if (method.getName().equals("hashCode")) {
                    return System.identityHashCode(proxy);
                  }
                  if (method.getName().equals("getParent")) {
                    return partialTable;
                  }
                  if (method.getName().equals("getFullName")) {
                    return "PARTIAL_TABLE.COL";
                  }
                  if (method.getName().equals("isNullable")) {
                    return false;
                  }
                  return null;
                });

    final ImplicitColumnReference columnRef = new ImplicitColumnReference(partialColumn, pkColumn);
    final ImplicitAssociation implicitAssociation = new ImplicitAssociation(columnRef);
    assertThat(implicitAssociation.isParentPartial(), is(true));
  }
}
