package schemacrawler.ermodel.implementation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static schemacrawler.ermodel.model.EntityType.strong_entity;

import java.io.Serial;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import schemacrawler.namedobject.AbstractNamedObject;
import schemacrawler.schema.Table;
import schemacrawler.test.utility.crawl.LightForeignKey;
import schemacrawler.test.utility.crawl.LightTable;

public class ERModelImplementationTest {

  @Test
  public void testAbstractTableBacked() {
    final LightTable table = new LightTable("TEST_TABLE");
    table.setRemarks("Table remarks");
    table.setAttribute("attr1", "value1");

    final AbstractTableBacked tableBacked = new MutableEntity(table, strong_entity);

    // NamedObject methods
    assertThat(tableBacked.getName(), is("TEST_TABLE"));
    assertThat(tableBacked.getFullName(), is("TEST_TABLE"));
    assertThat(tableBacked.key(), is(table.key()));

    // DescribedObject methods
    assertThat(tableBacked.hasRemarks(), is(true));
    assertThat(tableBacked.getRemarks(), is("Table remarks"));
    tableBacked.setRemarks("New remarks");
    assertThat(tableBacked.getRemarks(), is("New remarks"));
    assertThat(table.getRemarks(), is("New remarks"));

    // AttributedObject methods
    assertThat(tableBacked.hasAttribute("attr1"), is(true));
    assertThat(tableBacked.getAttribute("attr1"), is("value1"));
    assertThat(tableBacked.getAttribute("attr1", "default"), is("value1"));
    assertThat(tableBacked.getAttribute("nonexistent"), nullValue());
    assertThat(tableBacked.getAttribute("nonexistent", "default"), is("default"));

    tableBacked.setAttribute("attr2", "value2");
    assertThat(tableBacked.getAttribute("attr2"), is("value2"));
    assertThat(tableBacked.getAttributes().get("attr2"), is("value2"));

    assertThat(tableBacked.lookupAttribute("attr1"), is(Optional.of("value1")));
    assertThat(tableBacked.lookupAttribute("nonexistent"), is(Optional.empty()));

    tableBacked.removeAttribute("attr1");
    assertThat(tableBacked.hasAttribute("attr1"), is(false));

    // Other methods
    assertThat(tableBacked.getTable(), is((Table) table));
    assertThat(tableBacked.toString(), containsString("TEST_TABLE"));
    assertThat(tableBacked.getEntityAttributes(), notNullValue());
  }

  @Test
  public void testTableBackedComparisonWithGenericNamedObjects() {
    final MutableEntity wrapped = new MutableEntity(new LightTable("Z_WRAPPED"), strong_entity);
    final GenericNamedObject generic = new GenericNamedObject("A_GENERIC");

    assertThat(wrapped.compareTo(generic), greaterThan(0));
    assertThat(generic.compareTo(wrapped), lessThan(0));

    final MutableEntity wrappedCollision = new MutableEntity(new LightTable("A-B"), strong_entity);
    final GenericNamedObject genericCollision = new GenericNamedObject("A B");
    final int wrapperComparison = wrappedCollision.compareTo(genericCollision);
    final int genericComparison = genericCollision.compareTo(wrappedCollision);

    assertThat(Integer.signum(wrapperComparison), is(-Integer.signum(genericComparison)));
    assertThat(wrappedCollision.key().compareTo(genericCollision.key()) == 0, is(false));
  }

  @Test
  public void testMutableManyToManyRelationshipNamedObjectMethods() {
    final LightTable table1 = new LightTable("TABLE1");
    final MutableManyToManyRelationship rel1 = new MutableManyToManyRelationship(table1);

    final LightTable table2 = new LightTable("TABLE2");
    final MutableManyToManyRelationship rel2 = new MutableManyToManyRelationship(table2);

    // compareTo
    assertThat(rel1.compareTo(rel2) < 0, is(true));
    assertThat(rel2.compareTo(rel1) > 0, is(true));
    assertThat(rel1.compareTo(rel1), is(0));
    assertThat(rel1.compareTo(null), greaterThan(0));

    // equals
    assertThat(rel1.equals(rel1), is(true));
    assertThat(rel1.equals(rel2), is(false));
    assertThat(rel1.equals(null), is(false));
    assertThat("not a named object".equals(rel1), is(false));

    // hashCode
    assertThat(rel1.hashCode(), is(table1.key().hashCode()));
  }

  @Test
  public void testMutableTableReferenceRelationship() {
    final LightTable pkTable = new LightTable("PK_TABLE");
    final LightTable fkTable = new LightTable("FK_TABLE");
    final LightForeignKey tableRef = new LightForeignKey("FK_PK", fkTable, pkTable);
    tableRef.setRemarks("Relationship remarks");
    tableRef.setAttribute("attr1", "value1");

    final MutableTableReferenceRelationship rel = new MutableTableReferenceRelationship(tableRef);

    // NamedObject methods
    assertThat(rel.getName(), is("FK_PK"));
    assertThat(rel.getFullName(), is("FK_PK"));
    assertThat(rel.key(), is(tableRef.key()));

    // DescribedObject methods
    assertThat(rel.hasRemarks(), is(true));
    assertThat(rel.getRemarks(), is("Relationship remarks"));
    rel.setRemarks("New remarks");
    assertThat(rel.getRemarks(), is("New remarks"));
    assertThat(tableRef.getRemarks(), is("New remarks"));

    // AttributedObject methods
    assertThat(rel.hasAttribute("attr1"), is(true));
    assertThat(rel.getAttribute("attr1"), is("value1"));
    assertThat(rel.getAttribute("attr1", "default"), is("value1"));
    assertThat(rel.getAttribute("nonexistent"), nullValue());
    assertThat(rel.getAttribute("nonexistent", "default"), is("default"));

    rel.setAttribute("attr2", "value2");
    assertThat(rel.getAttribute("attr2"), is("value2"));
    assertThat(rel.getAttributes().get("attr2"), is("value2"));

    assertThat(rel.lookupAttribute("attr1"), is(Optional.of("value1")));
    assertThat(rel.lookupAttribute("nonexistent"), is(Optional.empty()));

    rel.removeAttribute("attr1");
    assertThat(rel.hasAttribute("attr1"), is(false));

    // NamedObject methods (compareTo, equals, hashCode)
    final LightForeignKey tableRef2 = new LightForeignKey("FK_PK2", fkTable, pkTable);
    final MutableTableReferenceRelationship rel2 = new MutableTableReferenceRelationship(tableRef2);

    // compareTo
    assertThat(rel.compareTo(rel2) < 0, is(true));
    assertThat(rel2.compareTo(rel) > 0, is(true));
    assertThat(rel.compareTo(rel), is(0));
    assertThat(rel.compareTo(null), greaterThan(0));

    // equals
    assertThat(rel.equals(rel), is(true));
    assertThat(rel.equals(rel2), is(false));
    assertThat(rel.equals(null), is(false));
    assertThat("not a named object".equals(rel), is(false));

    // hashCode
    assertThat(rel.hashCode(), is(tableRef.key().hashCode()));
  }

  private static final class GenericNamedObject extends AbstractNamedObject {

    @Serial private static final long serialVersionUID = -3178421622581031907L;

    GenericNamedObject(final String name) {
      super(name);
    }
  }
}
