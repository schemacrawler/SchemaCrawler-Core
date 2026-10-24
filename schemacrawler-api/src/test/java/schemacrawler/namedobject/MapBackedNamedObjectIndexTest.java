/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;
import schemacrawler.schemacrawler.SchemaReference;

public class MapBackedNamedObjectIndexTest {

  @Test
  public void mutationUsesCompleteKeysAndReturnsTheActualRemovedValue() {
    final MapBackedNamedObjectIndex<Schema> index = new MapBackedNamedObjectIndex<>();
    final Schema first = new SchemaReference(null, "SCHEMA");
    final Schema replacement = new SchemaReference(null, "SCHEMA");
    final Schema distinct = new SchemaReference("", "SCHEMA");
    index.add(first);
    index.add(replacement);
    index.add(distinct);
    assertThat(index.remove(first).orElseThrow(), sameInstance(replacement));
    assertThat(index.lookup(first.key()), is(Optional.empty()));
    assertThat(index.remove(first), is(Optional.empty()));
    assertThat(index.lookup(distinct.key()).orElseThrow(), sameInstance(distinct));
    assertThat(index.remove(null), is(Optional.empty()));
  }

  @Test
  public void immutableMembershipRejectsRemovalAndNullEntriesOrKeys() {
    final Schema schema = new SchemaReference("CATALOG", "SCHEMA");
    for (final MapBackedNamedObjectIndex<Schema> index :
        List.of(
            new MapBackedNamedObjectIndex<>(List.of(schema)),
            new MapBackedNamedObjectIndex<Schema>(null),
            new MapBackedNamedObjectIndex<Schema>(List.of()))) {
      assertThrows(UnsupportedOperationException.class, () -> index.remove(schema));
      assertThrows(UnsupportedOperationException.class, () -> index.add(schema));
      assertThat(index.add(null), is(false));
      assertThat(index.remove(null), is(Optional.empty()));
    }
    assertThrows(
        NullPointerException.class,
        () -> new MapBackedNamedObjectIndex<>(Arrays.asList((Schema) null)));
    final Schema withoutKey = mock(Schema.class);
    assertThrows(
        NullPointerException.class, () -> new MapBackedNamedObjectIndex<>(List.of(withoutKey)));
    final MapBackedNamedObjectIndex<Schema> mutable = new MapBackedNamedObjectIndex<>();
    assertThrows(NullPointerException.class, () -> mutable.add(withoutKey));
    assertThrows(NullPointerException.class, () -> mutable.remove(withoutKey));
  }

  @Test
  public void collectionConstructorCopiesMembershipAndRejectsMutation() {
    final Schema first = new SchemaReference("CATALOG", "first");
    final Schema second = new SchemaReference("CATALOG", "second");
    final List<Schema> source = new ArrayList<>(List.of(first));
    final MapBackedNamedObjectIndex<Schema> index = new MapBackedNamedObjectIndex<>(source);
    source.clear();
    source.add(second);

    assertThat(index.getObjects(), contains(sameInstance(first)));
    assertThat(index.containsKey(second.key()), is(false));
    assertThrows(UnsupportedOperationException.class, () -> index.add(second));
    assertThrows(UnsupportedOperationException.class, () -> index.remove(first));
    first.setAttribute("shared", "value");
    assertThat(index.lookup(first.key()).orElseThrow().getAttribute("shared"), is("value"));
  }

  @Test
  public void duplicateKeysReplacePreviousSchemaInBothConstructionModes() {
    final Schema first = new SchemaReference("CATALOG", "SCHEMA");
    final Schema replacement = new SchemaReference("CATALOG", "SCHEMA");
    final MapBackedNamedObjectIndex<Schema> mutable = new MapBackedNamedObjectIndex<>();
    assertThat(mutable.add(first), is(true));
    assertThat(mutable.add(replacement), is(true));
    final MapBackedNamedObjectIndex<Schema> immutable =
        new MapBackedNamedObjectIndex<>(List.of(first, replacement));

    for (final MapBackedNamedObjectIndex<Schema> index : List.of(mutable, immutable)) {
      assertThat(index.size(), is(1));
      assertThat(index.lookup(first.key()).orElseThrow(), sameInstance(replacement));
      assertThat(index.getObjects(), contains(sameInstance(replacement)));
    }
  }

  @Test
  public void emptyIndexesAndMissingKeysReturnEmptyResults() {
    final MapBackedNamedObjectIndex<Schema> mutable = new MapBackedNamedObjectIndex<>();
    assertThat(mutable.add(null), is(false));

    for (final MapBackedNamedObjectIndex<Schema> index :
        List.of(
            mutable,
            new MapBackedNamedObjectIndex<Schema>(List.of()),
            new MapBackedNamedObjectIndex<Schema>(null))) {
      assertThat(index.isEmpty(), is(true));
      assertThat(index.size(), is(0));
      assertThat(index.getObjects(), is(empty()));
      assertThat(index.snapshot(), is(empty()));
      assertThat(index.toString(), is(""));
      assertThat(index.containsKey(null), is(false));
      assertThat(index.containsKey(new NamedObjectKey("missing", "schema")), is(false));
      assertThat(index.lookup((NamedObjectKey) null), is(Optional.empty()));
      assertThat(index.lookup((String) null), is(Optional.empty()));
      assertThat(index.lookup("missing"), is(Optional.empty()));
      assertThat(index.lookup(new NamedObjectKey("missing", "schema")), is(Optional.empty()));
    }
  }

  @Test
  public void enumerationAndIteratorsAreDetachedAndUnmodifiable() {
    final Schema first = new SchemaReference("CATALOG", "first");
    final Schema second = new SchemaReference("CATALOG", "second");
    final MapBackedNamedObjectIndex<Schema> index = new MapBackedNamedObjectIndex<>();
    index.add(first);
    final List<Schema> values = index.getObjects();
    final List<Schema> snapshot = index.snapshot();
    final Iterator<Schema> iterator = index.getObjects().iterator();
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

  @Test
  public void fullNameLookupMatchesBlankSchemaNamesLiterally() {
    final Schema blank = new SchemaReference(null, null);
    final MapBackedNamedObjectIndex<Schema> mutable = new MapBackedNamedObjectIndex<>();
    mutable.add(blank);
    for (final MapBackedNamedObjectIndex<Schema> index :
        List.of(mutable, new MapBackedNamedObjectIndex<>(List.of(blank)))) {
      assertThat(index.lookup("").orElseThrow(), sameInstance(blank));
      assertThat(index.lookup(" "), is(Optional.empty()));
    }
  }

  @Test
  public void serializationPreservesDistinctKeysAndConstructionMode() {
    final Schema first = new SchemaReference(null, "SCHEMA");
    final Schema second = new SchemaReference("", "SCHEMA");
    final Schema third = new SchemaReference("SCHEMA", null);
    first.setAttribute("attribute", "value");
    final List<Schema> schemas = List.of(third, second, first);
    final MapBackedNamedObjectIndex<Schema> mutable = new MapBackedNamedObjectIndex<>();
    schemas.forEach(mutable::add);

    final MapBackedNamedObjectIndex<Schema> mutableCopy = SerializationUtils.clone(mutable);
    final MapBackedNamedObjectIndex<Schema> immutableCopy =
        SerializationUtils.clone(new MapBackedNamedObjectIndex<>(schemas));
    for (final MapBackedNamedObjectIndex<Schema> index : List.of(mutableCopy, immutableCopy)) {
      assertThat(index.getObjects(), contains(second, third, first));
      assertThat(index.size(), is(3));
      for (final Schema schema : schemas) {
        assertThat(index.containsKey(schema.key()), is(true));
        assertThat(index.lookup(schema.key()).orElseThrow(), is(schema));
      }
      assertThat(index.lookup(first.key()).orElseThrow().getAttribute("attribute"), is("value"));
    }
    final Schema additional = new SchemaReference("OTHER", "SCHEMA");
    assertThat(mutableCopy.add(additional), is(true));
    assertThat(mutableCopy.remove(additional).orElseThrow(), sameInstance(additional));
    assertThrows(UnsupportedOperationException.class, () -> immutableCopy.add(additional));
    assertThrows(UnsupportedOperationException.class, () -> immutableCopy.remove(first));
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
    final MapBackedNamedObjectIndex<Schema> mutable = new MapBackedNamedObjectIndex<>();
    mutable.add(first);
    mutable.add(second);

    for (final MapBackedNamedObjectIndex<Schema> index :
        List.of(mutable, new MapBackedNamedObjectIndex<>(List.of(first, second)))) {
      assertThat(index.snapshot(), containsInAnyOrder(sameInstance(first), sameInstance(second)));
    }
  }

  @ParameterizedTest(name = "immutable={0}")
  @ValueSource(booleans = {true, false})
  public void fullNameLookupDoesNotCompareUnrelatedOrSingleMatchingObjects(
      final boolean immutable) {
    for (final String name : List.of("SCHEMA.with.dot", "\"SCHEMA\"", "", " ", "\t")) {
      final NamedObject matching = mock(NamedObject.class);
      final NamedObject unrelated = mock(NamedObject.class);
      when(matching.key()).thenReturn(new NamedObjectKey("matching"));
      when(unrelated.key()).thenReturn(new NamedObjectKey("unrelated"));
      when(matching.getFullName()).thenReturn(name);
      when(unrelated.getFullName()).thenReturn("unrelated");
      when(matching.compareTo(any(NamedObject.class)))
          .thenThrow(new AssertionError("Single match must not be compared"));
      when(unrelated.compareTo(any(NamedObject.class)))
          .thenThrow(new AssertionError("Unrelated object must not be compared"));
      final MapBackedNamedObjectIndex<NamedObject> index;
      if (immutable) {
        index = new MapBackedNamedObjectIndex<>(List.of(unrelated, matching));
      } else {
        index = new MapBackedNamedObjectIndex<>();
        index.add(unrelated);
        index.add(matching);
      }

      assertThat(index.lookup(name).orElseThrow(), sameInstance(matching));
      assertThat(index.lookup("missing"), is(Optional.empty()));
      assertThat(index.lookup(name + "suffix"), is(Optional.empty()));
      if (name.startsWith("SCHEMA")) {
        assertThat(index.lookup("schema.with.dot"), is(Optional.empty()));
      }
      assertThat(index.lookup("prefix" + name), is(Optional.empty()));
      assertThat(index.lookup((String) null), is(Optional.empty()));
      verify(matching, never()).compareTo(any(NamedObject.class));
      verify(unrelated, never()).compareTo(any(NamedObject.class));
    }
  }

  @ParameterizedTest(name = "immutable={0}")
  @ValueSource(booleans = {true, false})
  public void fullNameLookupPropagatesMatchingComparisonFailures(final boolean immutable) {
    final NamedObject first = mock(NamedObject.class);
    final NamedObject second = mock(NamedObject.class);
    final AssertionError failure = new AssertionError("Matching objects cannot be compared");
    when(first.key()).thenReturn(new NamedObjectKey("first"));
    when(second.key()).thenReturn(new NamedObjectKey("second"));
    for (final NamedObject object : List.of(first, second)) {
      when(object.getFullName()).thenReturn("same");
      when(object.compareTo(any(NamedObject.class))).thenThrow(failure);
    }
    final MapBackedNamedObjectIndex<NamedObject> index;
    if (immutable) {
      index = new MapBackedNamedObjectIndex<>(List.of(second, first));
    } else {
      index = new MapBackedNamedObjectIndex<>();
      index.add(second);
      index.add(first);
    }
    assertThat(
        assertThrows(AssertionError.class, () -> index.lookup("same")), sameInstance(failure));
  }
}
