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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import schemacrawler.ermodel.implementation.TableEntityModelInferrer;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.schemacrawler.SchemaReference;

public class EntityIdentifierTest {

  /**
   * Test for a non-entity table.
   *
   * <pre>
   *   TABLE_NON_ENTITY
   *   ----------------
   *   COLUMN1
   *   COLUMN2
   * </pre>
   *
   * No primary key.
   */
  @Test
  public void testNonEntity() {
    final SchemaReference schema = new SchemaReference("catalog", "schema");
    final MutableTable table = new MutableTable(schema, "TABLE_NON_ENTITY");
    table.addColumn(new MutableColumn(table, "COLUMN1"));
    table.addColumn(new MutableColumn(table, "COLUMN2"));

    final EntityType entityType = new TableEntityModelInferrer(table).inferEntityType();
    assertThat(entityType, is(EntityType.non_entity));
  }

  /**
   * Test for a strong entity table.
   *
   * <pre>
   *   TABLE_STRONG
   *   ------------
   *   ID (PK)
   *   NAME
   * </pre>
   */
  @Test
  public void testStrongEntity() {
    final SchemaReference schema = new SchemaReference("catalog", "schema");
    final MutableTable table = new MutableTable(schema, "TABLE_STRONG");
    final MutableColumn id = new MutableColumn(table, "ID");
    table.addColumn(id);
    table.addColumn(new MutableColumn(table, "NAME"));

    final MutablePrimaryKey pk = MutablePrimaryKey.newPrimaryKey(table, "PK_STRONG");
    pk.addColumn(new MutableTableConstraintColumn(pk, id));
    table.setPrimaryKey(pk);

    final EntityType entityType = new TableEntityModelInferrer(table).inferEntityType();
    assertThat(entityType, is(EntityType.strong_entity));
  }

  /**
   * Test for a subtype entity table.
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
  public void testSubtype() {
    final SchemaReference schema = new SchemaReference("catalog", "schema");

    final MutableTable parentTable = new MutableTable(schema, "PARENT_TABLE");
    final MutableColumn parentId = new MutableColumn(parentTable, "ID");
    parentTable.addColumn(parentId);
    final MutablePrimaryKey parentPk = MutablePrimaryKey.newPrimaryKey(parentTable, "PK_PARENT");
    parentPk.addColumn(new MutableTableConstraintColumn(parentPk, parentId));
    parentTable.setPrimaryKey(parentPk);

    final MutableTable subtypeTable = new MutableTable(schema, "SUBTYPE_TABLE");
    final MutableColumn subtypeId = new MutableColumn(subtypeTable, "ID");
    subtypeTable.addColumn(subtypeId);
    final MutablePrimaryKey subtypePk = MutablePrimaryKey.newPrimaryKey(subtypeTable, "PK_SUBTYPE");
    subtypePk.addColumn(new MutableTableConstraintColumn(subtypePk, subtypeId));
    subtypeTable.setPrimaryKey(subtypePk);

    final ImmutableColumnReference columnReference =
        new ImmutableColumnReference(1, subtypeId, parentId);
    final MutableForeignKey fk = new MutableForeignKey("FK_SUBTYPE", columnReference);
    subtypeTable.addForeignKey(fk);
    subtypeId.setReferencedColumn(parentId);

    final EntityType entityType = new TableEntityModelInferrer(subtypeTable).inferEntityType();
    assertThat(entityType, is(EntityType.subtype));
  }

  /**
   * Test for an unknown entity table (high connectivity).
   *
   * <pre>
   *   TABLE1, TABLE2, TABLE3
   *   ----------------------
   *   ID (PK)
   *
   *   UNKNOWN_TABLE
   *   -------------
   *   ID (PK)
   *   FK1 referencing TABLE1.ID
   *   FK2 referencing TABLE2.ID
   *   FK3 referencing TABLE3.ID
   * </pre>
   */
  @Test
  public void testUnknown() {
    final SchemaReference schema = new SchemaReference("catalog", "schema");

    final MutableTable table1 = new MutableTable(schema, "TABLE1");
    final MutableColumn id1 = new MutableColumn(table1, "ID");
    table1.addColumn(id1);
    final MutablePrimaryKey pk1 = MutablePrimaryKey.newPrimaryKey(table1, "PK1");
    pk1.addColumn(new MutableTableConstraintColumn(pk1, id1));
    table1.setPrimaryKey(pk1);

    final MutableTable table2 = new MutableTable(schema, "TABLE2");
    final MutableColumn id2 = new MutableColumn(table2, "ID");
    table2.addColumn(id2);
    final MutablePrimaryKey pk2 = MutablePrimaryKey.newPrimaryKey(table2, "PK2");
    pk2.addColumn(new MutableTableConstraintColumn(pk2, id2));
    table2.setPrimaryKey(pk2);

    final MutableTable table3 = new MutableTable(schema, "TABLE3");
    final MutableColumn id3 = new MutableColumn(table3, "ID");
    table3.addColumn(id3);
    final MutablePrimaryKey pk3 = MutablePrimaryKey.newPrimaryKey(table3, "PK3");
    pk3.addColumn(new MutableTableConstraintColumn(pk3, id3));
    table3.setPrimaryKey(pk3);

    final MutableTable unknownTable = new MutableTable(schema, "UNKNOWN_TABLE");
    final MutableColumn unknownId = new MutableColumn(unknownTable, "ID");
    final MutableColumn fk1Col = new MutableColumn(unknownTable, "FK1");
    final MutableColumn fk2Col = new MutableColumn(unknownTable, "FK2");
    final MutableColumn fk3Col = new MutableColumn(unknownTable, "FK3");
    unknownTable.addColumn(unknownId);
    unknownTable.addColumn(fk1Col);
    unknownTable.addColumn(fk2Col);
    unknownTable.addColumn(fk3Col);
    final MutablePrimaryKey pkUnknown = MutablePrimaryKey.newPrimaryKey(unknownTable, "PK_UNKNOWN");
    pkUnknown.addColumn(new MutableTableConstraintColumn(pkUnknown, unknownId));
    unknownTable.setPrimaryKey(pkUnknown);

    unknownTable.addForeignKey(
        new MutableForeignKey("FK1", new ImmutableColumnReference(1, fk1Col, id1)));
    unknownTable.addForeignKey(
        new MutableForeignKey("FK2", new ImmutableColumnReference(1, fk2Col, id2)));
    unknownTable.addForeignKey(
        new MutableForeignKey("FK3", new ImmutableColumnReference(1, fk3Col, id3)));
    fk1Col.setReferencedColumn(id1);
    fk2Col.setReferencedColumn(id2);
    fk3Col.setReferencedColumn(id3);

    final EntityType entityType = new TableEntityModelInferrer(unknownTable).inferEntityType();
    assertThat(entityType, is(EntityType.unknown));
  }

  /**
   * Test for a weak entity table.
   *
   * <pre>
   *   PARENT_TABLE
   *   ------------
   *   ID (PK)
   *
   *   WEAK_ENTITY_TABLE
   *   -----------------
   *   PARENT_ID (PK, FK referencing PARENT_TABLE.ID)
   *   DISCRIMINATOR (PK)
   * </pre>
   */
  @Test
  public void testWeakEntity() {
    final SchemaReference schema = new SchemaReference("catalog", "schema");

    final MutableTable parentTable = new MutableTable(schema, "PARENT_TABLE");
    final MutableColumn parentId = new MutableColumn(parentTable, "ID");
    parentTable.addColumn(parentId);
    final MutablePrimaryKey parentPk = MutablePrimaryKey.newPrimaryKey(parentTable, "PK_PARENT");
    parentPk.addColumn(new MutableTableConstraintColumn(parentPk, parentId));
    parentTable.setPrimaryKey(parentPk);

    final MutableTable weakTable = new MutableTable(schema, "WEAK_ENTITY_TABLE");
    final MutableColumn parentIdInWeak = new MutableColumn(weakTable, "PARENT_ID");
    final MutableColumn discriminator = new MutableColumn(weakTable, "DISCRIMINATOR");
    weakTable.addColumn(parentIdInWeak);
    weakTable.addColumn(discriminator);

    final MutablePrimaryKey weakPk = MutablePrimaryKey.newPrimaryKey(weakTable, "PK_WEAK");
    weakPk.addColumn(new MutableTableConstraintColumn(weakPk, parentIdInWeak));
    weakPk.addColumn(new MutableTableConstraintColumn(weakPk, discriminator));
    weakTable.setPrimaryKey(weakPk);

    final ImmutableColumnReference columnReference =
        new ImmutableColumnReference(1, parentIdInWeak, parentId);
    final MutableForeignKey fk = new MutableForeignKey("FK_WEAK", columnReference);
    weakTable.addForeignKey(fk);
    parentIdInWeak.setReferencedColumn(parentId);

    final EntityType entityType = new TableEntityModelInferrer(weakTable).inferEntityType();
    assertThat(entityType, is(EntityType.weak_entity));
  }

  @Test
  public void testTablePartial() {
    final SchemaReference schema = new SchemaReference("catalog", "schema");
    final TablePartial table = new TablePartial(schema, "TABLE_PARTIAL");

    assertThrows(IllegalArgumentException.class, () -> new TableEntityModelInferrer(table));
  }
}
