/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import schemacrawler.ermodel.implementation.TableEntityModelInferrer;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.ermodel.model.RelationshipCardinality;
import schemacrawler.schemacrawler.SchemaReference;
import us.fatehi.utility.OptionalBoolean;

public class TableEntityModelInferrerTest {

  @Test
  public void testForeignKeyCoveredByIndex() {
    final SchemaReference schema = new SchemaReference("catalog", "schema");
    final MutableTable table = new MutableTable(schema, "TABLE");
    final MutableColumn col1 = new MutableColumn(table, "COL1");
    table.addColumn(col1);

    final MutableTable parentTable = new MutableTable(schema, "PARENT");
    final MutableColumn parentCol1 = new MutableColumn(parentTable, "COL1");
    parentTable.addColumn(parentCol1);

    final ImmutableColumnReference columnReference =
        new ImmutableColumnReference(1, col1, parentCol1);
    final MutableForeignKey fk = new MutableForeignKey("FK", columnReference);
    table.addForeignKey(fk);

    final TableEntityModelInferrer model = new TableEntityModelInferrer(table);

    // No index yet
    assertThat(model.coveredByIndex(fk), is(OptionalBoolean.false_value));

    // Add index
    final MutableIndex index = new MutableIndex(table, "IDX");
    index.addColumn(new MutableIndexColumn(index, col1));
    table.addIndex(index);

    final TableEntityModelInferrer modelWithIndex = new TableEntityModelInferrer(table);
    assertThat(modelWithIndex.coveredByIndex(fk), is(OptionalBoolean.true_value));
  }

  @Test
  public void testForeignKeyCoveredByUniqueIndex() {
    final SchemaReference schema = new SchemaReference("catalog", "schema");
    final MutableTable table = new MutableTable(schema, "TABLE");
    final MutableColumn col1 = new MutableColumn(table, "COL1");
    table.addColumn(col1);

    final MutableTable parentTable = new MutableTable(schema, "PARENT");
    final MutableColumn parentCol1 = new MutableColumn(parentTable, "COL1");
    parentTable.addColumn(parentCol1);

    final ImmutableColumnReference columnReference =
        new ImmutableColumnReference(1, col1, parentCol1);
    final MutableForeignKey fk = new MutableForeignKey("FK", columnReference);
    table.addForeignKey(fk);

    final TableEntityModelInferrer model = new TableEntityModelInferrer(table);

    // No index yet
    assertThat(model.coveredByUniqueIndex(fk), is(OptionalBoolean.false_value));

    // Add non-unique index
    final MutableIndex index = new MutableIndex(table, "IDX");
    index.addColumn(new MutableIndexColumn(index, col1));
    index.setUnique(false);
    table.addIndex(index);

    final TableEntityModelInferrer modelWithIndex = new TableEntityModelInferrer(table);
    assertThat(modelWithIndex.coveredByUniqueIndex(fk), is(OptionalBoolean.false_value));

    // Add unique index
    index.setUnique(true);
    final TableEntityModelInferrer modelWithUniqueIndex = new TableEntityModelInferrer(table);
    assertThat(modelWithUniqueIndex.coveredByUniqueIndex(fk), is(OptionalBoolean.true_value));

    // Test PK as unique index
    final MutableTable tableWithPk = new MutableTable(schema, "TABLE_PK");
    final MutableColumn pkCol = new MutableColumn(tableWithPk, "PK_COL");
    tableWithPk.addColumn(pkCol);
    final MutablePrimaryKey pk = MutablePrimaryKey.newPrimaryKey(tableWithPk, "PK");
    pk.addColumn(new MutableTableConstraintColumn(pk, pkCol));
    tableWithPk.setPrimaryKey(pk);

    final ImmutableColumnReference fkPkRef = new ImmutableColumnReference(1, pkCol, parentCol1);
    final MutableForeignKey fkPk = new MutableForeignKey("FK_PK", fkPkRef);
    tableWithPk.addForeignKey(fkPk);

    final TableEntityModelInferrer modelWithPk = new TableEntityModelInferrer(tableWithPk);
    assertThat(modelWithPk.coveredByUniqueIndex(fkPk), is(OptionalBoolean.true_value));
  }

  @Test
  public void testIdentifyForeignKeyCardinality() {
    final SchemaReference schema = new SchemaReference("catalog", "schema");
    final MutableTable table = new MutableTable(schema, "TABLE");
    final MutableColumn col1 = new MutableColumn(table, "COL1");
    col1.setNullable(false); // Required
    table.addColumn(col1);

    final MutableTable parentTable = new MutableTable(schema, "PARENT");
    final MutableColumn parentCol1 = new MutableColumn(parentTable, "COL1");
    parentTable.addColumn(parentCol1);

    final ImmutableColumnReference columnReference =
        new ImmutableColumnReference(1, col1, parentCol1);
    final MutableForeignKey fk = new MutableForeignKey("FK", columnReference);
    table.addForeignKey(fk);

    final TableEntityModelInferrer model = new TableEntityModelInferrer(table);

    // 1. One-Many (Not unique, Not optional)
    assertThat(model.inferCardinality(fk), is(RelationshipCardinality.one_many));

    // 2. Zero-Many (Not unique, Optional)
    col1.setNullable(true);
    // TableEntityModel caches optionality from fk.isOptional() which relies on
    // col.isNullable()
    // but TableEntityModel also caches importedColumnsMap.
    // We need a new model or a new FK if we want to test changes after model
    // construction,
    // although TableEntityModel.identifyRelationshipCardinality(fk) calls
    // findOrGetImportedKeys(fk).
    final MutableTable table2 = new MutableTable(schema, "TABLE2");
    final MutableColumn col2 = new MutableColumn(table2, "COL2");
    col2.setNullable(true);
    table2.addColumn(col2);
    final MutableForeignKey fk2 =
        new MutableForeignKey("FK2", new ImmutableColumnReference(1, col2, parentCol1));
    table2.addForeignKey(fk2);
    final TableEntityModelInferrer model2 = new TableEntityModelInferrer(table2);
    assertThat(model2.inferCardinality(fk2), is(RelationshipCardinality.zero_many));

    // 3. One-One (Unique, Not optional)
    final MutableTable table3 = new MutableTable(schema, "TABLE3");
    final MutableColumn col3 = new MutableColumn(table3, "COL3");
    col3.setNullable(false);
    table3.addColumn(col3);
    final MutableIndex uniqueIdx3 = new MutableIndex(table3, "UIDX3");
    uniqueIdx3.addColumn(new MutableIndexColumn(uniqueIdx3, col3));
    uniqueIdx3.setUnique(true);
    table3.addIndex(uniqueIdx3);
    final MutableForeignKey fk3 =
        new MutableForeignKey("FK3", new ImmutableColumnReference(1, col3, parentCol1));
    table3.addForeignKey(fk3);
    final TableEntityModelInferrer model3 = new TableEntityModelInferrer(table3);
    assertThat(model3.inferCardinality(fk3), is(RelationshipCardinality.one_one));

    // 4. Zero-One (Unique, Optional)
    final MutableTable table4 = new MutableTable(schema, "TABLE4");
    final MutableColumn col4 = new MutableColumn(table4, "COL4");
    col4.setNullable(true);
    table4.addColumn(col4);
    final MutableIndex uniqueIdx4 = new MutableIndex(table4, "UIDX4");
    uniqueIdx4.addColumn(new MutableIndexColumn(uniqueIdx4, col4));
    uniqueIdx4.setUnique(true);
    table4.addIndex(uniqueIdx4);
    final MutableForeignKey fk4 =
        new MutableForeignKey("FK4", new ImmutableColumnReference(1, col4, parentCol1));
    table4.addForeignKey(fk4);
    final TableEntityModelInferrer model4 = new TableEntityModelInferrer(table4);
    assertThat(model4.inferCardinality(fk4), is(RelationshipCardinality.zero_one));

    // 5. Null FK
    assertThat(model.inferCardinality(null), is(RelationshipCardinality.unknown));
  }

  @Test
  public void testInferBridgeTable() {
    final SchemaReference schema = new SchemaReference("catalog", "schema");

    final MutableTable tableA = new MutableTable(schema, "TABLE_A");
    final MutableColumn colA = new MutableColumn(tableA, "ID");
    tableA.addColumn(colA);
    final MutablePrimaryKey pkA = MutablePrimaryKey.newPrimaryKey(tableA, "PK_A");
    pkA.addColumn(new MutableTableConstraintColumn(pkA, colA));
    tableA.setPrimaryKey(pkA);

    final MutableTable tableB = new MutableTable(schema, "TABLE_B");
    final MutableColumn colB = new MutableColumn(tableB, "ID");
    tableB.addColumn(colB);
    final MutablePrimaryKey pkB = MutablePrimaryKey.newPrimaryKey(tableB, "PK_B");
    pkB.addColumn(new MutableTableConstraintColumn(pkB, colB));
    tableB.setPrimaryKey(pkB);

    final MutableTable bridgeTable = new MutableTable(schema, "BRIDGE");
    final MutableColumn bridgeColA = new MutableColumn(bridgeTable, "A_ID");
    bridgeTable.addColumn(bridgeColA);
    final MutableColumn bridgeColB = new MutableColumn(bridgeTable, "B_ID");
    bridgeTable.addColumn(bridgeColB);

    // Add a dummy PK so it's not a non_entity
    final MutableColumn bridgePkCol = new MutableColumn(bridgeTable, "ID");
    bridgeTable.addColumn(bridgePkCol);
    final MutablePrimaryKey dummyPk = MutablePrimaryKey.newPrimaryKey(bridgeTable, "PK_DUMMY");
    dummyPk.addColumn(new MutableTableConstraintColumn(dummyPk, bridgePkCol));
    bridgeTable.setPrimaryKey(dummyPk);

    final MutableForeignKey fkA =
        new MutableForeignKey("FK_A", new ImmutableColumnReference(1, bridgeColA, colA));
    bridgeTable.addForeignKey(fkA);
    final MutableForeignKey fkB =
        new MutableForeignKey("FK_B", new ImmutableColumnReference(1, bridgeColB, colB));
    bridgeTable.addForeignKey(fkB);

    // No PK/Unique index yet
    TableEntityModelInferrer model = new TableEntityModelInferrer(bridgeTable);
    assertThat(model.inferBridgeTable(), is(false));
    assertThat(model.inferEntityType(), is(EntityType.unknown));

    // Add PK on both columns
    final MutablePrimaryKey bridgePk = MutablePrimaryKey.newPrimaryKey(bridgeTable, "PK_BRIDGE");
    bridgePk.addColumn(new MutableTableConstraintColumn(bridgePk, bridgeColA));
    bridgePk.addColumn(new MutableTableConstraintColumn(bridgePk, bridgeColB));
    bridgeTable.setPrimaryKey(bridgePk);

    model = new TableEntityModelInferrer(bridgeTable);
    assertThat(model.inferBridgeTable(), is(true));

    // Test with PK containing an extra column - should NOT be a bridge table
    final MutableColumn extraCol = new MutableColumn(bridgeTable, "EXTRA");
    bridgeTable.addColumn(extraCol);
    bridgePk.addColumn(new MutableTableConstraintColumn(bridgePk, extraCol));
    model = new TableEntityModelInferrer(bridgeTable);
    assertThat(model.inferBridgeTable(), is(false));

    // Test with Unique Index instead of PK
    final MutableTable bridgeTable2 = new MutableTable(schema, "BRIDGE2");
    final MutableColumn b2ColA = new MutableColumn(bridgeTable2, "A_ID");
    bridgeTable2.addColumn(b2ColA);
    final MutableColumn b2ColB = new MutableColumn(bridgeTable2, "B_ID");
    bridgeTable2.addColumn(b2ColB);

    bridgeTable2.addForeignKey(
        new MutableForeignKey("FK_A2", new ImmutableColumnReference(1, b2ColA, colA)));
    bridgeTable2.addForeignKey(
        new MutableForeignKey("FK_B2", new ImmutableColumnReference(1, b2ColB, colB)));

    final MutableIndex uniqueIdx = new MutableIndex(bridgeTable2, "UIDX_BRIDGE");
    uniqueIdx.addColumn(new MutableIndexColumn(uniqueIdx, b2ColA));
    uniqueIdx.addColumn(new MutableIndexColumn(uniqueIdx, b2ColB));
    uniqueIdx.setUnique(true);
    bridgeTable2.addIndex(uniqueIdx);

    // Also need a PK for inferEntityType to not return non_entity
    final MutableColumn b2PkCol = new MutableColumn(bridgeTable2, "ID");
    bridgeTable2.addColumn(b2PkCol);
    final MutablePrimaryKey b2Pk = MutablePrimaryKey.newPrimaryKey(bridgeTable2, "PK_B2");
    b2Pk.addColumn(new MutableTableConstraintColumn(b2Pk, b2PkCol));
    bridgeTable2.setPrimaryKey(b2Pk);

    model = new TableEntityModelInferrer(bridgeTable2);
    assertThat(model.inferBridgeTable(), is(true));
  }
}
