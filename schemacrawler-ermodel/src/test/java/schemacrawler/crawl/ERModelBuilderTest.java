/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import schemacrawler.ermodel.implementation.ERModelBuilder;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.EntitySubtype;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.SchemaReference;

public class ERModelBuilderTest {

  /**
   * Regression test for ConcurrentModificationException when building an ER model with a subtype
   * entity. The subtype entity's lookup triggers a recursive call to lookupOrCreateEntity, which
   * previously caused a ConcurrentModificationException due to nested HashMap.computeIfAbsent
   * calls on the same map.
   *
   * <pre>
   *   PARENT_TABLE
   *   ------------
   *   ID (PK)
   *
   *   SUBTYPE_TABLE
   *   -------------
   *   ID (PK, FK referencing PARENT_TABLE.ID)
   * </pre>
   */
  @Test
  public void testBuildWithSubtypeEntityDoesNotThrowConcurrentModificationException() {
    final SchemaReference schema = new SchemaReference("catalog", "schema");

    // Set up the supertype (parent) table with a PK
    final MutableTable parentTable = new MutableTable(schema, "PARENT_TABLE");
    final MutableColumn parentId = new MutableColumn(parentTable, "ID");
    parentId.markAsPartOfPrimaryKey();
    parentTable.addColumn(parentId);
    final MutablePrimaryKey parentPk = MutablePrimaryKey.newPrimaryKey(parentTable, "PK_PARENT");
    parentPk.addColumn(new MutableTableConstraintColumn(parentPk, parentId));
    parentTable.setPrimaryKey(parentPk);

    // Set up the subtype table: PK is also a FK to parent's PK
    final MutableTable subtypeTable = new MutableTable(schema, "SUBTYPE_TABLE");
    final MutableColumn subtypeId = new MutableColumn(subtypeTable, "ID");
    subtypeId.markAsPartOfPrimaryKey();
    subtypeTable.addColumn(subtypeId);
    final MutablePrimaryKey subtypePk = MutablePrimaryKey.newPrimaryKey(subtypeTable, "PK_SUBTYPE");
    subtypePk.addColumn(new MutableTableConstraintColumn(subtypePk, subtypeId));
    subtypeTable.setPrimaryKey(subtypePk);

    final ImmutableColumnReference columnReference =
        new ImmutableColumnReference(1, subtypeId, parentId);
    final MutableForeignKey fk = new MutableForeignKey("FK_SUBTYPE", columnReference);
    subtypeTable.addForeignKey(fk);
    subtypeId.setReferencedColumn(parentId);

    // Create a catalog with both tables; subtype is listed first to trigger
    // the recursive lookupOrCreateEntity call before the parent entity is cached
    final Catalog catalog = mock(Catalog.class);
    when(catalog.getTables()).thenReturn(List.of(subtypeTable, parentTable));

    // Should not throw ConcurrentModificationException
    final ERModel erModel = new ERModelBuilder(catalog).build();

    assertThat(erModel, is(notNullValue()));
    final List<Entity> entities = List.copyOf(erModel.getEntities());
    assertThat(entities, hasSize(2));

    // Verify the subtype entity is correctly identified
    final Entity subtypeEntity = erModel.lookupEntity(subtypeTable).orElseThrow();
    assertThat(subtypeEntity.getType(), is(EntityType.subtype));
    assertThat(subtypeEntity, is(instanceOf(EntitySubtype.class)));
    final EntitySubtype entitySubtype = (EntitySubtype) subtypeEntity;
    assertThat(entitySubtype.getSupertype(), is(notNullValue()));
    assertThat(entitySubtype.getSupertype().getName(), is("PARENT_TABLE"));

    // Verify the parent entity is correctly identified
    final Entity parentEntity = erModel.lookupEntity(parentTable).orElseThrow();
    assertThat(parentEntity.getType(), is(EntityType.strong_entity));
  }
}
