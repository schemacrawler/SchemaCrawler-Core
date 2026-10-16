/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serial;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.test.utility.crawl.LightTable;
import schemacrawler.utility.ConcurrentNamedObjectIndex;
import schemacrawler.utility.ImmutableNamedObjectIndex;
import schemacrawler.utility.NamedObjectIndex;

public class NamedObjectIndexTest {

  private static class TestNamedObject implements NamedObject {

    @Serial private static final long serialVersionUID = 1L;

    private final NamedObjectKey key;
    private final String name;
    private int order;

    TestNamedObject(final NamedObjectKey key, final String name, final int order) {
      this.key = key;
      this.name = name;
      this.order = order;
    }

    @Override
    public int compareTo(final NamedObject other) {
      return Integer.compare(order, ((TestNamedObject) other).order);
    }

    @Override
    public String getFullName() {
      return name;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public NamedObjectKey key() {
      return key;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  private static class KeyedLightTable extends LightTable {

    @Serial private static final long serialVersionUID = 1L;

    private final NamedObjectKey key;

    KeyedLightTable(final Schema schema, final String name, final NamedObjectKey key) {
      super(schema, name);
      this.key = key;
    }

    @Override
    public NamedObjectKey key() {
      return key;
    }
  }

  private static final class ComparisonFailingTable extends LightTable {

    @Serial private static final long serialVersionUID = 1L;

    ComparisonFailingTable(final Schema schema, final String name) {
      super(schema, name);
    }

    @Override
    public int compareTo(final NamedObject other) {
      throw new AssertionError("A non-matching object was compared");
    }
  }

  @Test
  public void concurrentIndexAddsReplacesAndRemovesAtomically() {
    final ConcurrentNamedObjectIndex<TestNamedObject> index = new ConcurrentNamedObjectIndex<>();
    final NamedObjectKey key = new NamedObjectKey("object");
    final TestNamedObject first = new TestNamedObject(key, "first", 0);
    final TestNamedObject replacement = new TestNamedObject(key, "replacement", 1);

    assertThat(index.add(first), is(true));
    assertThat(index.add(replacement), is(true));
    assertThat(index.lookup(key).orElseThrow(), is(replacement));
    assertThat(index.size(), is(1));
    assertThat(index.containsKey(key), is(true));
    assertThat(index.remove(key).orElseThrow(), is(replacement));
    assertThat(index.remove(key), is(Optional.empty()));
    assertThat(index.isEmpty(), is(true));
  }

  @Test
  public void concurrentIndexSnapshotAndValuesAreDetachedAndUnmodifiable() {
    final ConcurrentNamedObjectIndex<TestNamedObject> index = new ConcurrentNamedObjectIndex<>();
    final TestNamedObject first = new TestNamedObject(new NamedObjectKey("first"), "first", 1);
    final TestNamedObject second = new TestNamedObject(new NamedObjectKey("second"), "second", 0);
    index.add(first);
    index.add(second);
    final ImmutableNamedObjectIndex<TestNamedObject> immutable =
        new ImmutableNamedObjectIndex<>(List.of(first, second));

    final String concurrentRendering = index.toString();
    final List<TestNamedObject> snapshot = index.snapshot();
    final List<TestNamedObject> values = index.values();
    index.remove(first.key());

    assertThat(values, contains(second, first));
    assertThat(concurrentRendering, is(immutable.toString()));
    assertThat(snapshot.size(), is(2));
    assertThrows(UnsupportedOperationException.class, () -> snapshot.add(first));
    assertThrows(UnsupportedOperationException.class, () -> values.clear());
  }

  @Test
  public void concurrentSchemaNameLookupFiltersBeforeComparingAndTreatsBlankLiterally() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Schema otherSchema = new SchemaReference("CATALOG", "OTHER");
    final KeyedLightTable routine =
        new KeyedLightTable(schema, "routine", schema.key().with("routine").with("specific"));
    final ComparisonFailingTable nonmatching = new ComparisonFailingTable(otherSchema, "routine");
    final ConcurrentNamedObjectIndex<NamedObject> index = new ConcurrentNamedObjectIndex<>();
    index.add(nonmatching);
    index.add(routine);

    assertThat(
        index.lookup(new SchemaReference("CATALOG", "PUBLIC"), "routine").orElseThrow(),
        is(routine));
    assertThat(index.lookup(schema, "specific"), is(Optional.empty()));
    assertThat(index.lookup(otherSchema, "routine").orElseThrow(), is(nonmatching));

    final Table blankNamedTable = mock(Table.class);
    when(blankNamedTable.getSchema()).thenReturn(schema);
    when(blankNamedTable.getName()).thenReturn("");
    when(blankNamedTable.key()).thenReturn(schema.key().with("").with("specific"));
    when(blankNamedTable.compareTo(any(NamedObject.class))).thenReturn(0);
    index.add(blankNamedTable);
    assertThat(index.lookup(schema, "").orElseThrow(), is(blankNamedTable));
  }

  @Test
  public void concurrentSchemaNameLookupSelectsNaturalFirstOverload() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final KeyedLightTable later =
        new KeyedLightTable(schema, "routine", schema.key().with("routine").with("z"));
    final KeyedLightTable earlier =
        new KeyedLightTable(schema, "routine", schema.key().with("routine").with("a"));
    final ConcurrentNamedObjectIndex<KeyedLightTable> index = new ConcurrentNamedObjectIndex<>();
    index.add(later);
    index.add(earlier);

    assertThat(index.lookup(schema, "routine").orElseThrow(), is(earlier));
    assertThat(index.lookup(earlier.key()).orElseThrow(), is(earlier));
    assertThat(index.lookup(later.key()).orElseThrow(), is(later));
  }

  @Test
  public void concurrentWritersRetainDistinctKeysAndSameKeyWritesReplace() throws Exception {
    final ConcurrentNamedObjectIndex<TestNamedObject> distinctIndex =
        new ConcurrentNamedObjectIndex<>();
    final List<Callable<Void>> distinctWriters = new ArrayList<>();
    for (int writer = 0; writer < 4; writer++) {
      final int writerId = writer;
      distinctWriters.add(
          () -> {
            for (int item = 0; item < 25; item++) {
              final NamedObjectKey key = new NamedObjectKey("writer", writerId + "", item + "");
              distinctIndex.add(new TestNamedObject(key, key.join(), item));
            }
            return null;
          });
    }
    runTogether(distinctWriters);
    assertThat(distinctIndex.size(), is(100));
    assertThat(distinctIndex.values().size(), is(100));

    final ConcurrentNamedObjectIndex<TestNamedObject> sameKeyIndex =
        new ConcurrentNamedObjectIndex<>();
    final NamedObjectKey sharedKey = new NamedObjectKey("shared");
    final List<Callable<Void>> sameKeyWriters = new ArrayList<>();
    for (int writer = 0; writer < 4; writer++) {
      final int writerId = writer;
      sameKeyWriters.add(
          () -> {
            for (int item = 0; item < 25; item++) {
              sameKeyIndex.add(new TestNamedObject(sharedKey, writerId + "-" + item, item));
            }
            return null;
          });
    }
    runTogether(sameKeyWriters);
    assertThat(sameKeyIndex.size(), is(1));
    assertThat(sameKeyIndex.lookup(sharedKey).isPresent(), is(true));
  }

  @Test
  public void concurrentWritersCanOverlapSnapshotsAndRemoval() throws Exception {
    final ConcurrentNamedObjectIndex<TestNamedObject> index = new ConcurrentNamedObjectIndex<>();
    final List<Callable<Void>> workers =
        List.of(
            () -> {
              for (int item = 0; item < 200; item++) {
                final NamedObjectKey key = new NamedObjectKey("changing", item + "");
                index.add(new TestNamedObject(key, key.join(), item));
                index.remove(key);
              }
              return null;
            },
            () -> {
              for (int item = 0; item < 200; item++) {
                index.snapshot();
                index.values();
              }
              return null;
            });

    runTogether(workers);
    assertThat(index.isEmpty(), is(true));
  }

  @Test
  public void immutableIndexDeduplicatesBeforeSortingAndDetachesCollection() {
    final NamedObjectKey firstKey = new NamedObjectKey("first");
    final NamedObjectKey secondKey = new NamedObjectKey("second");
    final TestNamedObject first = new TestNamedObject(firstKey, "first", 2);
    final TestNamedObject second = new TestNamedObject(secondKey, "second", 1);
    final TestNamedObject duplicate = new TestNamedObject(firstKey, "duplicate", 0);
    final Collection<TestNamedObject> source = new ArrayList<>(List.of(first, second, duplicate));
    final ImmutableNamedObjectIndex<TestNamedObject> index =
        new ImmutableNamedObjectIndex<>(source);
    source.clear();

    assertThat(index.size(), is(2));
    assertThat(index.lookup(firstKey).orElseThrow(), is(first));
    assertThat(index.snapshot(), contains(first, second));
    assertThat(index.values(), contains(second, first));
    assertThat(index.containsKey(firstKey), is(true));
    assertThat(index.lookup(new NamedObjectKey("missing")), is(Optional.empty()));
    assertThrows(UnsupportedOperationException.class, () -> index.snapshot().clear());
    assertThrows(UnsupportedOperationException.class, () -> index.values().clear());
  }

  @Test
  public void immutableIndexReordersFreshlyAndPreservesTieEncounterOrder() {
    final TestNamedObject first = new TestNamedObject(new NamedObjectKey("first"), "first", 1);
    final TestNamedObject tied = new TestNamedObject(new NamedObjectKey("tied"), "tied", 1);
    final TestNamedObject last = new TestNamedObject(new NamedObjectKey("last"), "last", 2);
    final ImmutableNamedObjectIndex<TestNamedObject> index =
        new ImmutableNamedObjectIndex<>(List.of(first, tied, last));

    final List<TestNamedObject> originalValues = index.values();
    last.order = 0;

    assertThat(originalValues, contains(first, tied, last));
    assertThat(index.values(), contains(last, first, tied));
  }

  @Test
  public void immutableIndexSnapshotDoesNotCompareObjects() {
    final ComparisonFailingNamedObject first =
        new ComparisonFailingNamedObject(new NamedObjectKey("first"));
    final ComparisonFailingNamedObject second =
        new ComparisonFailingNamedObject(new NamedObjectKey("second"));
    final ImmutableNamedObjectIndex<ComparisonFailingNamedObject> index =
        new ImmutableNamedObjectIndex<>(List.of(first, second));

    assertThat(index.snapshot(), contains(first, second));
    assertThrows(AssertionError.class, index::values);
  }

  @Test
  public void immutableIndexSchemaNameLookupUsesEncounterOrderAndCompleteKeys() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final KeyedLightTable first =
        new KeyedLightTable(schema, "routine", schema.key().with("routine").with("z"));
    final KeyedLightTable second =
        new KeyedLightTable(schema, "routine", schema.key().with("routine").with("a"));
    final Schema otherSchema = new SchemaReference("CATALOG", "OTHER");
    final KeyedLightTable otherSchemaRoutine =
        new KeyedLightTable(
            otherSchema, "routine", otherSchema.key().with("routine").with("specific"));
    final ImmutableNamedObjectIndex<KeyedLightTable> index =
        new ImmutableNamedObjectIndex<>(List.of(first, second, otherSchemaRoutine));

    assertThat(index.lookup(schema, "routine").orElseThrow(), is(first));
    assertThat(index.lookup(otherSchema, "routine").orElseThrow(), is(otherSchemaRoutine));
    assertThat(index.lookup(schema, "a"), is(Optional.empty()));
    assertThat(index.lookup(first.key()).orElseThrow(), is(first));
    assertThat(index.lookup(second.key()).orElseThrow(), is(second));
  }

  @Test
  public void immutableAndConcurrentIndexesSerializeTheirContracts() {
    final TestNamedObject first = new TestNamedObject(new NamedObjectKey("first"), "first", 1);
    final TestNamedObject second = new TestNamedObject(new NamedObjectKey("second"), "second", 0);
    final ImmutableNamedObjectIndex<TestNamedObject> immutable =
        new ImmutableNamedObjectIndex<>(List.of(first, second));
    final ImmutableNamedObjectIndex<TestNamedObject> immutableClone =
        SerializationUtils.clone(immutable);

    assertThat(
        immutableClone.snapshot().stream().map(NamedObject::getName).toList(),
        contains("first", "second"));
    assertThat(
        immutableClone.values().stream().map(NamedObject::getName).toList(),
        contains("second", "first"));
    assertThat(immutableClone.toString(), is("second, first"));

    final ConcurrentNamedObjectIndex<TestNamedObject> concurrent =
        new ConcurrentNamedObjectIndex<>();
    concurrent.add(first);
    final ConcurrentNamedObjectIndex<TestNamedObject> concurrentClone =
        SerializationUtils.clone(concurrent);
    final TestNamedObject afterRead =
        new TestNamedObject(new NamedObjectKey("after-read"), "after-read", 2);
    assertThat(concurrentClone.add(afterRead), is(true));
    assertThat(concurrentClone.lookup(afterRead.key()).orElseThrow().getName(), is("after-read"));
  }

  @Test
  public void indexesExposePublicReadContractWithoutIterable() {
    final NamedObjectIndex<NamedObjectKey, TestNamedObject> immutable =
        new ImmutableNamedObjectIndex<>(List.of());
    final NamedObjectIndex<NamedObjectKey, TestNamedObject> concurrent =
        new ConcurrentNamedObjectIndex<>();

    assertThat(immutable.isEmpty(), is(true));
    assertThat(concurrent.isEmpty(), is(true));
    assertThat(immutable.values(), is(empty()));
    assertThat(concurrent.values(), is(empty()));
    assertThat(immutable.toString(), is(""));
    assertThat(concurrent.toString(), is(""));
    assertThat(Iterable.class.isAssignableFrom(NamedObjectIndex.class), is(false));
    assertThat(Iterable.class.isAssignableFrom(ImmutableNamedObjectIndex.class), is(false));
    assertThat(Iterable.class.isAssignableFrom(ConcurrentNamedObjectIndex.class), is(false));
  }

  @Test
  public void indexesRejectNullRequiredArgumentsAndNullStoredKeys() {
    final ImmutableNamedObjectIndex<TestNamedObject> immutable =
        new ImmutableNamedObjectIndex<>(List.of());
    final ConcurrentNamedObjectIndex<TestNamedObject> concurrent =
        new ConcurrentNamedObjectIndex<>();
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");

    assertThrows(
        NullPointerException.class, () -> new ImmutableNamedObjectIndex<TestNamedObject>(null));
    assertThrows(
        NullPointerException.class,
        () -> new ImmutableNamedObjectIndex<>(java.util.Arrays.asList((TestNamedObject) null)));
    assertThrows(NullPointerException.class, () -> immutable.lookup((NamedObjectKey) null));
    assertThrows(NullPointerException.class, () -> immutable.containsKey(null));
    assertThrows(NullPointerException.class, () -> immutable.lookup(null, "name"));
    assertThrows(NullPointerException.class, () -> immutable.lookup(schema, null));
    assertThrows(NullPointerException.class, () -> concurrent.add(null));
    assertThrows(NullPointerException.class, () -> concurrent.lookup((NamedObjectKey) null));
    assertThrows(NullPointerException.class, () -> concurrent.containsKey(null));
    assertThrows(NullPointerException.class, () -> concurrent.remove(null));
    assertThrows(NullPointerException.class, () -> concurrent.lookup(null, "name"));
    assertThat(concurrent.lookup(schema, null), is(Optional.empty()));
    assertThrows(
        NullPointerException.class,
        () -> new ImmutableNamedObjectIndex<>(List.of(new TestNamedObject(null, "no-key", 0))));
    assertThrows(
        NullPointerException.class, () -> concurrent.add(new TestNamedObject(null, "no-key", 0)));
  }

  @Test
  public void schemaNameLookupExcludesNonDatabaseAndSchemaLessObjects() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Table schemaLessTable = mock(Table.class);
    when(schemaLessTable.getSchema()).thenReturn(null);
    when(schemaLessTable.getName()).thenReturn("target");
    when(schemaLessTable.key()).thenReturn(new NamedObjectKey("schema-less", "target"));
    final TestNamedObject nonDatabaseObject =
        new TestNamedObject(schema.key().with("target"), "target", 0);
    final ImmutableNamedObjectIndex<NamedObject> immutable =
        new ImmutableNamedObjectIndex<>(List.of(schemaLessTable, nonDatabaseObject));
    final ConcurrentNamedObjectIndex<NamedObject> concurrent = new ConcurrentNamedObjectIndex<>();
    concurrent.add(schemaLessTable);
    concurrent.add(nonDatabaseObject);

    assertThat(immutable.lookup(schema, "target"), is(Optional.empty()));
    assertThat(concurrent.lookup(schema, "target"), is(Optional.empty()));
  }

  @Test
  public void schemaNameLookupMatchesLiteralQuotedAndDottedNames() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final String exactName = "\"routine.with.dot\"";
    final KeyedLightTable table =
        new KeyedLightTable(schema, exactName, schema.key().with(exactName).with("specific"));
    final ImmutableNamedObjectIndex<KeyedLightTable> immutable =
        new ImmutableNamedObjectIndex<>(List.of(table));
    final ConcurrentNamedObjectIndex<KeyedLightTable> concurrent =
        new ConcurrentNamedObjectIndex<>();
    concurrent.add(table);

    assertThat(immutable.lookup(schema, exactName).orElseThrow(), is(table));
    assertThat(concurrent.lookup(schema, exactName).orElseThrow(), is(table));
    assertThat(immutable.lookup(schema, "routine.with.dot"), is(Optional.empty()));
    assertThat(concurrent.lookup(schema, "routine.with.dot"), is(Optional.empty()));
  }

  @Test
  public void schemaNameLookupSupportsNullAndEmptySpecificNameKeyParts() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final KeyedLightTable nullSpecific =
        new KeyedLightTable(schema, "routine", schema.key().with("routine").with(null));
    final KeyedLightTable emptySpecific =
        new KeyedLightTable(schema, "routine", schema.key().with("routine").with(""));
    final ConcurrentNamedObjectIndex<KeyedLightTable> index = new ConcurrentNamedObjectIndex<>();
    index.add(nullSpecific);
    index.add(emptySpecific);

    assertThat(index.lookup(schema, "routine").isPresent(), is(true));
    assertThat(index.lookup(schema, "nullSpecific"), is(Optional.empty()));
    assertThat(index.lookup(nullSpecific.key()).orElseThrow(), is(nullSpecific));
    assertThat(index.lookup(emptySpecific.key()).orElseThrow(), is(emptySpecific));
    assertThat(index.lookup(schema.key().with("routine")), is(Optional.empty()));
  }

  private void runTogether(final List<Callable<Void>> workers) throws Exception {
    final ExecutorService executor = Executors.newFixedThreadPool(workers.size());
    final CountDownLatch ready = new CountDownLatch(workers.size());
    final CountDownLatch start = new CountDownLatch(1);
    final List<Future<Void>> results = new ArrayList<>();
    try {
      for (final Callable<Void> worker : workers) {
        results.add(
            executor.submit(
                () -> {
                  ready.countDown();
                  if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Workers did not start together");
                  }
                  return worker.call();
                }));
      }
      assertThat(ready.await(10, TimeUnit.SECONDS), is(true));
      start.countDown();
      for (final Future<Void> result : results) {
        result.get(Duration.ofSeconds(10).toMillis(), TimeUnit.MILLISECONDS);
      }
    } finally {
      start.countDown();
      executor.shutdownNow();
    }
  }

  private static final class ComparisonFailingNamedObject implements NamedObject {

    @Serial private static final long serialVersionUID = 1L;

    private final NamedObjectKey key;

    ComparisonFailingNamedObject(final NamedObjectKey key) {
      this.key = key;
    }

    @Override
    public int compareTo(final NamedObject other) {
      throw new AssertionError("Snapshot must not compare objects");
    }

    @Override
    public String getFullName() {
      return key.join();
    }

    @Override
    public String getName() {
      return key.join();
    }

    @Override
    public NamedObjectKey key() {
      return key;
    }
  }
}
