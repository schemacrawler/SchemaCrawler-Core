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
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import schemacrawler.namedobject.SchemaIndex;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;
import schemacrawler.schemacrawler.SchemaReference;

public class SchemaIndexTest {

  @Test
  public void collectionConstructorCopiesMembershipAndRejectsMutation() {
    final Schema first = new SchemaReference("CATALOG", "first");
    final Schema second = new SchemaReference("CATALOG", "second");
    final List<Schema> source = new ArrayList<>(List.of(first));
    final SchemaIndex index = new SchemaIndex(source);
    source.clear();
    source.add(second);

    assertThat(index.getObjects(), contains(sameInstance(first)));
    assertThat(index.containsKey(second.key()), is(false));
    assertThrows(UnsupportedOperationException.class, () -> index.add(second));
    first.setAttribute("shared", "value");
    assertThat(index.lookup(first.key()).orElseThrow().getAttribute("shared"), is("value"));
  }

  @Test
  public void duplicateKeysReplacePreviousSchemaInBothConstructionModes() {
    final Schema first = new SchemaReference("CATALOG", "SCHEMA");
    final Schema replacement = new SchemaReference("CATALOG", "SCHEMA");
    final SchemaIndex mutable = new SchemaIndex();
    assertThat(mutable.add(first), is(true));
    assertThat(mutable.add(replacement), is(true));
    final SchemaIndex immutable = new SchemaIndex(List.of(first, replacement));

    for (final SchemaIndex index : List.of(mutable, immutable)) {
      assertThat(index.size(), is(1));
      assertThat(index.lookup(first.key()).orElseThrow(), sameInstance(replacement));
      assertThat(index.getObjects(), contains(sameInstance(replacement)));
    }
  }

  @Test
  public void emptyIndexesAndMissingKeysReturnEmptyResults() {
    final SchemaIndex mutable = new SchemaIndex();
    assertThat(mutable.add(null), is(false));

    for (final SchemaIndex index :
        List.of(mutable, new SchemaIndex(List.of()), new SchemaIndex(null))) {
      assertThat(index.isEmpty(), is(true));
      assertThat(index.size(), is(0));
      assertThat(index.getObjects(), is(empty()));
      assertThat(index.snapshot(), is(empty()));
      assertThat(index.iterator().hasNext(), is(false));
      assertThat(index.toString(), is(""));
      assertThat(index.containsKey(null), is(false));
      assertThat(index.containsKey(new NamedObjectKey("missing", "schema")), is(false));
      assertThat(index.lookup((NamedObjectKey) null), is(Optional.empty()));
      assertThat(index.lookup(new NamedObjectKey("missing", "schema")), is(Optional.empty()));
    }
  }

  @Test
  public void enumerationAndIteratorsAreDetachedAndUnmodifiable() {
    final Schema first = new SchemaReference("CATALOG", "first");
    final Schema second = new SchemaReference("CATALOG", "second");
    final SchemaIndex index = new SchemaIndex();
    index.add(first);
    final List<Schema> values = index.getObjects();
    final List<Schema> snapshot = index.snapshot();
    final Iterator<Schema> iterator = index.iterator();
    index.add(second);

    assertThat(values, contains(sameInstance(first)));
    assertThat(snapshot, contains(sameInstance(first)));
    assertThat(iterator.next(), sameInstance(first));
    assertThat(iterator.hasNext(), is(false));
    assertThrows(UnsupportedOperationException.class, iterator::remove);
    assertThrows(UnsupportedOperationException.class, () -> values.add(second));
    assertThrows(UnsupportedOperationException.class, snapshot::clear);
    assertThat(index.getObjects(), contains(first, second));
    assertThat(index.toString(), is("CATALOG.first, CATALOG.\"second\""));
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
    final SchemaIndex mutable = new SchemaIndex();
    for (final Schema schema : shuffled) {
      assertThat(mutable.add(schema), is(true));
    }
    final SchemaIndex immutable = new SchemaIndex(shuffled);

    for (final SchemaIndex index : List.of(mutable, immutable)) {
      assertThat(index.isEmpty(), is(false));
      assertThat(index.size(), is(3));
      assertThat(
          index.getObjects(),
          contains(sameInstance(schema2), sameInstance(schema1), sameInstance(schema3)));
      assertThat(index.snapshot(), containsInAnyOrder(schema1, schema2, schema3));
      final List<Schema> iterated = new ArrayList<>();
      index.forEach(iterated::add);
      assertThat(iterated, contains(schema2, schema1, schema3));
      for (final Schema schema : shuffled) {
        assertThat(index.containsKey(schema.key()), is(true));
        assertThat(index.lookup(schema.key()).orElseThrow(), sameInstance(schema));
      }
      assertThat(
          index.lookup(new NamedObjectKey(null, schemaName)).orElseThrow(), sameInstance(schema2));
      assertThat(index.toString(), is("SCHEMA, SCHEMA, SCHEMA"));
    }
  }

  @Test
  public void serializationPreservesDistinctKeysAndConstructionMode() {
    final Schema first = new SchemaReference(null, "SCHEMA");
    final Schema second = new SchemaReference("", "SCHEMA");
    final Schema third = new SchemaReference("SCHEMA", null);
    first.setAttribute("attribute", "value");
    final List<Schema> schemas = List.of(third, second, first);
    final SchemaIndex mutable = new SchemaIndex();
    schemas.forEach(mutable::add);

    final SchemaIndex mutableCopy = SerializationUtils.clone(mutable);
    final SchemaIndex immutableCopy = SerializationUtils.clone(new SchemaIndex(schemas));
    for (final SchemaIndex index : List.of(mutableCopy, immutableCopy)) {
      assertThat(index.getObjects(), contains(first, second, third));
      assertThat(index.size(), is(3));
      for (final Schema schema : schemas) {
        assertThat(index.containsKey(schema.key()), is(true));
        assertThat(index.lookup(schema.key()).orElseThrow(), is(schema));
      }
      assertThat(index.lookup(first.key()).orElseThrow().getAttribute("attribute"), is("value"));
    }
    final Schema additional = new SchemaReference("OTHER", "SCHEMA");
    assertThat(mutableCopy.add(additional), is(true));
    assertThrows(UnsupportedOperationException.class, () -> immutableCopy.add(additional));
  }

  @Test
  public void snapshotsDoNotCompareSchemas() {
    final Schema first = mock(Schema.class);
    final Schema second = mock(Schema.class);
    when(first.key()).thenReturn(new NamedObjectKey("CATALOG", "first"));
    when(second.key()).thenReturn(new NamedObjectKey("CATALOG", "second"));
    when(first.compareTo(any(NamedObject.class)))
        .thenThrow(new AssertionError("Snapshot must not compare schemas"));
    when(second.compareTo(any(NamedObject.class)))
        .thenThrow(new AssertionError("Snapshot must not compare schemas"));
    final SchemaIndex mutable = new SchemaIndex();
    mutable.add(first);
    mutable.add(second);

    for (final SchemaIndex index : List.of(mutable, new SchemaIndex(List.of(first, second)))) {
      assertThat(index.snapshot(), containsInAnyOrder(sameInstance(first), sameInstance(second)));
    }
  }
}
