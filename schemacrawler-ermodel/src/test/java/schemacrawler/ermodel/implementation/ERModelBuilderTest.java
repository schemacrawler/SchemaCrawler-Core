package schemacrawler.ermodel.implementation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static schemacrawler.test.utility.crawl.LightCatalogUtility.lightCatalog;

import org.junit.jupiter.api.Test;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.EntitySubtype;
import schemacrawler.ermodel.model.Relationship;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.PrimaryKey;
import schemacrawler.test.utility.crawl.LightColumn;
import schemacrawler.test.utility.crawl.LightForeignKey;
import schemacrawler.test.utility.crawl.LightPrimaryKey;
import schemacrawler.test.utility.crawl.LightTable;

public class ERModelBuilderTest {

  @Test
  public void testConcurrentModificationException() {
    final LightTable superTable = new LightTable("SUPER_TABLE_1");
    final LightColumn superPkCol = superTable.addColumn("ID");
    final PrimaryKey superPk = new LightPrimaryKey(superPkCol);
    superTable.setPrimaryKey(superPk);

    final LightTable subTable = new LightTable("SUB_TABLE_1");
    final LightColumn subPkCol = subTable.addColumn("ID");
    final LightPrimaryKey subPk = new LightPrimaryKey(subPkCol);
    subTable.setPrimaryKey(subPk);

    final ForeignKey fk = new LightForeignKey("FK_SUB_TABLE_1", subPkCol, superPkCol);
    subTable.addImportedForeignKey(fk);

    final Catalog catalog = lightCatalog(subTable, superTable);

    final ERModelBuilder builder = ERModelBuilder.builder(catalog);
    final ERModel erModel = assertDoesNotThrow(() -> builder.build());
    assertNotNull(erModel);
  }

  @Test
  public void testSubtypeIdentifyingRelationship() {
    final LightTable superTable = new LightTable("SUPER_TABLE_2");
    final LightColumn superPkCol = superTable.addColumn("ID");
    final LightPrimaryKey superPk = new LightPrimaryKey(superPkCol);
    superTable.setPrimaryKey(superPk);

    final LightTable subTable = new LightTable("SUB_TABLE_2");
    final LightColumn subPkCol = subTable.addColumn("ID");
    final LightPrimaryKey subPk = new LightPrimaryKey(subPkCol);
    subTable.setPrimaryKey(subPk);

    final ForeignKey fk = new LightForeignKey("FK_SUB_TABLE_2", subPkCol, superPkCol);
    subTable.addImportedForeignKey(fk);

    final Catalog catalog = lightCatalog(subTable, superTable);

    final ERModel erModel = ERModelBuilder.builder(catalog).build();

    final Entity subtypeEntity = erModel.lookupEntity(subTable).orElseThrow();
    final EntitySubtype entitySubtype = assertInstanceOf(EntitySubtype.class, subtypeEntity);
    final Relationship relationship = entitySubtype.getIdentifyingRelationship();

    assertNotNull(relationship);
    assertTrue(erModel.lookupRelationship(fk).isPresent());
    assertEquals(erModel.lookupRelationship(fk).orElseThrow(), relationship);
  }
}
