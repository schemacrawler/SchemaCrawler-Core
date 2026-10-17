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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serial;
import java.time.Duration;
import java.util.ArrayList;
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
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.test.utility.TestNamedObject;
import schemacrawler.test.utility.crawl.LightProcedure;
import schemacrawler.utility.ConcurrentNamedObjectIndex;
import schemacrawler.utility.ImmutableNamedObjectIndex;
import schemacrawler.utility.NamedObjectIndex;

public class NamedObjectIndexTest {

  private static final class ComparisonFailingRoutine extends LightProcedure {

    @Serial private static final long serialVersionUID = 1L;

    ComparisonFailingRoutine(final Schema schema, final String name) {
      super(schema, name);
    }

    @Override
    public int compareTo(final NamedObject other) {
      throw new AssertionError("A non-matching object was compared");
    }
  }

  private static final class MutableOrderNamedObject implements NamedObject {

    @Serial private static final long serialVersionUID = 1L;

    private final NamedObjectKey key;
    private final String name;
    private int order;

    MutableOrderNamedObject(final String name, final int order) {
      key = new NamedObjectKey(name);
      this.name = name;
      this.order = order;
    }

    @Override
    public int compareTo(final NamedObject other) {
      return Integer.compare(order, ((MutableOrderNamedObject) other).order);
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
  }

  @Test
  public void concurrentIndexSnapshotAndValuesAreDetachedAndUnmodifiable() {
    final ConcurrentNamedObjectIndex<TestNamedObject> index = new ConcurrentNamedObjectIndex<>();
    final TestNamedObject first = new TestNamedObject("first", 1);
    final TestNamedObject second = new TestNamedObject("second", 0);
    index.add(first);
    index.add(second);
    final ImmutableNamedObjectIndex<TestNamedObject> immutable =
        new ImmutableNamedObjectIndex<>(List.of(first, second));

    final String concurrentRendering = index.toString();
    final List<TestNamedObject> snapshot = index.snapshot();
    final List<TestNamedObject> values = index.getObjects();
    index.remove(first);

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
    final Routine routine = new LightProcedure(schema, "routine");
    final Routine nonmatching = new ComparisonFailingRoutine(otherSchema, "routine");
    final ConcurrentNamedObjectIndex<Routine> index = new ConcurrentNamedObjectIndex<>();
    index.add(nonmatching);
    index.add(routine);

    assertThat(
        index.lookup(new SchemaReference("CATALOG", "PUBLIC"), "routine").orElseThrow(),
        is(routine));
    assertThat(index.lookup(schema, "specific"), is(Optional.empty()));
    assertThat(index.lookup(otherSchema, "routine").orElseThrow(), is(nonmatching));

    final Routine blankNamed = mock(Routine.class);
    when(blankNamed.getSchema()).thenReturn(schema);
    when(blankNamed.getName()).thenReturn("");
    when(blankNamed.key()).thenReturn(schema.key().with("").with("specific"));
    when(blankNamed.compareTo(any(NamedObject.class))).thenReturn(0);
    index.add(blankNamed);
    assertThat(index.lookup(schema, "").orElseThrow(), is(blankNamed));
  }

  @Test
  public void concurrentSchemaNameLookupSelectsNaturalFirstOverload() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Routine later = new LightProcedure(schema, "routine", "z");
    final Routine earlier = new LightProcedure(schema, "routine", "a");
    final ConcurrentNamedObjectIndex<Routine> index = new ConcurrentNamedObjectIndex<>();
    index.add(later);
    index.add(earlier);

    assertThat(index.lookup(schema, "routine").orElseThrow(), is(earlier));
    assertThat(index.lookup(earlier.key()).orElseThrow(), is(earlier));
    assertThat(index.lookup(later.key()).orElseThrow(), is(later));
  }

  @Test
  public void concurrentWritersCanOverlapSnapshotsAndRemoval() throws Exception {
    final ConcurrentNamedObjectIndex<TestNamedObject> index = new ConcurrentNamedObjectIndex<>();
    final List<Callable<Void>> workers =
        List.of(
            () -> {
              for (int item = 0; item < 200; item++) {
                final NamedObjectKey key = new NamedObjectKey("changing", item + "");
                final TestNamedObject namedObject = new TestNamedObject(key.join(), item);
                index.add(namedObject);
                index.remove(namedObject);
              }
              return null;
            },
            () -> {
              for (int item = 0; item < 200; item++) {
                index.snapshot();
                index.getObjects();
              }
              return null;
            });

    runTogether(workers);
    assertThat(index.isEmpty(), is(true));
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
              distinctIndex.add(new TestNamedObject(key.join(), item));
            }
            return null;
          });
    }
    runTogether(distinctWriters);
    assertThat(distinctIndex.size(), is(100));
    assertThat(distinctIndex.getObjects().size(), is(100));

    final ConcurrentNamedObjectIndex<TestNamedObject> sameKeyIndex =
        new ConcurrentNamedObjectIndex<>();
    final NamedObjectKey sharedKey = new NamedObjectKey("shared");
    final List<Callable<Void>> sameKeyWriters = new ArrayList<>();
    for (int writer = 0; writer < 4; writer++) {
      sameKeyWriters.add(
          () -> {
            for (int item = 0; item < 25; item++) {
              sameKeyIndex.add(new TestNamedObject(sharedKey.join(), item));
            }
            return null;
          });
    }
    runTogether(sameKeyWriters);
    assertThat(sameKeyIndex.size(), is(1));
    assertThat(sameKeyIndex.lookup(sharedKey).isPresent(), is(true));
  }

  @Test
  public void immutableAndConcurrentIndexesSerializeTheirContracts() {
    final TestNamedObject first = new TestNamedObject("first", 1);
    final TestNamedObject second = new TestNamedObject("second", 0);
    final ImmutableNamedObjectIndex<TestNamedObject> immutable =
        new ImmutableNamedObjectIndex<>(List.of(first, second));
    final ImmutableNamedObjectIndex<TestNamedObject> immutableClone =
        SerializationUtils.clone(immutable);

    assertThat(
        immutableClone.snapshot().stream().map(NamedObject::getName).toList(),
        containsInAnyOrder("first", "second"));
    assertThat(
        immutableClone.getObjects().stream().map(NamedObject::getName).toList(),
        contains("second", "first"));
    assertThat(immutableClone.toString(), is("second, first"));

    final ConcurrentNamedObjectIndex<TestNamedObject> concurrent =
        new ConcurrentNamedObjectIndex<>();
    concurrent.add(first);
    final ConcurrentNamedObjectIndex<TestNamedObject> concurrentClone =
        SerializationUtils.clone(concurrent);
    final TestNamedObject afterRead = new TestNamedObject("after-read", 2);
    assertThat(concurrentClone.add(afterRead), is(true));
    assertThat(concurrentClone.lookup(afterRead.key()).orElseThrow().getName(), is("after-read"));
  }

  @Test
  public void immutableSnapshotDoesNotCompareAndValuesSortFreshly() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final ComparisonFailingRoutine first = new ComparisonFailingRoutine(schema, "first");
    final ComparisonFailingRoutine second = new ComparisonFailingRoutine(schema, "second");
    final ImmutableNamedObjectIndex<Routine> comparisonFreeIndex =
        new ImmutableNamedObjectIndex<>(List.of(first, second));

    assertThat(comparisonFreeIndex.snapshot(), containsInAnyOrder(first, second));
    assertThrows(AssertionError.class, comparisonFreeIndex::getObjects);

    final MutableOrderNamedObject earlier = new MutableOrderNamedObject("earlier", 1);
    final MutableOrderNamedObject later = new MutableOrderNamedObject("later", 2);
    final ImmutableNamedObjectIndex<MutableOrderNamedObject> mutableOrderIndex =
        new ImmutableNamedObjectIndex<>(List.of(earlier, later));
    final List<MutableOrderNamedObject> previousValues = mutableOrderIndex.getObjects();

    later.order = 0;

    assertThat(previousValues, contains(earlier, later));
    assertThat(mutableOrderIndex.getObjects(), contains(later, earlier));
  }

  @Test
  public void schemaNameLookupExcludesNonDatabaseNamedObjects() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final NamedObject nonDatabaseObject = mock(NamedObject.class);
    when(nonDatabaseObject.getName()).thenReturn("target");
    when(nonDatabaseObject.key()).thenReturn(schema.key().with("target"));
    final ImmutableNamedObjectIndex<NamedObject> immutable =
        new ImmutableNamedObjectIndex<>(List.of(nonDatabaseObject));
    final ConcurrentNamedObjectIndex<NamedObject> concurrent = new ConcurrentNamedObjectIndex<>();
    concurrent.add(nonDatabaseObject);

    assertThat(immutable.lookup(schema, "target"), is(Optional.empty()));
    assertThat(concurrent.lookup(schema, "target"), is(Optional.empty()));
  }

  @Test
  public void indexesExposePublicReadContractWithoutIterable() {
    final NamedObjectIndex<TestNamedObject> immutable = new ImmutableNamedObjectIndex<>(List.of());
    final NamedObjectIndex<TestNamedObject> concurrent = new ConcurrentNamedObjectIndex<>();

    assertThat(immutable.isEmpty(), is(true));
    assertThat(concurrent.isEmpty(), is(true));
    assertThat(immutable.getObjects(), is(empty()));
    assertThat(concurrent.getObjects(), is(empty()));
    assertThat(immutable.toString(), is(""));
    assertThat(concurrent.toString(), is(""));
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

    assertThat(immutable.lookup((NamedObjectKey) null), is(Optional.empty()));
    assertThat(immutable.containsKey(null), is(false));
    assertThat(immutable.lookup(null, "name"), is(Optional.empty()));
    assertThat(immutable.lookup(schema, null), is(Optional.empty()));

    assertThat(concurrent.add(null), is(false));
    assertThat(concurrent.lookup((NamedObjectKey) null), is(Optional.empty()));
    assertThat(concurrent.containsKey(null), is(false));
    assertThat(concurrent.remove(null), is(Optional.empty()));
    assertThat(concurrent.lookup(null, "name"), is(Optional.empty()));
    assertThat(concurrent.lookup(schema, null), is(Optional.empty()));
  }

  @Test
  public void schemaNameLookupMatchesLiteralQuotedAndDottedNames() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final String exactName = "routine.with.dot";
    final Routine routine = new LightProcedure(schema, exactName, "specific");
    final ImmutableNamedObjectIndex<Routine> immutable =
        new ImmutableNamedObjectIndex<>(List.of(routine));
    final ConcurrentNamedObjectIndex<Routine> concurrent = new ConcurrentNamedObjectIndex<>();
    concurrent.add(routine);

    assertThat(immutable.lookup(schema, exactName).orElseThrow(), is(routine));
    assertThat(concurrent.lookup(schema, exactName).orElseThrow(), is(routine));
    assertThat(immutable.lookup(schema, "\"routine.with.dot\""), is(Optional.empty()));
    assertThat(concurrent.lookup(schema, "\"routine.with.dot\""), is(Optional.empty()));
  }

  @Test
  public void schemaNameLookupSupportsNullAndEmptySpecificNameKeyParts() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Routine nullSpecific = new LightProcedure(schema, "routine", null);
    final Routine emptySpecific = new LightProcedure(schema, "routine", "");
    final ConcurrentNamedObjectIndex<Routine> index = new ConcurrentNamedObjectIndex<>();
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
}
