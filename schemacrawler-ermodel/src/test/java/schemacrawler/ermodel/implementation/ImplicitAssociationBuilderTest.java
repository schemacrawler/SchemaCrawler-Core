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
import static schemacrawler.ermodel.model.EntityType.strong_entity;
import static schemacrawler.ermodel.model.EntityType.weak_entity;

import org.junit.jupiter.api.Test;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.schema.Catalog;
import schemacrawler.test.utility.crawl.LightCatalogUtility;
import schemacrawler.test.utility.crawl.LightColumn;
import schemacrawler.test.utility.crawl.LightTable;

public class ImplicitAssociationBuilderTest {

  @Test
  public void addImplicitAssociationVerifiesEntities() {
    final LightTable pkTable = new LightTable("PK_TABLE");
    final LightColumn pkColumn = pkTable.addColumn("ID");
    final LightTable fkTable = new LightTable("FK_TABLE");
    final LightColumn fkColumn = fkTable.addColumn("PK_TABLE_ID");

    Catalog catalog = LightCatalogUtility.lightCatalog();

    final MutableERModel mutableERModel = new MutableERModel();
    final MutableEntity pkEntity = new MutableEntity(pkTable, strong_entity);
    final MutableEntity fkEntity = new MutableEntity(fkTable, weak_entity);
    mutableERModel.addEntity(pkEntity);
    mutableERModel.addEntity(fkEntity);

    // Build implicit associations
    ImplicitRelationshipBuilder.builder(catalog, mutableERModel)
        .addColumnReference(fkColumn, pkColumn)
        .build();

    assertThat(mutableERModel.getImplicitRelationships(), hasSize(1));
    final Entity relationship =
        mutableERModel.getImplicitRelationships().iterator().next().getLeftEntity();
    assertThat(relationship, is(fkEntity));
  }

  @Test
  public void addImplicitAssociationWithMissingEntity() {
    // Tables with no entities in the model
    final LightTable pkTable = new LightTable("PK_TABLE");
    final LightColumn pkColumn = pkTable.addColumn("ID");
    final LightTable fkTable = new LightTable("FK_TABLE");
    final LightColumn fkColumn = fkTable.addColumn("PK_TABLE_ID");

    Catalog catalog = LightCatalogUtility.lightCatalog(pkTable, fkTable);

    final MutableERModel mutableERModel = new MutableERModel();
    // No entities added

    // Build implicit associations
    ImplicitRelationshipBuilder.builder(catalog, mutableERModel)
        .addColumnReference(fkColumn, pkColumn)
        .build();

    // Should be skipped because entities don't exist
    assertThat(mutableERModel.getImplicitRelationships(), is(empty()));
  }

  @Test
  public void builderRejectsNonMutableERModel() {
    Catalog catalog = LightCatalogUtility.lightCatalog();
    final ERModel nonMutableERModel = mock(ERModel.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> ImplicitRelationshipBuilder.builder(catalog, nonMutableERModel));
  }

  @Test
  public void builderRejectsNullERModel() {
    Catalog catalog = LightCatalogUtility.lightCatalog();
    assertThrows(
        NullPointerException.class, () -> ImplicitRelationshipBuilder.builder(catalog, null));
  }

  @Test
  public void builderReturnsNonNullForMutableERModel() {
    Catalog catalog = LightCatalogUtility.lightCatalog();
    final MutableERModel mutableERModel = new MutableERModel();
    final ImplicitRelationshipBuilder builder =
        ImplicitRelationshipBuilder.builder(catalog, mutableERModel);
    assertThat(builder, is(notNullValue()));
  }
}
