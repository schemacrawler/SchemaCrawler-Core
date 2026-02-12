package schemacrawler.ermodel.implementation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.ermodel.model.RelationshipCardinality;
import schemacrawler.schema.TableReference;
import schemacrawler.schemacrawler.exceptions.ExecutionRuntimeException;
import schemacrawler.test.utility.crawl.LightTable;
import schemacrawler.test.utility.crawl.LightTableReference;

public class MutableERModelTest {

  @Test
  public void testMutableEntity() {
    final LightTable table = new LightTable("TABLE");
    final MutableEntity entity = new MutableEntity(table);

    assertThat(entity.getType(), is(EntityType.unknown));
    entity.setEntityType(EntityType.strong_entity);
    assertThat(entity.getType(), is(EntityType.strong_entity));

    // No change when null is set
    entity.setEntityType(null);
    assertThat(entity.getType(), is(EntityType.strong_entity));

    // Possible to set to any type of entity
    entity.setEntityType(EntityType.unknown);
    assertThat(entity.getType(), is(EntityType.unknown));
  }

  @Test
  public void testMutableEntitySubtype() {
    final LightTable superTable = new LightTable("SUPER");
    final MutableEntity superEntity = new MutableEntity(superTable);
    superEntity.setEntityType(EntityType.strong_entity);

    final LightTable subTable = new LightTable("SUB");
    final MutableEntitySubtype subEntity = new MutableEntitySubtype(subTable);
    subEntity.setEntityType(EntityType.subtype);
    subEntity.setSupertype(superEntity);

    assertThat(subEntity.getType(), is(EntityType.subtype));
    assertThat(subEntity.getSupertype(), is(superEntity));

    final MutableERModel model = new MutableERModel();
    model.addEntity(superEntity);
    model.addEntity(subEntity);

    assertThat(model.getSubtypesOf(superEntity), containsInAnyOrder(subEntity));
    assertThat(model.getSubtypesOf(subEntity), is(empty()));
  }

  @Test
  public void testMutableERModel() {
    final MutableERModel model = new MutableERModel();

    final LightTable table1 = new LightTable("TABLE1");
    final MutableEntity entity1 = new MutableEntity(table1);
    entity1.setEntityType(EntityType.strong_entity);

    final LightTable table2 = new LightTable("TABLE2");
    final MutableEntity entity2 = new MutableEntity(table2);
    entity2.setEntityType(EntityType.weak_entity);

    model.addTable(table1);
    model.addEntity(entity1);
    model.addTable(table2);
    model.addEntity(entity2);

    assertThat(model.getTables(), containsInAnyOrder(table1, table2));
    assertThat(model.getEntities(), containsInAnyOrder(entity1, entity2));

    assertThat(model.getEntitiesByType(EntityType.strong_entity), containsInAnyOrder(entity1));
    assertThat(model.getEntitiesByType(EntityType.weak_entity), containsInAnyOrder(entity2));
    assertThat(model.getEntitiesByType(EntityType.unknown), is(empty()));

    assertThat(model.lookupEntity("TABLE1").isPresent(), is(true));
    assertThat(model.lookupEntity("TABLE1").get(), is(entity1));
    assertThat(model.lookupEntity(table2).isPresent(), is(true));
    assertThat(model.lookupEntity(table2).get(), is(entity2));
  }

  @Test
  public void testMutableManyToManyRelationship() {
    final LightTable bridgeTable = new LightTable("BRIDGE");
    final MutableManyToManyRelationship rel = new MutableManyToManyRelationship(bridgeTable);

    final LightTable table1 = new LightTable("T1");
    final MutableEntity entity1 = new MutableEntity(table1);
    final LightTable table2 = new LightTable("T2");
    final MutableEntity entity2 = new MutableEntity(table2);

    rel.setLeftEntity(entity1);
    rel.setRightEntity(entity2);

    assertThat(rel.getType(), is(RelationshipCardinality.many_many));
    assertThat(rel.getLeftEntity(), is(entity1));
    assertThat(rel.getRightEntity(), is(entity2));

    final MutableERModel model = new MutableERModel();
    model.addRelationship(rel);

    assertThat(model.getRelationships(), containsInAnyOrder(rel));
    assertThat(
        model.getRelationshipsByType(RelationshipCardinality.many_many), containsInAnyOrder(rel));
    assertThat(model.lookupByBridgeTable(bridgeTable).get(), is(rel));
  }

  @Test
  public void testTableReferenceLookups() {
    final MutableERModel model = new MutableERModel();

    final LightTable pkTable = new LightTable("PK_TABLE");
    final LightTable fkTable = new LightTable("FK_TABLE");
    final LightTableReference tableRef = new LightTableReference("FK_PK", fkTable, pkTable);
    final MutableTableReferenceRelationship rel = new MutableTableReferenceRelationship(tableRef);

    model.addRelationship(rel);

    assertThat(model.lookupRelationship(tableRef).isPresent(), is(true));
    assertThat(model.lookupRelationship(tableRef).get(), is(rel));
    assertThat(model.lookupRelationship("FK_PK").isPresent(), is(true));
    assertThat(model.lookupRelationship("FK_PK").get(), is(rel));

    assertThat(model.lookupRelationship((String) null).isPresent(), is(false));
    assertThat(model.lookupRelationship((TableReference) null).isPresent(), is(false));
    assertThat(model.lookupRelationship("NONEXISTENT").isPresent(), is(false));
  }

  @Test
  public void testMutableTableReferenceRelationship() {
    final LightTable pkTable = new LightTable("PK_TABLE");
    pkTable.addColumn("ID");
    final LightTable fkTable = new LightTable("FK_TABLE");
    fkTable.addColumn("PK_ID");

    final LightTableReference mockRef = new LightTableReference("FK_PK", fkTable, pkTable);

    final MutableTableReferenceRelationship rel = new MutableTableReferenceRelationship(mockRef);

    final MutableEntity pkEntity = new MutableEntity(pkTable);
    final MutableEntity fkEntity = new MutableEntity(fkTable);

    rel.setLeftEntity(fkEntity);
    rel.setRightEntity(pkEntity);

    assertThat(rel.getLeftEntity(), is(fkEntity));
    assertThat(rel.getRightEntity(), is(pkEntity));
    assertThat(rel.getTableReference(), is(mockRef));

    // Test mismatched entities
    final LightTable otherTable = new LightTable("OTHER");
    final MutableEntity otherEntity = new MutableEntity(otherTable);
    assertThrows(ExecutionRuntimeException.class, () -> rel.setLeftEntity(otherEntity));
    assertThrows(ExecutionRuntimeException.class, () -> rel.setRightEntity(otherEntity));
  }

  @Test
  public void testUnmodeledTables() {
    final MutableERModel model = new MutableERModel();

    final LightTable modeledTable = new LightTable("MODELED");
    final MutableEntity entity = new MutableEntity(modeledTable);
    entity.setEntityType(EntityType.strong_entity);
    model.addTable(modeledTable);
    model.addEntity(entity);

    final LightTable unmodeledTable = new LightTable("UNMODELED");
    model.addTable(unmodeledTable);

    final LightTable bridgeTable = new LightTable("BRIDGE");
    final MutableManyToManyRelationship rel = new MutableManyToManyRelationship(bridgeTable);
    model.addTable(bridgeTable);
    model.addRelationship(rel);

    assertThat(model.getUnmodeledTables(), containsInAnyOrder(unmodeledTable));
  }
}
