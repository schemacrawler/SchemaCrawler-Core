/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import schemacrawler.namedobject.BuildableIndex;
import schemacrawler.namedobject.ConcurrentDatabaseObjectIndex;
import schemacrawler.namedobject.MapBackedNamedObjectIndex;
import schemacrawler.namedobject.NamedObjectIndex;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.test.utility.crawl.LightProcedure;
import schemacrawler.test.utility.crawl.LightTable;

public class NamedObjectIndexConcurrencyTest {

  @Test
  public void concurrentDatabaseIndexRetainsKeysDuringOverlappingTraversal() throws Exception {
    verifyConcurrentMembership(new ConcurrentDatabaseObjectIndex<Table>());
  }

  @Test
  public void schemaAndOverloadKeyTraversalToleratesConcurrentMembershipChanges() throws Exception {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final ConcurrentDatabaseObjectIndex<Routine> index = new ConcurrentDatabaseObjectIndex<>(true);
    final Routine retained = new LightProcedure(schema, "run", "retained");
    index.add(retained);
    final List<Routine> detached = index.getObjects(schema);
    final CountDownLatch firstTraversal = new CountDownLatch(1);
    final CountDownLatch firstMutation = new CountDownLatch(1);
    final List<Callable<Void>> workers =
        List.of(
            () -> {
              if (!firstTraversal.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Traversal did not start");
              }
              for (int iteration = 0; iteration < 1000; iteration++) {
                final Routine temporary = new LightProcedure(schema, "run", "temporary");
                index.add(temporary);
                index.add(new LightProcedure(schema, "run", "temporary"));
                index.remove(temporary);
                firstMutation.countDown();
              }
              return null;
            },
            () -> {
              index.getObjects(schema);
              firstTraversal.countDown();
              if (!firstMutation.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Mutation did not start");
              }
              for (int iteration = 0; iteration < 1000; iteration++) {
                assertThat(index.hasObjects(schema), is(true));
                index.getObjects(schema).forEach(routine -> assertThat(routine, notNullValue()));
                index
                    .getObjects(schema, "run")
                    .forEach(routine -> assertThat(routine, notNullValue()));
                assertThat(index.lookup(schema, "run").isPresent(), is(true));
              }
              return null;
            });
    runTogether(workers);
    assertThat(index.size(), is(1));
    assertThat(index.lookup(schema, "run").orElseThrow(), is(retained));
    assertThat(detached.size(), is(1));
    assertThat(detached.get(0), is(retained));
  }

  @Test
  public void mutableMapBackedIndexRetainsKeysDuringOverlappingTraversal() throws Exception {
    verifyConcurrentMembership(new MapBackedNamedObjectIndex<Table>());
  }

  private static <I extends NamedObjectIndex<Table> & BuildableIndex<Table>>
      void verifyConcurrentMembership(final I index) throws Exception {
    final CountDownLatch firstWrites = new CountDownLatch(4);
    final CountDownLatch firstTraversal = new CountDownLatch(1);
    final List<Callable<Void>> workers = new ArrayList<>();
    for (int writer = 0; writer < 4; writer++) {
      final int writerId = writer;
      workers.add(
          () -> {
            index.add(new LightTable("writer_%d_item_0".formatted(writerId)));
            firstWrites.countDown();
            if (!firstTraversal.await(10, TimeUnit.SECONDS)) {
              throw new IllegalStateException("Traversal did not start");
            }
            for (int item = 1; item < 25; item++) {
              index.add(new LightTable("writer_%d_item_%d".formatted(writerId, item)));
              index.add(new LightTable("shared"));
              final Table temporary = new LightTable("temporary_" + writerId);
              index.add(temporary);
              index.remove(temporary);
            }
            return null;
          });
    }
    workers.add(
        () -> {
          if (!firstWrites.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Writers did not start");
          }
          index.snapshot();
          firstTraversal.countDown();
          for (int iteration = 0; iteration < 100; iteration++) {
            index.getObjects().forEach(table -> index.lookup(table.key()));
          }
          return null;
        });
    runTogether(workers);
    assertThat(index.size(), is(101));
    for (int writer = 0; writer < 4; writer++) {
      for (int item = 0; item < 25; item++) {
        final Table expected = new LightTable("writer_%d_item_%d".formatted(writer, item));
        assertThat(index.containsKey(expected.key()), is(true));
        assertThat(index.lookup(expected.key()).orElseThrow().getName(), is(expected.getName()));
      }
    }
    final Table shared = new LightTable("shared");
    assertThat(index.remove(shared).isPresent(), is(true));
    assertThat(index.size(), is(100));
    assertThat(index.lookup(shared.key()).isEmpty(), is(true));
  }

  private static void runTogether(final List<Callable<Void>> workers) throws Exception {
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
        result.get(10, TimeUnit.SECONDS);
      }
    } finally {
      start.countDown();
      executor.shutdownNow();
      assertThat(executor.awaitTermination(10, TimeUnit.SECONDS), is(true));
    }
  }
}
