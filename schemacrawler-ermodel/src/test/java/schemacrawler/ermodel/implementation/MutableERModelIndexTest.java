/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.implementation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static schemacrawler.ermodel.model.EntityType.non_entity;
import static schemacrawler.ermodel.model.EntityType.strong_entity;
import static schemacrawler.ermodel.model.EntityType.weak_entity;
import static schemacrawler.ermodel.model.RelationshipCardinality.many_many;
import static schemacrawler.ermodel.model.RelationshipCardinality.one_many;

import java.util.Set;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.Relationship;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.TableReference;
import schemacrawler.test.utility.crawl.LightForeignKey;
import schemacrawler.test.utility.crawl.LightTable;

public class MutableERModelIndexTest {

  @Test
  public void addsReplaceByCompleteKeyAndIgnoreNullInputs() {
    final MutableERModel model = new MutableERModel();
    final LightTable originalTable = new LightTable("TABLE");
    final LightTable replacementTable = new LightTable("TABLE");
    final MutableEntity originalEntity = new MutableEntity(originalTable, strong_entity);
    final MutableEntity replacementEntity = new MutableEntity(replacementTable, weak_entity);

    model.addTable(originalTable);
    model.addTable(null);
    model.addEntity(originalEntity);
    model.addEntity(null);
    model.addRelationship(null);
    model.addImplicitRelationship(null);
    model.addUnmodeledTableReference(null);
    model.addTable(replacementTable);
    model.addEntity(replacementEntity);

    assertThat(model.getTables().size(), is(1));
    assertThat(model.getTables().iterator().next(), sameInstance(replacementTable));
    assertThat(model.lookupEntity(replacementTable).orElseThrow(), sameInstance(replacementEntity));
  }

  @Test
  public void filtersBeforeSortingEntitiesAndRelationships() {
    final MutableERModel model = new MutableERModel();
    final MutableEntity firstEntity = new MutableEntity(new LightTable("A"), strong_entity);
    final MutableEntity secondEntity = new MutableEntity(new LightTable("B"), strong_entity);
    final Entity excludedEntity = mock(Entity.class);
    when(excludedEntity.key()).thenReturn(new NamedObjectKey("excluded-entity"));
    when(excludedEntity.getType()).thenReturn(non_entity);
    when(excludedEntity.compareTo(any(NamedObject.class)))
        .thenThrow(new AssertionError("Filtered entity must not be sorted"));
    model.addEntity(excludedEntity);
    model.addEntity(firstEntity);
    model.addEntity(secondEntity);

    final MutableManyToManyRelationship firstRelationship =
        new MutableManyToManyRelationship(new LightTable("BRIDGE_A"));
    final MutableManyToManyRelationship secondRelationship =
        new MutableManyToManyRelationship(new LightTable("BRIDGE_B"));
    final Relationship excludedRelationship = mock(Relationship.class);
    when(excludedRelationship.key()).thenReturn(new NamedObjectKey("excluded-relationship"));
    when(excludedRelationship.getType()).thenReturn(one_many);
    when(excludedRelationship.compareTo(any(NamedObject.class)))
        .thenThrow(new AssertionError("Filtered relationship must not be sorted"));
    model.addRelationship(excludedRelationship);
    model.addRelationship(firstRelationship);
    model.addRelationship(secondRelationship);

    assertThat(model.getEntities(), containsInAnyOrder(firstEntity, secondEntity));
    assertThat(
        model.getEntitiesByType(strong_entity), containsInAnyOrder(firstEntity, secondEntity));
    assertThat(
        model.getRelationshipsByType(many_many), contains(firstRelationship, secondRelationship));
  }

  @Test
  public void retainsBridgeUnmodeledAndEndpointRelationshipBehavior() {
    final MutableERModel model = new MutableERModel();
    final LightTable modeledTable = new LightTable("MODELED");
    final MutableEntity leftEntity = new MutableEntity(modeledTable, strong_entity);
    final LightTable unmodeledTable = new LightTable("UNMODELED");
    final LightTable bridgeTable = new LightTable("BRIDGE");
    final MutableManyToManyRelationship bridgeRelationship =
        new MutableManyToManyRelationship(bridgeTable);
    bridgeRelationship.setLeftEntity(leftEntity);
    bridgeRelationship.setRightEntity(new MutableEntity(new LightTable("OTHER"), weak_entity));
    model.addTable(modeledTable);
    model.addEntity(leftEntity);
    model.addTable(unmodeledTable);
    model.addTable(bridgeTable);
    model.addRelationship(bridgeRelationship);

    final LightTable foreignTable = new LightTable("FOREIGN");
    final LightTable primaryTable = new LightTable("PRIMARY");
    final MutableEntity foreignEntity = new MutableEntity(foreignTable, weak_entity);
    final MutableEntity primaryEntity = new MutableEntity(primaryTable, strong_entity);
    final TableReference foreignKey = new LightForeignKey("FK_PRIMARY", foreignTable, primaryTable);
    final MutableTableReferenceRelationship relationship =
        new MutableTableReferenceRelationship(foreignKey);
    relationship.setEntities(foreignEntity, primaryEntity);
    model.addTable(foreignTable);
    model.addTable(primaryTable);
    model.addEntity(foreignEntity);
    model.addEntity(primaryEntity);
    model.addRelationship(relationship);
    model.addImplicitRelationship(relationship);
    model.addUnmodeledTableReference(foreignKey);

    assertThat(
        model.lookupByBridgeTable(bridgeTable).orElseThrow(), sameInstance(bridgeRelationship));
    assertThat(model.lookupRelationship(foreignKey).orElseThrow(), sameInstance(relationship));
    assertThat(model.getUnmodeledTables(), contains(unmodeledTable));
    assertThat(model.getUnmodeledTableReferences(), is(Set.of(foreignKey)));
    assertThat(foreignEntity.getRelationships(), contains(relationship));
    assertThat(primaryEntity.getRelationships(), contains(relationship));
    assertThat(foreignEntity.getImplicitRelationships(), contains(relationship));
    assertThat(primaryEntity.getImplicitRelationships(), contains(relationship));
  }

  @Test
  public void serializedModelPreservesLookupsAndRemainsMutable() {
    final MutableERModel model = new MutableERModel();
    final LightTable table = new LightTable("TABLE");
    final MutableEntity entity = new MutableEntity(table, strong_entity);
    model.addTable(table);
    model.addEntity(entity);

    final MutableERModel clonedModel = SerializationUtils.clone(model);
    final LightTable addedTable = new LightTable("AFTER_READ");

    assertThat(clonedModel.lookupEntity(table).isPresent(), is(true));
    assertThat(clonedModel.getTables(), contains(table));
    clonedModel.addTable(addedTable);
    assertThat(clonedModel.getTables(), contains(addedTable, table));
  }

  @Test
  public void emptyModelKeepsReadOnlyCollections() {
    final MutableERModel model = new MutableERModel();

    assertThat(model.getEntities(), is(empty()));
    assertThat(model.getRelationships(), is(empty()));
    assertThat(model.getTables(), is(empty()));
    assertThat(model.getUnmodeledTables(), is(empty()));
  }
}
