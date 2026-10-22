/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import schemacrawler.namedobject.BuildableDatabaseObjectIndex;
import schemacrawler.namedobject.BuildableDependantObjectIndex;
import schemacrawler.namedobject.BuildableNamedObjectIndex;
import schemacrawler.namedobject.DatabaseObjectIndex;
import schemacrawler.namedobject.NamedObjectIndex;
import schemacrawler.namedobject.NamedObjectIndexes;
import schemacrawler.schema.Column;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.test.utility.crawl.LightProcedure;
import schemacrawler.test.utility.crawl.LightTable;

public class NamedObjectIndexesTest {

  @Test
  public void concurrentNamedAndSchemaFactoriesCreateIndependentMutableIndexes() {
    final Schema first = new SchemaReference(null, "APP");
    final Schema replacement = new SchemaReference(null, "APP");
    final Schema distinct = new SchemaReference("", "APP");
    final Schema blank = new SchemaReference();
    final BuildableNamedObjectIndex<Schema> schemas = NamedObjectIndexes.newBuildableSchemaIndex();
    final BuildableNamedObjectIndex<Schema> namedObjects =
        NamedObjectIndexes.newBuildableNamedObjectIndex();
    for (final BuildableNamedObjectIndex<Schema> index : List.of(schemas, namedObjects)) {
      assertTrue(index.isEmpty());
      index.add(first);
      index.add(replacement);
      index.add(distinct);
      index.add(blank);
      assertEquals(3, index.size());
      assertSame(replacement, index.lookup(first.key()).orElseThrow());
      assertSame(distinct, index.lookup(distinct.key()).orElseThrow());
      assertSame(blank, index.lookup("").orElseThrow());
      assertSame(replacement, index.remove(first).orElseThrow());
    }
    schemas.remove(blank);
    assertTrue(namedObjects.lookup("").isPresent());
    assertTrue(NamedObjectIndexes.newBuildableSchemaIndex().isEmpty());
  }

  @Test
  public void immutableNamedAndSchemaFactoriesCopyMembershipAndKeepLastDuplicate() {
    final Schema first = new SchemaReference(null, "APP");
    final Schema replacement = new SchemaReference(null, "APP");
    final Schema distinct = new SchemaReference("", "APP");
    final Schema blank = new SchemaReference();
    final List<Schema> source = new ArrayList<>(List.of(first, replacement, distinct, blank));
    final NamedObjectIndex<Schema> schemas = NamedObjectIndexes.newImmutableSchemaIndex(source);
    final NamedObjectIndex<Schema> namedObjects =
        NamedObjectIndexes.newImmutableNamedObjectIndex(source);
    source.clear();
    for (final NamedObjectIndex<Schema> index : List.of(schemas, namedObjects)) {
      assertEquals(3, index.size());
      assertSame(replacement, index.lookup(first.key()).orElseThrow());
      assertSame(distinct, index.lookup(distinct.key()).orElseThrow());
      assertSame(blank, index.lookup("").orElseThrow());
      assertThrows(UnsupportedOperationException.class, () -> index.snapshot().clear());
    }
    assertTrue(NamedObjectIndexes.newImmutableNamedObjectIndex(null).isEmpty());
    assertTrue(NamedObjectIndexes.newImmutableSchemaIndex(null).isEmpty());
  }

  @Test
  public void databaseFactoriesRetainExactLookupAndDuplicatePolicies() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Table first = new LightTable(schema, "TABLE");
    final Table replacement = new LightTable(schema, "TABLE");
    final BuildableDatabaseObjectIndex<Table> mutable =
        NamedObjectIndexes.newBuildableDatabaseObjectIndex();
    mutable.add(first);
    mutable.add(replacement);
    assertSame(replacement, mutable.lookup(schema, "TABLE").orElseThrow());

    final List<Table> source = new ArrayList<>(List.of(first, replacement));
    final DatabaseObjectIndex<Table> immutable =
        NamedObjectIndexes.newImmutableDatabaseObjectIndex(source);
    source.clear();
    assertSame(first, immutable.lookup(schema, "TABLE").orElseThrow());
    assertEquals(1, immutable.size());
    assertThrows(UnsupportedOperationException.class, () -> immutable.snapshot().clear());

    final Routine routine = new LightProcedure(schema, "run", "specific");
    final DatabaseObjectIndex<Routine> ordinary =
        NamedObjectIndexes.newImmutableDatabaseObjectIndex(List.of(routine));
    assertTrue(ordinary.lookup(schema, "run").isEmpty());
    assertSame(routine, ordinary.lookup(routine.key()).orElseThrow());
  }

  @Test
  public void dependentFactoryRetainsParentLookupAndMutation() {
    final LightTable table = new LightTable("TABLE");
    final Column column = table.addColumn("COLUMN");
    final BuildableDependantObjectIndex<Column, Table> index =
        NamedObjectIndexes.newBuildableDependantObjectIndex();
    assertTrue(index.add(column));
    assertSame(column, index.lookup(table, "COLUMN").orElseThrow());
    assertSame(column, index.remove(column).orElseThrow());
    assertTrue(index.isEmpty());
  }

  @Test
  public void immutableFactoriesPreserveNullValidation() {
    assertThrows(
        NullPointerException.class, () -> NamedObjectIndexes.newImmutableDatabaseObjectIndex(null));
    assertThrows(
        NullPointerException.class, () -> NamedObjectIndexes.newImmutableRoutineIndex(null));
    assertThrows(
        NullPointerException.class,
        () -> NamedObjectIndexes.newImmutableNamedObjectIndex(Arrays.asList((Schema) null)));
    assertThrows(
        NullPointerException.class,
        () -> NamedObjectIndexes.newImmutableSchemaIndex(Arrays.asList((Schema) null)));
    assertThrows(
        NullPointerException.class,
        () -> NamedObjectIndexes.newImmutableDatabaseObjectIndex(Arrays.asList((Table) null)));
    assertThrows(
        NullPointerException.class,
        () -> NamedObjectIndexes.newImmutableRoutineIndex(Arrays.asList((Routine) null)));
  }

  @ParameterizedTest(name = "immutable={0}")
  @ValueSource(booleans = {true, false})
  public void routineFactoriesEnableOverloadsAndRetainPolicyAfterSerialization(
      final boolean immutable) {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Routine first = new LightProcedure(schema, "run", "a");
    final Routine second = new LightProcedure(schema, "run", "z");
    final DatabaseObjectIndex<Routine> index;
    if (immutable) {
      index = NamedObjectIndexes.newImmutableRoutineIndex(List.of(second, first));
    } else {
      final BuildableDatabaseObjectIndex<Routine> mutable =
          NamedObjectIndexes.newBuildableRoutineIndex();
      mutable.add(second);
      mutable.add(first);
      index = mutable;
    }
    assertEquals(List.of(first, second), index.getObjects(schema, "run"));
    assertSame(first, index.lookup(schema, "run").orElseThrow());
    assertSame(second, index.lookup(second.key()).orElseThrow());
    assertTrue(index.hasObjects(schema));
    assertFalse(index.hasObjects(null));
    assertThrows(
        UnsupportedOperationException.class, () -> index.getObjects(schema, "run").clear());
    final DatabaseObjectIndex<Routine> copy = SerializationUtils.clone(index);
    assertEquals(first.key(), copy.lookup(schema, "run").orElseThrow().key());
    assertEquals(2, copy.getObjects(schema, "run").size());
  }
}
