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
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import schemacrawler.schema.Column;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.test.utility.crawl.LightProcedure;
import schemacrawler.test.utility.crawl.LightTable;

public class DatabaseObjectIndexTest {

  @Test
  public void concurrentMutationReplacesAndRemovesTheActualMappedObject() {
    final ConcurrentDatabaseObjectIndex<Table> index = new ConcurrentDatabaseObjectIndex<>();
    final Table first = new LightTable("TABLE");
    final Table replacement = new LightTable("TABLE");
    assertThat(index.add(null), is(false));
    assertThat(index.remove(null), is(Optional.empty()));
    assertThat(index.add(first), is(true));
    final List<Table> previous = index.getObjects();
    assertThat(index.add(replacement), is(true));
    assertThat(index.size(), is(1));
    assertThat(index.lookup(first.key()).orElseThrow(), sameInstance(replacement));
    assertThat(index.remove(first).orElseThrow(), sameInstance(replacement));
    assertThat(index.remove(first), is(Optional.empty()));
    assertThat(index.isEmpty(), is(true));
    assertThat(previous, contains(sameInstance(first)));
  }

  @ParameterizedTest(name = "immutable={0}")
  @ValueSource(booleans = {true, false})
  public void fullNameLookupDoesNotCompareUnrelatedOrSingleMatchingObjects(
      final boolean immutable) {
    for (final String fullName : List.of("PUBLIC.TABLE", "\"PUBLIC\".\"TABLE.WITH.DOT\"")) {
      final Table matching = mock(Table.class);
      final Table unrelated = mock(Table.class);
      when(matching.key()).thenReturn(new NamedObjectKey("matching"));
      when(unrelated.key()).thenReturn(new NamedObjectKey("unrelated"));
      when(matching.getFullName()).thenReturn(fullName);
      when(unrelated.getFullName()).thenReturn("unrelated");
      when(matching.compareTo(any(NamedObject.class)))
          .thenThrow(new AssertionError("Single match must not be compared"));
      when(unrelated.compareTo(any(NamedObject.class)))
          .thenThrow(new AssertionError("Unrelated object must not be compared"));
      final DatabaseObjectIndex<Table> index = index(immutable, List.of(unrelated, matching));

      assertThat(index.lookup(fullName).orElseThrow(), sameInstance(matching));
      assertThat(index.lookup("missing"), is(Optional.empty()));
      assertThat(index.lookup(fullName.toLowerCase()), is(Optional.empty()));
      for (final String blank : Arrays.asList(null, "", " ", "\t")) {
        assertThat(index.lookup(blank), is(Optional.empty()));
      }
      verify(matching, never()).compareTo(any(NamedObject.class));
      verify(unrelated, never()).compareTo(any(NamedObject.class));
    }
  }

  @ParameterizedTest(name = "immutable={0}")
  @ValueSource(booleans = {true, false})
  public void fullNameLookupPropagatesMatchingComparisonFailures(final boolean immutable) {
    final Table first = mock(Table.class);
    final Table second = mock(Table.class);
    final AssertionError failure = new AssertionError("Matching objects cannot be compared");
    when(first.key()).thenReturn(new NamedObjectKey("first"));
    when(second.key()).thenReturn(new NamedObjectKey("second"));
    for (final Table object : List.of(first, second)) {
      when(object.getFullName()).thenReturn("same");
      when(object.compareTo(any(NamedObject.class))).thenThrow(failure);
    }
    final DatabaseObjectIndex<Table> index = index(immutable, List.of(second, first));
    assertThat(
        assertThrows(AssertionError.class, () -> index.lookup("same")), sameInstance(failure));
  }

  @Test
  public void dependentLookupAppendsParentKeysAndHandlesMissingArguments() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final LightTable table = new LightTable(schema, "TABLE");
    final Table otherParent = new LightTable(schema, "OTHER");
    final Column column = table.addColumn("COLUMN");
    final ConcurrentDependantObjectIndex<Column, Table> mutable =
        new ConcurrentDependantObjectIndex<>();
    mutable.add(column);
    final DependantObjectIndex<Column, Table> index = mutable;

    assertThat(index.lookup(table, "COLUMN").orElseThrow(), sameInstance(column));
    assertThat(index.lookup(otherParent, "COLUMN"), is(Optional.empty()));
    assertThat(index.lookup(table, "missing"), is(Optional.empty()));
    assertThat(index.lookup((Table) null, "COLUMN"), is(Optional.empty()));
    assertThat(index.lookup(table, null), is(Optional.empty()));
    assertThat(index.lookup(schema, "COLUMN"), is(Optional.empty()));
    assertThat(index.lookup(schema, "TABLE"), is(Optional.empty()));
    assertThat(index.getObjects(schema, "TABLE"), is(empty()));
    for (final String blankName : List.of("", " ", "\t")) {
      final Column blank = mock(Column.class);
      when(blank.key()).thenReturn(table.key().with(blankName));
      mutable.add(blank);
      assertThat(index.lookup(table, blankName), is(Optional.empty()));
      assertThat(index.lookup(schema, blankName), is(Optional.empty()));
      assertThat(index.lookup(blank.key()).orElseThrow(), sameInstance(blank));
    }
  }

  @Test
  public void immutableConstructionCopiesMembershipAndKeepsFirstDuplicate() {
    final Table first = new LightTable("TABLE");
    final Table duplicate = new LightTable("TABLE");
    final List<Table> source = new ArrayList<>(List.of(first, duplicate));
    final ImmutableDatabaseObjectIndex<Table> index = new ImmutableDatabaseObjectIndex<>(source);
    source.clear();
    assertThat(index.size(), is(1));
    assertThat(index.lookup(first.key()).orElseThrow(), sameInstance(first));
    assertThrows(NullPointerException.class, () -> new ImmutableDatabaseObjectIndex<Table>(null));
    assertThrows(
        NullPointerException.class,
        () -> new ImmutableDatabaseObjectIndex<>(Arrays.asList((Table) null)));
  }

  @ParameterizedTest(name = "immutable={0}")
  @ValueSource(booleans = {true, false})
  public void nullAndMissingQueriesHaveEmptyResults(final boolean immutable) {
    final DatabaseObjectIndex<Table> index = index(immutable, List.of());
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    assertThat(index.isEmpty(), is(true));
    assertThat(index.size(), is(0));
    assertThat(index.getObjects(), is(empty()));
    assertThat(index.snapshot(), is(empty()));
    assertThat(index.toString(), is(""));
    assertThat(index.containsKey(null), is(false));
    assertThat(index.containsKey(schema.key()), is(false));
    assertThat(index.lookup((NamedObjectKey) null), is(Optional.empty()));
    assertThat(index.lookup(schema.key()), is(Optional.empty()));
    assertThat(index.lookup(schema, "missing"), is(Optional.empty()));
    assertThat(index.lookup((Schema) null, "name"), is(Optional.empty()));
    assertThat(index.lookup(schema, null), is(Optional.empty()));
    assertThat(index.getObjects(null), is(empty()));
    assertThat(index.getObjects(schema, "missing"), is(empty()));
    assertThat(index.getObjects(null, "name"), is(empty()));
    assertThat(index.getObjects(schema, null), is(empty()));
    assertThat(index.lookup((String) null), is(Optional.empty()));
    assertThat(index.lookup("missing"), is(Optional.empty()));
  }

  @ParameterizedTest(name = "immutable={0}")
  @ValueSource(booleans = {true, false})
  public void nullStoredKeysAreRejected(final boolean immutable) {
    final Table withoutKey = mock(Table.class);
    assertThrows(NullPointerException.class, () -> index(immutable, List.of(withoutKey)));
    if (!immutable) {
      assertThrows(
          NullPointerException.class,
          () -> new ConcurrentDatabaseObjectIndex<Table>().remove(withoutKey));
    }
  }

  @ParameterizedTest(name = "immutable={0}")
  @ValueSource(booleans = {true, false})
  public void schemaFilteringAndSnapshotsDoNotCompareExcludedObjects(final boolean immutable) {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Schema otherSchema = new SchemaReference("CATALOG", "OTHER");
    final Table matching = new LightTable(schema, "TABLE");
    final Table excluded = mock(Table.class);
    when(excluded.key()).thenReturn(otherSchema.key().with("TABLE"));
    when(excluded.getSchema()).thenThrow(new AssertionError("Excluded schema was accessed"));
    when(excluded.getName()).thenReturn("TABLE");
    when(excluded.compareTo(any(NamedObject.class)))
        .thenThrow(new AssertionError("Excluded object was compared"));
    final DatabaseObjectIndex<Table> index = index(immutable, List.of(excluded, matching));

    assertThat(index.snapshot(), containsInAnyOrder(matching, excluded));
    assertThat(
        index.getObjects(new SchemaReference("CATALOG", "PUBLIC")),
        contains(sameInstance(matching)));
    assertThat(index.lookup(schema, "TABLE").orElseThrow(), sameInstance(matching));
    assertThat(index.lookup(otherSchema, "TABLE").orElseThrow(), sameInstance(excluded));
    assertThat(index.hasObjects(schema), is(true));
    assertThat(index.hasObjects(otherSchema), is(true));
    assertThat(index.hasObjects(null), is(false));
    assertThat(index.hasObjects(new SchemaReference("CATALOG", "MISSING")), is(false));
    verify(excluded, never()).getSchema();
    verify(excluded, never()).getName();
    final List<Table> scoped = index.getObjects(schema);
    assertThrows(UnsupportedOperationException.class, scoped::clear);
  }

  @ParameterizedTest(name = "immutable={0}")
  @ValueSource(booleans = {true, false})
  public void ordinaryNamesMatchLiterallyIncludingQuotedAndDottedNames(final boolean immutable) {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final List<Table> tables = new ArrayList<>();
    for (final String name : List.of("routine.with.dot", "\"quoted\"", " name ")) {
      final Table table = mock(Table.class);
      when(table.key()).thenReturn(schema.key().with(name));
      when(table.getName()).thenReturn(name);
      when(table.getSchema()).thenReturn(schema);
      tables.add(table);
    }
    final DatabaseObjectIndex<Table> index = index(immutable, tables);
    for (final Table table : tables) {
      assertThat(index.lookup(schema, table.getName()).orElseThrow(), sameInstance(table));
    }
    assertThat(index.lookup(schema, "\"routine.with.dot\""), is(Optional.empty()));
    assertThat(index.lookup(schema, "quoted"), is(Optional.empty()));
    assertThat(index.lookup(schema, "ROUTINE.WITH.DOT"), is(Optional.empty()));
  }

  @ParameterizedTest(name = "immutable={0}")
  @ValueSource(booleans = {true, false})
  public void schemaNameLookupRejectsBlankNamesButRetainsExactKeys(final boolean immutable) {
    final Schema schema = new SchemaReference();
    final Table blank = mock(Table.class);
    when(blank.key()).thenReturn(schema.key().with(""));
    when(blank.getSchema()).thenReturn(schema);
    when(blank.getName()).thenReturn("");
    when(blank.getFullName()).thenReturn("");
    final DatabaseObjectIndex<Table> index = index(immutable, List.of(blank));
    for (final String name : Arrays.asList(null, "", " ", "\t")) {
      assertThat(index.lookup(schema, name), is(Optional.empty()));
      assertThat(index.getObjects(schema, name), is(empty()));
    }
    assertThat(index.lookup(blank.key()).orElseThrow(), sameInstance(blank));
    assertThat(index.lookup(""), is(Optional.empty()));
  }

  @ParameterizedTest(name = "immutable={0}")
  @ValueSource(booleans = {true, false})
  public void schemaReadsDistinguishAbsentAndEmptyCatalogComponents(final boolean immutable) {
    final Schema absentCatalog = new SchemaReference(null, "APP");
    final Schema emptyCatalog = new SchemaReference("", "APP");
    final Table first = new LightTable(absentCatalog, "TABLE");
    final Table second = new LightTable(emptyCatalog, "TABLE");
    final DatabaseObjectIndex<Table> index = index(immutable, List.of(second, first));

    assertThat(absentCatalog.getFullName(), is(emptyCatalog.getFullName()));
    assertThat(index.getObjects(absentCatalog), contains(sameInstance(first)));
    assertThat(index.getObjects(emptyCatalog), contains(sameInstance(second)));
    assertThat(index.hasObjects(absentCatalog), is(true));
    assertThat(index.hasObjects(emptyCatalog), is(true));
    assertThat(index.lookup(absentCatalog, "TABLE").orElseThrow(), sameInstance(first));
    assertThat(index.lookup(emptyCatalog, "TABLE").orElseThrow(), sameInstance(second));
  }

  @ParameterizedTest(name = "immutable={0}")
  @ValueSource(booleans = {true, false})
  public void ordinaryLookupDoesNotTraverseStoredKeys(final boolean immutable) {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Table matching = new LightTable(schema, "TABLE");
    final Table unrelated = mock(Table.class);
    final NamedObjectKey unrelatedKey = mock(NamedObjectKey.class);
    when(unrelated.key()).thenReturn(unrelatedKey);
    when(unrelatedKey.startsWith(any(NamedObjectKey.class)))
        .thenThrow(new AssertionError("Ordinary lookup scanned membership"));
    final DatabaseObjectIndex<Table> index = index(immutable, List.of(unrelated, matching));

    assertThat(index.lookup(schema, "TABLE").orElseThrow(), sameInstance(matching));
    assertThat(index.getObjects(schema, "TABLE"), contains(sameInstance(matching)));
    assertThat(index.lookup(schema, "missing"), is(Optional.empty()));
    verify(unrelatedKey, never()).startsWith(any(NamedObjectKey.class));
    verify(unrelated, never()).getSchema();
    verify(unrelated, never()).getName();
  }

  @ParameterizedTest(name = "immutable={0}")
  @ValueSource(booleans = {true, false})
  public void overloadLookupComparesOnlyActualCandidates(final boolean immutable) {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Routine matching = mock(Routine.class);
    final Routine unrelated = mock(Routine.class);
    when(matching.key()).thenReturn(schema.key().with("run").with("specific"));
    when(unrelated.key()).thenReturn(schema.key().with("other").with("specific"));
    for (final Routine routine : List.of(matching, unrelated)) {
      when(routine.getSchema()).thenThrow(new AssertionError("Schema accessor was used"));
      when(routine.getName()).thenThrow(new AssertionError("Name accessor was used"));
      when(routine.compareTo(any(NamedObject.class)))
          .thenThrow(new AssertionError("Single or unrelated candidate was compared"));
    }
    final DatabaseObjectIndex<Routine> index = index(immutable, List.of(unrelated, matching), true);

    assertThat(index.lookup(schema, "run").orElseThrow(), sameInstance(matching));
    assertThat(index.getObjects(schema, "run"), contains(sameInstance(matching)));
    assertThat(index.lookup(schema, "missing"), is(Optional.empty()));
    assertThat(index.getObjects(schema, "missing"), is(empty()));
    assertThat(index.getObjects(schema, "RUN"), is(empty()));
    verify(matching, never()).compareTo(any(NamedObject.class));
    verify(unrelated, never()).compareTo(any(NamedObject.class));
  }

  @ParameterizedTest(name = "immutable={0}")
  @ValueSource(booleans = {true, false})
  public void overloadModePreservesBlankQueryGuardsAndEmptyMembership(final boolean immutable) {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final DatabaseObjectIndex<Routine> empty = index(immutable, List.of(), true);
    assertThat(empty.lookup(schema, "run"), is(Optional.empty()));
    assertThat(empty.getObjects(schema, "run"), is(empty()));
    assertThat(empty.getObjects(schema), is(empty()));
    assertThat(empty.hasObjects(schema), is(false));
    assertThat(empty.hasObjects(null), is(false));
    assertThat(empty.getObjects(null), is(empty()));
    assertThat(empty.lookup((Schema) null, "run"), is(Optional.empty()));

    for (final String blankName : List.of("", " ", "\t")) {
      final Routine blank = mock(Routine.class);
      when(blank.key()).thenReturn(schema.key().with(blankName).with(null));
      final DatabaseObjectIndex<Routine> index = index(immutable, List.of(blank), true);
      for (final String query : Arrays.asList(null, "", " ", "\t")) {
        assertThat(index.lookup(schema, query), is(Optional.empty()));
        assertThat(index.getObjects(schema, query), is(empty()));
        assertThat(index.lookup(query), is(Optional.empty()));
      }
      assertThat(index.lookup(blank.key()).orElseThrow(), sameInstance(blank));
    }
  }

  @ParameterizedTest(name = "immutable={0}")
  @ValueSource(booleans = {true, false})
  public void overloadSerializationPreservesPolicyAndDuplicateRules(final boolean immutable) {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Routine earlier = new LightProcedure(schema, "run", "a");
    final Routine later = new LightProcedure(schema, "run", "z");
    final Routine duplicate = new LightProcedure(schema, "run", "a");
    final DatabaseObjectIndex<Routine> original =
        index(immutable, List.of(later, earlier, duplicate), true);
    assertThat(
        original.lookup(earlier.key()).orElseThrow(),
        sameInstance(immutable ? earlier : duplicate));
    final DatabaseObjectIndex<Routine> copy = SerializationUtils.clone(original);
    assertThat(copy.size(), is(2));
    assertThat(copy.lookup(schema, "run").orElseThrow().key(), is(earlier.key()));
    assertThat(
        copy.getObjects(schema, "run").stream().map(Routine::key).toList(),
        contains(earlier.key(), later.key()));
    assertThat(copy.lookup(schema.key().with("run")), is(Optional.empty()));
    assertThrows(UnsupportedOperationException.class, () -> copy.getObjects(schema, "run").clear());
  }

  @Test
  public void mutableOverloadSerializationRetainsReplacementAndRemoval() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Routine routine = new LightProcedure(schema, "run", "a");
    final ConcurrentDatabaseObjectIndex<Routine> original =
        new ConcurrentDatabaseObjectIndex<>(true);
    original.add(routine);
    final ConcurrentDatabaseObjectIndex<Routine> copy = SerializationUtils.clone(original);
    final List<Routine> before = copy.getObjects(schema, "run");
    final Routine replacement = new LightProcedure(schema, "run", "a");
    copy.add(replacement);
    assertThat(copy.lookup(schema, "run").orElseThrow(), sameInstance(replacement));
    assertThat(copy.remove(routine).orElseThrow(), sameInstance(replacement));
    assertThat(copy.lookup(schema, "run"), is(Optional.empty()));
    assertThat(before.size(), is(1));
    assertThat(original.lookup(schema, "run").orElseThrow(), sameInstance(routine));
  }

  @Test
  public void concurrentSerializationRetainsMutationAndKeyLookup() {
    final LightTable table = new LightTable("TABLE");
    final DatabaseObject column = table.addColumn("COLUMN");
    final ConcurrentDatabaseObjectIndex<DatabaseObject> original =
        new ConcurrentDatabaseObjectIndex<>();
    original.add(column);
    final ConcurrentDatabaseObjectIndex<DatabaseObject> copy = SerializationUtils.clone(original);
    assertThat(copy.lookup(column.key()).orElseThrow().key(), is(column.key()));
    assertThat(copy.remove(column).isPresent(), is(true));
    assertThat(copy.add(table), is(true));
    assertThat(copy.lookup(table.key()).orElseThrow(), sameInstance(table));
  }

  @Test
  public void dependentSerializationRetainsMutationAndParentLookup() {
    final LightTable table = new LightTable("TABLE");
    final Column column = table.addColumn("COLUMN");
    final ConcurrentDependantObjectIndex<Column, Table> original =
        new ConcurrentDependantObjectIndex<>();
    original.add(column);

    final ConcurrentDependantObjectIndex<Column, Table> copy = SerializationUtils.clone(original);
    final Column copiedColumn = copy.lookup(table, "COLUMN").orElseThrow();
    assertThat(copiedColumn.key(), is(column.key()));
    assertThat(copy.lookup(column.key()).orElseThrow(), sameInstance(copiedColumn));
    assertThat(copy.remove(column).orElseThrow(), sameInstance(copiedColumn));
    assertThat(copy.lookup(table, "COLUMN"), is(Optional.empty()));
    assertThat(copy.add(column), is(true));
    assertThat(copy.lookup(table, "COLUMN").orElseThrow(), sameInstance(column));
    assertThat(original.lookup(table, "COLUMN").orElseThrow(), sameInstance(column));
  }

  private static <D extends DatabaseObject> DatabaseObjectIndex<D> index(
      final boolean immutable, final List<D> values) {
    return index(immutable, values, false);
  }

  private static <D extends DatabaseObject> DatabaseObjectIndex<D> index(
      final boolean immutable, final List<D> values, final boolean supportsOverloads) {
    if (immutable) {
      return new ImmutableDatabaseObjectIndex<>(values, supportsOverloads);
    }
    final ConcurrentDatabaseObjectIndex<D> index =
        new ConcurrentDatabaseObjectIndex<>(supportsOverloads);
    values.forEach(index::add);
    return index;
  }
}
