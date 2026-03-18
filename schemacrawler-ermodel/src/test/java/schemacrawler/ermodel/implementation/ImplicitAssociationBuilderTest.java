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

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import schemacrawler.ermodel.associations.ImplicitAssociationsAnalyzer;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.TableReference;
import schemacrawler.test.utility.crawl.LightForeignKey;
import schemacrawler.test.utility.crawl.LightTable;

public class ImplicitAssociationBuilderTest {

  @Test
  public void addEmptyImplicitAssociationIsNoOp() {
    final MutableERModel mutableERModel = new MutableERModel();
    // No entities added

    final ImplicitAssociationsAnalyzer analyzer = mock(ImplicitAssociationsAnalyzer.class);
    when(analyzer.analyzeTables()).thenReturn(null);

    // Build implicit associations
    ImplicitAssociationBuilder.builder(mutableERModel)
        .withImplicitAssociationsAnalyzer(analyzer)
        .build();

    assertThat(mutableERModel.getImplicitRelationships(), is(empty()));
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

    final TableReference implicitAssociation =
        new LightForeignKey("with_entities", fkTable, pkTable);
    final ImplicitAssociationsAnalyzer analyzer = mock(ImplicitAssociationsAnalyzer.class);
    when(analyzer.analyzeTables()).thenReturn(List.of(implicitAssociation));

    // Build implicit associations
    ImplicitAssociationBuilder.builder(mutableERModel)
        .withImplicitAssociationsAnalyzer(analyzer)
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
    pkTable.addColumn("ID");
    final LightTable fkTable = new LightTable("FK_TABLE");
    fkTable.addColumn("PK_TABLE_ID");

    final MutableERModel mutableERModel = new MutableERModel();
    // No entities added

    final TableReference implicitAssociation =
        new LightForeignKey("without_entities", fkTable, pkTable);
    final ImplicitAssociationsAnalyzer analyzer = mock(ImplicitAssociationsAnalyzer.class);
    when(analyzer.analyzeTables()).thenReturn(List.of(implicitAssociation));

    // Build implicit associations
    ImplicitAssociationBuilder.builder(mutableERModel)
        .withImplicitAssociationsAnalyzer(analyzer)
        .build();

    // Should be skipped because entities don't exist
    assertThat(mutableERModel.getImplicitRelationships(), is(empty()));
  }

  @Test
  public void addNullImplicitAssociationIsNoOp() {
    final MutableERModel mutableERModel = new MutableERModel();
    // No entities added

    final ImplicitAssociationsAnalyzer analyzer = mock(ImplicitAssociationsAnalyzer.class);
    when(analyzer.analyzeTables()).thenReturn(Collections.singletonList(null));

    // Build implicit associations
    ImplicitAssociationBuilder.builder(mutableERModel)
        .withImplicitAssociationsAnalyzer(analyzer)
        .build();

    assertThat(mutableERModel.getImplicitRelationships(), is(empty()));
  }

  @Test
  public void builderRejectsNonMutableERModel() {
    final ERModel nonMutableERModel = mock(ERModel.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> ImplicitAssociationBuilder.builder(nonMutableERModel));
  }

  @Test
  public void builderRejectsNullERModel() {
    assertThrows(NullPointerException.class, () -> ImplicitAssociationBuilder.builder(null));
  }

  @Test
  public void builderReturnsNonNullForMutableERModel() {
    final MutableERModel mutableERModel = new MutableERModel();
    final ImplicitAssociationBuilder builder = ImplicitAssociationBuilder.builder(mutableERModel);
    assertThat(builder, is(notNullValue()));
  }

  /** Creates a mock ImplicitColumnReference linking fkTable.fkCol -> pkTable.pkCol. */
  private ColumnReference mockColumnReference(final LightTable fkTable, final LightTable pkTable) {
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

    final ColumnReference columnReference = mock(ColumnReference.class);
    when(columnReference.getForeignKeyColumn()).thenReturn(fkColumn);
    when(columnReference.getPrimaryKeyColumn()).thenReturn(pkColumn);

    return columnReference;
  }
}
