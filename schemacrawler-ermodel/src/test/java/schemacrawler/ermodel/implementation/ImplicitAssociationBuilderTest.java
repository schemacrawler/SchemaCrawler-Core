/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.implementation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static schemacrawler.ermodel.model.EntityType.strong_entity;
import static schemacrawler.ermodel.model.EntityType.weak_entity;

import org.junit.jupiter.api.Test;
import schemacrawler.ermodel.associations.ImplicitColumnReference;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.schema.Column;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.test.utility.crawl.LightTable;

public class ImplicitAssociationBuilderTest {

  @Test
  public void builderRejectsNullERModel() {
    assertThrows(NullPointerException.class, () -> ImplicitAssociationBuilder.builder(null));
  }

  @Test
  public void builderRejectsNonMutableERModel() {
    final ERModel nonMutableERModel = mock(ERModel.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> ImplicitAssociationBuilder.builder(nonMutableERModel));
  }

  @Test
  public void builderReturnsNonNullForMutableERModel() {
    final MutableERModel mutableERModel = new MutableERModel();
    final ImplicitAssociationBuilder builder = ImplicitAssociationBuilder.builder(mutableERModel);
    assertThat(builder, is(notNullValue()));
  }

  @Test
  public void addNullImplicitAssociationIsNoOp() {
    final MutableERModel mutableERModel = new MutableERModel();
    final ImplicitAssociationBuilder builder = ImplicitAssociationBuilder.builder(mutableERModel);

    // Should not throw
    builder.addImplicitAssociation(null);

    assertThat(mutableERModel.getImplicitRelationships(), is(empty()));
  }

  @Test
  public void addImplicitAssociationWithMissingEntity() {
    // Tables with no entities in the model
    final LightTable pkTable = new LightTable("PK_TABLE");
    pkTable.addColumn("ID");
    final LightTable fkTable = new LightTable("FK_TABLE");
    fkTable.addColumn("PK_TABLE_ID");

    final MutableERModel mutableERModel = new MutableERModel();
    // No entities added

    final ImplicitColumnReference columnReference = mockColumnReference(fkTable, pkTable);
    final ImplicitAssociationBuilder builder = ImplicitAssociationBuilder.builder(mutableERModel);
    builder.addImplicitAssociation(columnReference);

    // Should be skipped because entities don't exist
    assertThat(mutableERModel.getImplicitRelationships(), is(empty()));
  }

  @Test
  public void addImplicitAssociationWithBothEntitiesPresent() {
    final LightTable pkTable = new LightTable("PK_TABLE");
    pkTable.addColumn("ID");
    final LightTable fkTable = new LightTable("FK_TABLE");
    fkTable.addColumn("PK_TABLE_ID");

    final MutableERModel mutableERModel = new MutableERModel();
    final MutableEntity pkEntity = new MutableEntity(pkTable, strong_entity);
    final MutableEntity fkEntity = new MutableEntity(fkTable, weak_entity);
    mutableERModel.addEntity(pkEntity);
    mutableERModel.addEntity(fkEntity);

    final ImplicitColumnReference columnReference = mockColumnReference(fkTable, pkTable);
    final ImplicitAssociationBuilder builder = ImplicitAssociationBuilder.builder(mutableERModel);
    builder.addImplicitAssociation(columnReference);

    assertThat(mutableERModel.getImplicitRelationships(), hasSize(1));
  }

  @Test
  public void addImplicitAssociationVerifiesEntities() {
    final LightTable pkTable = new LightTable("PK_TABLE");
    pkTable.addColumn("ID");
    final LightTable fkTable = new LightTable("FK_TABLE");
    fkTable.addColumn("PK_TABLE_ID");

    final MutableERModel mutableERModel = new MutableERModel();
    final MutableEntity pkEntity = new MutableEntity(pkTable, strong_entity);
    final MutableEntity fkEntity = new MutableEntity(fkTable, weak_entity);
    mutableERModel.addEntity(pkEntity);
    mutableERModel.addEntity(fkEntity);

    final ImplicitColumnReference columnReference = mockColumnReference(fkTable, pkTable);
    final ImplicitAssociationBuilder builder = ImplicitAssociationBuilder.builder(mutableERModel);
    builder.addImplicitAssociation(columnReference);

    final Entity relationship =
        mutableERModel.getImplicitRelationships().iterator().next().getLeftEntity();
    assertThat(relationship, is(fkEntity));
  }

  /** Creates a mock ImplicitColumnReference linking fkTable.fkCol -> pkTable.pkCol. */
  private ImplicitColumnReference mockColumnReference(
      final LightTable fkTable, final LightTable pkTable) {
    final Column fkColumn = mock(Column.class);
    when(fkColumn.getParent()).thenReturn(fkTable);
    when(fkColumn.getName()).thenReturn(pkTable.getName() + "_ID");
    when(fkColumn.getFullName()).thenReturn(fkTable.getName() + "." + pkTable.getName() + "_ID");
    when(fkColumn.isNullable()).thenReturn(true);
    when(fkColumn.isPartOfPrimaryKey()).thenReturn(false);
    final NamedObjectKey fkColKey =
        new NamedObjectKey(null, null, fkTable.getName()).with(pkTable.getName() + "_ID");
    when(fkColumn.key()).thenReturn(fkColKey);

    final Column pkColumn = mock(Column.class);
    when(pkColumn.getParent()).thenReturn(pkTable);
    when(pkColumn.getName()).thenReturn("ID");
    when(pkColumn.getFullName()).thenReturn(pkTable.getName() + ".ID");
    when(pkColumn.isNullable()).thenReturn(false);
    when(pkColumn.isPartOfPrimaryKey()).thenReturn(true);
    final NamedObjectKey pkColKey = new NamedObjectKey(null, null, pkTable.getName()).with("ID");
    when(pkColumn.key()).thenReturn(pkColKey);

    final ImplicitColumnReference columnReference = mock(ImplicitColumnReference.class);
    when(columnReference.getForeignKeyColumn()).thenReturn(fkColumn);
    when(columnReference.getPrimaryKeyColumn()).thenReturn(pkColumn);

    return columnReference;
  }

}
