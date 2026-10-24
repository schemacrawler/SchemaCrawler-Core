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
import static schemacrawler.crawl.SortTestUtility.newTable;
import static schemacrawler.crawl.SortTestUtility.setSortIndex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import schemacrawler.namedobject.BuildableNamedObjectIndex;
import schemacrawler.namedobject.NamedObjectIndex;
import schemacrawler.namedobject.NamedObjectIndexes;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaReference;

public class NamedObjectIndexSortTest {

  @ParameterizedTest(name = "immutable={0}")
  @ValueSource(booleans = {true, false})
  public void genericNamedObjectsAreSortedAfreshWithoutChangingPriorResults(
      final boolean immutable) {
    final Table first = newTable(new SchemaReference(), "first");
    setSortIndex(first, 1);
    final Table second = newTable(new SchemaReference(), "second");
    setSortIndex(second, 2);
    final NamedObjectIndex<Table> index;
    if (immutable) {
      index = NamedObjectIndexes.newImmutableNamedObjectIndex(List.of(second, first));
    } else {
      final BuildableNamedObjectIndex<Table> mutable =
          NamedObjectIndexes.newBuildableNamedObjectIndex();
      mutable.add(second);
      mutable.add(first);
      index = mutable;
    }
    final List<Table> previous = index.getObjects();
    setSortIndex(second, 0);
    assertThat(previous, contains(first, second));
    assertThat(index.getObjects(), contains(second, first));
    assertThat(index.toString(), is(second + ", " + first));
    assertThat(index.lookup(first.key()).orElseThrow(), sameInstance(first));
  }

  @RepeatedTest(100)
  public void sameFullNameSchemasRetainStructuredOrderAndLookupsAfterShuffling(
      final RepetitionInfo repetition) {
    final String schemaName = "SCHEMA";
    final Schema schema1 = new SchemaReference("", schemaName);
    final Schema schema2 = new SchemaReference(null, schemaName);
    final Schema schema3 = new SchemaReference(schemaName, null);

    assertThat(schema1.getFullName(), is(schemaName));
    assertThat(schema2.getFullName(), is(schemaName));
    assertThat(schema3.getFullName(), is(schemaName));

    final List<Schema> shuffled = new ArrayList<>(List.of(schema1, schema2, schema3));
    Collections.shuffle(shuffled, new Random(repetition.getCurrentRepetition()));
    final BuildableNamedObjectIndex<Schema> mutable = NamedObjectIndexes.newBuildableSchemaIndex();
    for (final Schema schema : shuffled) {
      assertThat(mutable.add(schema), is(true));
    }
    final NamedObjectIndex<Schema> immutable = NamedObjectIndexes.newImmutableSchemaIndex(shuffled);

    for (final NamedObjectIndex<Schema> index : List.of(mutable, immutable)) {
      assertThat(index.isEmpty(), is(false));
      assertThat(index.size(), is(3));
      assertThat(
          index.getObjects(),
          contains(sameInstance(schema1), sameInstance(schema3), sameInstance(schema2)));
      assertThat(index.snapshot(), containsInAnyOrder(schema1, schema2, schema3));
      final List<Schema> iterated = new ArrayList<>();
      index.getObjects().forEach(iterated::add);
      assertThat(iterated, contains(schema1, schema3, schema2));
      for (final Schema schema : shuffled) {
        assertThat(index.containsKey(schema.key()), is(true));
        assertThat(index.lookup(schema.key()).orElseThrow(), sameInstance(schema));
      }
      assertThat(
          index.lookup(new NamedObjectKey(null, schemaName)).orElseThrow(), sameInstance(schema2));
      assertThat(index.lookup(schemaName).orElseThrow(), sameInstance(schema1));
      assertThat(index.toString(), is("SCHEMA, SCHEMA, SCHEMA"));
    }
  }
}
