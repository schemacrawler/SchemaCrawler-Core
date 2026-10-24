/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.sort;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static schemacrawler.crawl.SortTestUtility.newProcedure;
import static schemacrawler.crawl.SortTestUtility.newTable;
import static schemacrawler.crawl.SortTestUtility.setSortIndex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import schemacrawler.namedobject.BuildableDatabaseObjectIndex;
import schemacrawler.namedobject.DatabaseObjectIndex;
import schemacrawler.namedobject.NamedObjectIndexes;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaReference;

public class DatabaseObjectIndexSortTest {

  @ParameterizedTest(name = "immutable={0}")
  @ValueSource(booleans = {true, false})
  public void enumerationsAreFreshSortedDetachedAndUnmodifiable(final boolean immutable) {
    final Table first = newTable(new SchemaReference(), "first");
    setSortIndex(first, 1);
    final Table second = newTable(new SchemaReference(), "second");
    setSortIndex(second, 2);
    final DatabaseObjectIndex<Table> index = index(immutable, List.of(second, first));
    final List<Table> values = index.getObjects();
    final List<Table> snapshot = new ArrayList<>(index.snapshot());
    final Iterator<Table> iterator = index.getObjects().iterator();
    setSortIndex(second, 0);

    assertThat(values, contains(first, second));
    assertThat(index.getObjects(), contains(second, first));
    assertThat(snapshot, containsInAnyOrder(first, second));
    assertThat(iterator.next(), sameInstance(first));
    assertThat(iterator.next(), sameInstance(second));
    assertThat(iterator.hasNext(), is(false));
    assertThrows(UnsupportedOperationException.class, iterator::remove);
    assertThrows(UnsupportedOperationException.class, () -> values.clear());
    assertThrows(UnsupportedOperationException.class, () -> index.snapshot().clear());
    assertThat(index.toString(), is(second + ", " + first));
  }

  @ParameterizedTest(name = "immutable={0}")
  @ValueSource(booleans = {true, false})
  public void routineIndexSelectsNaturalFirstOverloadAndPreservesCompleteKeys(
      final boolean immutable) {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Routine earlier = newProcedure(schema, "run", "a");
    final Routine later = newProcedure(schema, "run", "z");
    final Routine nullSpecific = newProcedure(schema, "nullable", null);
    final Routine emptySpecific = newProcedure(schema, "nullable", "");
    final DatabaseObjectIndex<Routine> index =
        index(immutable, List.of(later, earlier, nullSpecific, emptySpecific), true);

    assertThat(index.lookup(schema, "run").orElseThrow(), sameInstance(earlier));
    assertThat(index.getObjects(schema, "run"), contains(earlier, later));
    assertThrows(
        UnsupportedOperationException.class, () -> index.getObjects(schema, "run").clear());
    assertThat(index.lookup(earlier.getFullName()).orElseThrow(), sameInstance(earlier));
    assertThat(index.lookup(schema, "a"), is(Optional.empty()));
    for (final Routine routine : List.of(earlier, later, nullSpecific, emptySpecific)) {
      assertThat(index.lookup(routine.key()).orElseThrow(), sameInstance(routine));
    }
    assertThat(index.lookup(schema, "nullable").isPresent(), is(true));
    assertThat(index.lookup(schema.key().with("nullable")), is(Optional.empty()));
  }

  @ParameterizedTest(name = "immutable={0}")
  @ValueSource(booleans = {true, false})
  public void overloadLookupUsesNaturalOrderRatherThanKeyOrder(final boolean immutable) {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Routine first = mock(Routine.class);
    final Routine second = mock(Routine.class);
    when(first.key()).thenReturn(schema.key().with("run").with("z"));
    when(second.key()).thenReturn(schema.key().with("run").with("a"));
    when(first.compareTo(second)).thenReturn(-1);
    when(second.compareTo(first)).thenReturn(1);

    for (final List<Routine> input : List.of(List.of(first, second), List.of(second, first))) {
      final DatabaseObjectIndex<Routine> index = index(immutable, input, true);
      assertThat(index.lookup(schema, "run").orElseThrow(), sameInstance(first));
      assertThat(index.getObjects(schema, "run"), contains(first, second));
    }
    final AssertionError failure = new AssertionError("Actual candidates cannot be compared");
    when(first.compareTo(any(NamedObject.class))).thenThrow(failure);
    when(second.compareTo(any(NamedObject.class))).thenThrow(failure);
    final DatabaseObjectIndex<Routine> index = index(immutable, List.of(second, first), true);
    assertThat(
        assertThrows(AssertionError.class, () -> index.lookup(schema, "run")),
        sameInstance(failure));
    assertThat(
        assertThrows(AssertionError.class, () -> index.getObjects(schema, "run")),
        sameInstance(failure));
  }

  @ParameterizedTest(name = "immutable={0}")
  @ValueSource(booleans = {true, false})
  public void serializationPreservesSortedValuesAndSchemaLookup(final boolean immutable) {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Table earlier = newTable(schema, "A");
    final Table later = newTable(schema, "Z");
    final DatabaseObjectIndex<Table> original = index(immutable, List.of(later, earlier));
    final DatabaseObjectIndex<Table> copy = SerializationUtils.clone(original);
    assertThat(
        copy.getObjects().stream().map(Table::key).toList(), contains(earlier.key(), later.key()));
    assertThat(copy.snapshot().size(), is(2));
    assertThat(copy.lookup(schema, "A").orElseThrow().key(), is(earlier.key()));
    assertThat(copy.toString(), is(original.toString()));
  }

  private static <D extends DatabaseObject> DatabaseObjectIndex<D> index(
      final boolean immutable, final List<D> values) {
    if (immutable) {
      return NamedObjectIndexes.newImmutableDatabaseObjectIndex(values);
    }
    final BuildableDatabaseObjectIndex<D> index =
        NamedObjectIndexes.newBuildableDatabaseObjectIndex();
    values.forEach(index::add);
    return index;
  }

  private static DatabaseObjectIndex<Routine> index(
      final boolean immutable, final List<Routine> values, final boolean supportsOverloads) {
    if (immutable) {
      return NamedObjectIndexes.newImmutableRoutineIndex(values);
    }
    final BuildableDatabaseObjectIndex<Routine> index =
        NamedObjectIndexes.newBuildableRoutineIndex();
    values.forEach(index::add);
    return index;
  }
}
