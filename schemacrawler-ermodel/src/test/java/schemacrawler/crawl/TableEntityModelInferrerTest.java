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

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import schemacrawler.ermodel.implementation.TableEntityModelInferrer;
import schemacrawler.ermodel.implementation.TableEntityModelInferrerFactory;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.ermodel.model.RelationshipCardinality;
import schemacrawler.schemacrawler.SchemaReference;
import us.fatehi.utility.OptionalBoolean;

public class TableEntityModelInferrerTest {

  private record ParentFixture(MutableTable table, MutableColumn idColumn) {}

  private record ChildFixture(
      MutableTable table, MutableColumn fkColumn, MutableForeignKey foreignKey) {}

  private static final SchemaReference SCHEMA = new SchemaReference("catalog", "schema");
  private static final AtomicInteger NAME_COUNTER = new AtomicInteger();

  private static void addIndex(
      final MutableTable table,
      final String indexBaseName,
      final boolean unique,
      final MutableColumn... indexColumns) {
    final MutableIndex index = new MutableIndex(table, uniqueName(indexBaseName));
    for (final MutableColumn column : indexColumns) {
      index.addColumn(new MutableIndexColumn(index, column));
    }
    index.setUnique(unique);
    table.addIndex(index);
  }

  private static void addPrimaryKey(
      final MutableTable table, final String pkBaseName, final MutableColumn... pkColumns) {
    final MutablePrimaryKey pk = MutablePrimaryKey.newPrimaryKey(table, uniqueName(pkBaseName));
    for (final MutableColumn column : pkColumns) {
      pk.addColumn(new MutableTableConstraintColumn(pk, column));
    }
    table.setPrimaryKey(pk);
  }

  private static ParentFixture parentWithPk(final String baseName) {
    final MutableTable parent = new MutableTable(SCHEMA, uniqueName(baseName));
    final MutableColumn parentId = new MutableColumn(parent, "ID");
    parent.addColumn(parentId);
    addPrimaryKey(parent, "PK_" + baseName, parentId);
    return new ParentFixture(parent, parentId);
  }

  private static ChildFixture tableWithSingleFk(
      final String tableBaseName,
      final String fkBaseName,
      final String fkColumnName,
      final boolean nullable,
      final MutableColumn parentColumn) {
    final MutableTable table = new MutableTable(SCHEMA, uniqueName(tableBaseName));
    final MutableColumn fkColumn = new MutableColumn(table, fkColumnName);
    fkColumn.setNullable(nullable);
    table.addColumn(fkColumn);
    final MutableForeignKey fk =
        new MutableForeignKey(
            uniqueName(fkBaseName), new ImmutableColumnReference(1, fkColumn, parentColumn));
    table.addForeignKey(fk);
    return new ChildFixture(table, fkColumn, fk);
  }

  private static String uniqueName(final String baseName) {
    return baseName + "_" + NAME_COUNTER.incrementAndGet();
  }

  @Test
  public void testForeignKeyCoveredByIndex() {
    final ParentFixture parent = parentWithPk("PARENT_COVERED_INDEX");

    final ChildFixture withoutIndex =
        tableWithSingleFk("TABLE_NO_INDEX", "FK_NO_INDEX", "COL1", false, parent.idColumn());
    assertThat(
        TableEntityModelInferrerFactory.forTable(withoutIndex.table())
            .coveredByIndex(withoutIndex.foreignKey()),
        is(OptionalBoolean.false_value));

    final ChildFixture withIndex =
        tableWithSingleFk("TABLE_WITH_INDEX", "FK_WITH_INDEX", "COL1", false, parent.idColumn());
    addIndex(withIndex.table(), "IDX", false, withIndex.fkColumn());
    assertThat(
        TableEntityModelInferrerFactory.forTable(withIndex.table())
            .coveredByIndex(withIndex.foreignKey()),
        is(OptionalBoolean.true_value));
  }

  @Test
  public void testForeignKeyCoveredByUniqueIndex() {
    final ParentFixture parent = parentWithPk("PARENT_COVERED_UNIQUE_INDEX");

    final ChildFixture withoutIndex =
        tableWithSingleFk("TABLE_NO_UNIQUE", "FK_NO_UNIQUE", "COL1", false, parent.idColumn());
    assertThat(
        TableEntityModelInferrerFactory.forTable(withoutIndex.table())
            .coveredByUniqueIndex(withoutIndex.foreignKey()),
        is(OptionalBoolean.false_value));

    final ChildFixture withNonUniqueIndex =
        tableWithSingleFk("TABLE_NON_UNIQUE", "FK_NON_UNIQUE", "COL1", false, parent.idColumn());
    addIndex(withNonUniqueIndex.table(), "IDX_NON_UNIQUE", false, withNonUniqueIndex.fkColumn());
    assertThat(
        TableEntityModelInferrerFactory.forTable(withNonUniqueIndex.table())
            .coveredByUniqueIndex(withNonUniqueIndex.foreignKey()),
        is(OptionalBoolean.false_value));

    final ChildFixture withUniqueIndex =
        tableWithSingleFk("TABLE_UNIQUE", "FK_UNIQUE", "COL1", false, parent.idColumn());
    addIndex(withUniqueIndex.table(), "IDX_UNIQUE", true, withUniqueIndex.fkColumn());
    assertThat(
        TableEntityModelInferrerFactory.forTable(withUniqueIndex.table())
            .coveredByUniqueIndex(withUniqueIndex.foreignKey()),
        is(OptionalBoolean.true_value));

    final MutableTable tableWithPk = new MutableTable(SCHEMA, uniqueName("TABLE_PK_UNIQUE"));
    final MutableColumn pkCol = new MutableColumn(tableWithPk, "PK_COL");
    tableWithPk.addColumn(pkCol);
    addPrimaryKey(tableWithPk, "PK_TABLE_PK_UNIQUE", pkCol);
    final MutableForeignKey fkPk =
        new MutableForeignKey(
            uniqueName("FK_PK"), new ImmutableColumnReference(1, pkCol, parent.idColumn()));
    tableWithPk.addForeignKey(fkPk);

    assertThat(
        TableEntityModelInferrerFactory.forTable(tableWithPk).coveredByUniqueIndex(fkPk),
        is(OptionalBoolean.true_value));
  }

  @Test
  public void testIdentifyForeignKeyCardinality() {
    final ParentFixture parent = parentWithPk("PARENT_CARDINALITY");

    final ChildFixture oneMany =
        tableWithSingleFk("TABLE_ONE_MANY", "FK_ONE_MANY", "COL1", false, parent.idColumn());
    assertThat(
        TableEntityModelInferrerFactory.forTable(oneMany.table())
            .inferCardinality(oneMany.foreignKey()),
        is(RelationshipCardinality.one_many));

    final ChildFixture zeroMany =
        tableWithSingleFk("TABLE_ZERO_MANY", "FK_ZERO_MANY", "COL2", true, parent.idColumn());
    assertThat(
        TableEntityModelInferrerFactory.forTable(zeroMany.table())
            .inferCardinality(zeroMany.foreignKey()),
        is(RelationshipCardinality.zero_many));

    final ChildFixture oneOne =
        tableWithSingleFk("TABLE_ONE_ONE", "FK_ONE_ONE", "COL3", false, parent.idColumn());
    addIndex(oneOne.table(), "UIDX_ONE_ONE", true, oneOne.fkColumn());
    assertThat(
        TableEntityModelInferrerFactory.forTable(oneOne.table())
            .inferCardinality(oneOne.foreignKey()),
        is(RelationshipCardinality.one_one));

    final ChildFixture zeroOne =
        tableWithSingleFk("TABLE_ZERO_ONE", "FK_ZERO_ONE", "COL4", true, parent.idColumn());
    addIndex(zeroOne.table(), "UIDX_ZERO_ONE", true, zeroOne.fkColumn());
    assertThat(
        TableEntityModelInferrerFactory.forTable(zeroOne.table())
            .inferCardinality(zeroOne.foreignKey()),
        is(RelationshipCardinality.zero_one));

    assertThat(
        TableEntityModelInferrerFactory.forTable(oneMany.table()).inferCardinality(null),
        is(RelationshipCardinality.unknown));
  }

  @Test
  public void testInferBridgeTable() {
    final ParentFixture tableA = parentWithPk("TABLE_A");
    final ParentFixture tableB = parentWithPk("TABLE_B");

    final MutableTable noUnique = new MutableTable(SCHEMA, uniqueName("BRIDGE_NO_UNIQUE"));
    final MutableColumn noUniqueColA = new MutableColumn(noUnique, "A_ID");
    final MutableColumn noUniqueColB = new MutableColumn(noUnique, "B_ID");
    final MutableColumn noUniquePk = new MutableColumn(noUnique, "ID");
    noUnique.addColumn(noUniqueColA);
    noUnique.addColumn(noUniqueColB);
    noUnique.addColumn(noUniquePk);
    addPrimaryKey(noUnique, "PK_BRIDGE_NO_UNIQUE", noUniquePk);
    noUnique.addForeignKey(
        new MutableForeignKey(
            uniqueName("FK_A"), new ImmutableColumnReference(1, noUniqueColA, tableA.idColumn())));
    noUnique.addForeignKey(
        new MutableForeignKey(
            uniqueName("FK_B"), new ImmutableColumnReference(1, noUniqueColB, tableB.idColumn())));

    TableEntityModelInferrer model = TableEntityModelInferrerFactory.forTable(noUnique);
    assertThat(model.inferBridgeTable(), is(false));
    assertThat(model.inferEntityType(), is(EntityType.unknown));

    final MutableTable pkExact = new MutableTable(SCHEMA, uniqueName("BRIDGE_PK_EXACT"));
    final MutableColumn pkExactColA = new MutableColumn(pkExact, "A_ID");
    final MutableColumn pkExactColB = new MutableColumn(pkExact, "B_ID");
    pkExact.addColumn(pkExactColA);
    pkExact.addColumn(pkExactColB);
    addPrimaryKey(pkExact, "PK_BRIDGE_EXACT", pkExactColA, pkExactColB);
    pkExact.addForeignKey(
        new MutableForeignKey(
            uniqueName("FK_A_EXACT"),
            new ImmutableColumnReference(1, pkExactColA, tableA.idColumn())));
    pkExact.addForeignKey(
        new MutableForeignKey(
            uniqueName("FK_B_EXACT"),
            new ImmutableColumnReference(1, pkExactColB, tableB.idColumn())));
    model = TableEntityModelInferrerFactory.forTable(pkExact);
    assertThat(model.inferBridgeTable(), is(true));

    final MutableTable pkExtra = new MutableTable(SCHEMA, uniqueName("BRIDGE_PK_EXTRA"));
    final MutableColumn pkExtraColA = new MutableColumn(pkExtra, "A_ID");
    final MutableColumn pkExtraColB = new MutableColumn(pkExtra, "B_ID");
    final MutableColumn pkExtraColX = new MutableColumn(pkExtra, "EXTRA");
    pkExtra.addColumn(pkExtraColA);
    pkExtra.addColumn(pkExtraColB);
    pkExtra.addColumn(pkExtraColX);
    addPrimaryKey(pkExtra, "PK_BRIDGE_EXTRA", pkExtraColA, pkExtraColB, pkExtraColX);
    pkExtra.addForeignKey(
        new MutableForeignKey(
            uniqueName("FK_A_EXTRA"),
            new ImmutableColumnReference(1, pkExtraColA, tableA.idColumn())));
    pkExtra.addForeignKey(
        new MutableForeignKey(
            uniqueName("FK_B_EXTRA"),
            new ImmutableColumnReference(1, pkExtraColB, tableB.idColumn())));
    model = TableEntityModelInferrerFactory.forTable(pkExtra);
    assertThat(model.inferBridgeTable(), is(false));

    final MutableTable uniqueIdxBridge = new MutableTable(SCHEMA, uniqueName("BRIDGE_UNIQUE_IDX"));
    final MutableColumn uniqueIdxColA = new MutableColumn(uniqueIdxBridge, "A_ID");
    final MutableColumn uniqueIdxColB = new MutableColumn(uniqueIdxBridge, "B_ID");
    final MutableColumn uniqueIdxPk = new MutableColumn(uniqueIdxBridge, "ID");
    uniqueIdxBridge.addColumn(uniqueIdxColA);
    uniqueIdxBridge.addColumn(uniqueIdxColB);
    uniqueIdxBridge.addColumn(uniqueIdxPk);
    addPrimaryKey(uniqueIdxBridge, "PK_BRIDGE_UNIQUE_IDX", uniqueIdxPk);
    uniqueIdxBridge.addForeignKey(
        new MutableForeignKey(
            uniqueName("FK_A_UIDX"),
            new ImmutableColumnReference(1, uniqueIdxColA, tableA.idColumn())));
    uniqueIdxBridge.addForeignKey(
        new MutableForeignKey(
            uniqueName("FK_B_UIDX"),
            new ImmutableColumnReference(1, uniqueIdxColB, tableB.idColumn())));
    addIndex(uniqueIdxBridge, "UIDX_BRIDGE", true, uniqueIdxColA, uniqueIdxColB);

    model = TableEntityModelInferrerFactory.forTable(uniqueIdxBridge);
    assertThat(model.inferBridgeTable(), is(true));
  }

  @Test
  public void testInferEntityTypeNonEntity() {
    final MutableTable table = new MutableTable(SCHEMA, uniqueName("NO_PK"));
    assertThat(
        TableEntityModelInferrerFactory.forTable(table).inferEntityType(),
        is(EntityType.non_entity));
  }

  @Test
  public void testInferEntityTypeStrongEntity() {
    final MutableTable table = new MutableTable(SCHEMA, uniqueName("STRONG"));
    final MutableColumn id = new MutableColumn(table, "ID");
    table.addColumn(id);
    addPrimaryKey(table, "PK_STRONG", id);

    assertThat(
        TableEntityModelInferrerFactory.forTable(table).inferEntityType(),
        is(EntityType.strong_entity));
  }

  @Test
  public void testInferEntityTypeSubtype() {
    final ParentFixture parent = parentWithPk("PARENT_SUBTYPE");

    final MutableTable child = new MutableTable(SCHEMA, uniqueName("CHILD_SUBTYPE"));
    final MutableColumn childId = new MutableColumn(child, "ID");
    child.addColumn(childId);
    addPrimaryKey(child, "PK_CHILD_SUBTYPE", childId);

    final MutableForeignKey fk =
        new MutableForeignKey(
            uniqueName("FK_CHILD_PARENT"),
            new ImmutableColumnReference(1, childId, parent.idColumn()));
    child.addForeignKey(fk);

    final TableEntityModelInferrer model = TableEntityModelInferrerFactory.forTable(child);
    assertThat(model.inferEntityType(), is(EntityType.subtype));
    assertThat(model.inferSuperType().orElse(null), is(parent.table()));
  }

  @Test
  public void testInferEntityTypeWeakEntity() {
    final ParentFixture parent = parentWithPk("PARENT_WEAK");

    final MutableTable weak = new MutableTable(SCHEMA, uniqueName("WEAK"));
    final MutableColumn weakParentId = new MutableColumn(weak, "PARENT_ID");
    weak.addColumn(weakParentId);
    final MutableColumn discriminator = new MutableColumn(weak, "SEQ");
    weak.addColumn(discriminator);
    addPrimaryKey(weak, "PK_WEAK", weakParentId, discriminator);

    final MutableForeignKey fk =
        new MutableForeignKey(
            uniqueName("FK_WEAK_PARENT"),
            new ImmutableColumnReference(1, weakParentId, parent.idColumn()));
    weak.addForeignKey(fk);

    assertThat(
        TableEntityModelInferrerFactory.forTable(weak).inferEntityType(),
        is(EntityType.weak_entity));
  }
}
