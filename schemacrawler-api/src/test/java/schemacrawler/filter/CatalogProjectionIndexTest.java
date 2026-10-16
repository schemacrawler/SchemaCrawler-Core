/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.DatabaseUser;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.test.utility.crawl.LightProcedure;
import schemacrawler.test.utility.crawl.LightTable;

public class CatalogProjectionIndexTest {

  private static final class KeyedLightTable extends LightTable {

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

  private static final class OrderedLightTable extends LightTable {

    @Serial private static final long serialVersionUID = 1L;

    private int order;

    OrderedLightTable(final Schema schema, final String name, final int order) {
      super(schema, name);
      this.order = order;
    }

    @Override
    public int compareTo(final NamedObject other) {
      return Integer.compare(order, ((OrderedLightTable) other).order);
    }
  }

  @Test
  public void completeKeysDeduplicateFirstWinsAndRetainSameNameObjects() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final NamedObjectKey firstKey = schema.key().with("shared").with("first");
    final KeyedLightTable first = new KeyedLightTable(schema, "shared", firstKey);
    final KeyedLightTable duplicate = new KeyedLightTable(schema, "shared", firstKey);
    final KeyedLightTable distinct =
        new KeyedLightTable(schema, "shared", schema.key().with("shared").with("second"));
    final CatalogProjection projection =
        projection(List.of(schema), List.of(first, duplicate, distinct), List.of());

    final Collection<Table> tables = projection.getTables();
    assertThat(tables.size(), is(2));
    assertThat(tables.stream().anyMatch(table -> table == first), is(true));
    assertThat(tables.stream().anyMatch(table -> table == duplicate), is(false));
    assertThat(tables.stream().anyMatch(table -> table == distinct), is(true));
    assertThat(projection.lookupTable(schema, "shared").orElseThrow(), sameInstance(first));
  }

  @Test
  public void schemaBoundIndexesExcludeSchemaLessObjects() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Table table = schemaLess(Table.class, "table");
    final Sequence sequence = schemaLess(Sequence.class, "sequence");
    final Synonym synonym = schemaLess(Synonym.class, "synonym");
    final ColumnDataType dataType = schemaLess(ColumnDataType.class, "data_type");
    final CatalogProjection projection =
        projection(
            List.of(schema),
            List.of(table),
            List.of(),
            List.of(sequence),
            List.of(synonym),
            List.of(dataType),
            List.of());

    assertThat(projection.getTables(), is(empty()));
    assertThat(projection.getSequences(), is(empty()));
    assertThat(projection.getSynonyms(), is(empty()));
    assertThat(projection.getColumnDataTypes(), is(empty()));
    assertThat(projection.lookupTable(schema, "table").isEmpty(), is(true));
    assertThat(projection.lookupSequence(schema, "sequence").isEmpty(), is(true));
    assertThat(projection.lookupSynonym(schema, "synonym").isEmpty(), is(true));
    assertThat(projection.lookupColumnDataType(schema, "data_type").isEmpty(), is(true));
  }

  @Test
  public void enumerationsUseFreshNaturalOrderAfterFilteringBySchema() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final OrderedLightTable first = new OrderedLightTable(schema, "first", 1);
    final OrderedLightTable second = new OrderedLightTable(schema, "second", 2);
    final CatalogProjection projection =
        projection(List.of(schema), List.of(first, second), List.of());
    final Collection<Table> previousValues = projection.getTables();

    second.order = 0;

    assertThat(previousValues, contains(first, second));
    assertThat(projection.getTables(), contains(second, first));
    assertThat(projection.getTables(schema), contains(second, first));
  }

  @Test
  public void schemaIdentityUsesStructuredKeysAndFullNameLookupKeepsFirstMatch() {
    final String fullName = "CATALOG.\"schema.with.dot\"";
    final Schema first = schema(fullName, "first", new NamedObjectKey("CATALOG", "first"));
    final Schema second = schema(fullName, "second", new NamedObjectKey("CATALOG", "second"));
    final CatalogProjection projection = projection(List.of(first, second), List.of(), List.of());

    assertThat(projection.getSchemas(), contains(first, second));
    assertThat(projection.lookupSchema(fullName).orElseThrow(), sameInstance(first));
    assertThat(projection.lookupSchema("CATALOG.schema.with.dot").isEmpty(), is(true));
    assertThat(projection.lookupSchema(null).isEmpty(), is(true));
  }

  @Test
  public void routineLookupsPreserveOverloadsAndEncounterOrderForBlankNames() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Routine first = routine(schema, "run", "z");
    final Routine second = routine(schema, "run", "a");
    final CatalogProjection projection =
        projection(List.of(schema), List.of(), List.of(first, second));

    assertThat(projection.getRoutines(schema).size(), is(2));
    assertThat(projection.getRoutines(schema, "run").size(), is(2));
    assertThat(projection.lookupRoutine(schema, "run").orElseThrow(), sameInstance(first));
    assertThat(projection.lookupRoutine(schema, null).orElseThrow(), sameInstance(first));
    assertThat(projection.lookupRoutine(schema, " ").orElseThrow(), sameInstance(first));
    assertThat(projection.lookupRoutine(schema, "specific-z").isEmpty(), is(true));
    assertThat(projection.lookupRoutine(null, "run").isEmpty(), is(true));
  }

  @Test
  public void projectionSerializationRetainsIndexLookupsAndImmutability() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final LightTable table = new LightTable(schema, "table");
    final LightProcedure routine = new LightProcedure(schema, "routine");
    final CatalogProjection projection =
        projection(List.of(schema), List.of(table), List.of(routine));
    final CatalogProjection clonedProjection = SerializationUtils.clone(projection);
    final Schema clonedSchema = clonedProjection.lookupSchema(schema.getFullName()).orElseThrow();

    assertThat(clonedProjection.lookupTable(clonedSchema, "table").isPresent(), is(true));
    assertThat(clonedProjection.lookupRoutine(clonedSchema, "routine").isPresent(), is(true));
    assertThat(clonedProjection.getTables().size(), is(1));
    assertThrows(UnsupportedOperationException.class, () -> clonedProjection.getTables().clear());
  }

  private static <N extends DatabaseObject> N schemaLess(
      final Class<N> objectClass, final String name) {
    final N object = mock(objectClass);
    when(object.getSchema()).thenReturn(null);
    when(object.getName()).thenReturn(name);
    when(object.key()).thenReturn(new NamedObjectKey("schema-less", name));
    return object;
  }

  private static Schema schema(final String fullName, final String name, final NamedObjectKey key) {
    final Schema schema = mock(Schema.class);
    when(schema.getFullName()).thenReturn(fullName);
    when(schema.getName()).thenReturn(name);
    when(schema.key()).thenReturn(key);
    when(schema.compareTo(any(NamedObject.class))).thenReturn(0);
    return schema;
  }

  private static Routine routine(
      final Schema schema, final String name, final String specificName) {
    final Routine routine = mock(Routine.class);
    final NamedObjectKey key = schema.key().with(name).with(specificName);
    when(routine.getSchema()).thenReturn(schema);
    when(routine.getName()).thenReturn(name);
    when(routine.getFullName()).thenReturn(schema.getFullName() + "." + name);
    when(routine.key()).thenReturn(key);
    when(routine.compareTo(any(NamedObject.class)))
        .thenAnswer(invocation -> key.compareTo(((NamedObject) invocation.getArgument(0)).key()));
    return routine;
  }

  private static CatalogProjection projection(
      final Collection<Schema> schemas,
      final Collection<Table> tables,
      final Collection<Routine> routines) {
    return projection(schemas, tables, routines, List.of(), List.of(), List.of(), List.of());
  }

  private static CatalogProjection projection(
      final Collection<Schema> schemas,
      final Collection<Table> tables,
      final Collection<Routine> routines,
      final Collection<Sequence> sequences,
      final Collection<Synonym> synonyms,
      final Collection<ColumnDataType> columnDataTypes,
      final Collection<DatabaseUser> databaseUsers) {
    return new CatalogProjection(
        null,
        null,
        null,
        Map.of(),
        schemas,
        tables,
        routines,
        sequences,
        synonyms,
        columnDataTypes,
        databaseUsers);
  }
}
