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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static schemacrawler.crawl.SortTestUtility.newProcedure;
import static schemacrawler.crawl.SortTestUtility.newTable;
import static schemacrawler.crawl.SortTestUtility.setSortIndex;
import static schemacrawler.crawl.SortTestUtility.setTableType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import schemacrawler.filter.CatalogProjectionBuilder;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableType;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaReference;

public class CatalogProjectionSortTest {

  @Test
  public void enumerationsUseFreshNaturalOrderAfterFilteringBySchema() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Table first = newTable(schema, "first");
    setTableType(first, new TableType("TABLE"));
    setSortIndex(first, 1);
    final Table second = newTable(schema, "second");
    setTableType(second, new TableType("TABLE"));
    setSortIndex(second, 2);
    final Catalog projection = projection(List.of(schema), List.of(first, second), List.of());
    final Collection<Table> previousValues = projection.getTables();

    setSortIndex(second, 0);

    assertThat(previousValues, contains(first, second));
    assertThat(projection.getTables(), contains(second, first));
    assertThat(projection.getTables(schema), contains(second, first));
  }

  @RepeatedTest(100)
  public void ambiguousSchemaFullNameSelectsMinimumStructuredKey(final RepetitionInfo repetition) {
    final String schemaName = "SCHEMA";
    final Schema schema1 = new SchemaReference("", schemaName);
    final Schema schema2 = new SchemaReference(null, schemaName);
    final Schema schema3 = new SchemaReference(schemaName, null);

    assertThat(schema1.getFullName(), is(schemaName));
    assertThat(schema2.getFullName(), is(schemaName));
    assertThat(schema3.getFullName(), is(schemaName));

    final List<Schema> shuffled = new ArrayList<>(List.of(schema1, schema2, schema3));
    Collections.shuffle(shuffled, new Random(repetition.getCurrentRepetition()));
    final Catalog projection = projection(shuffled, List.of(), List.of());

    final Collection<Schema> schemas = projection.getSchemas();
    assertThat(schemas.size(), is(3));
    assertThat(schemas, is(List.of(schema1, schema3, schema2)));
    assertThat(projection.lookupSchema(schemaName).orElseThrow(), sameInstance(schema1));
  }

  @Test
  public void singularRoutineLookupSelectsNaturalFirstOverload() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Routine later = newProcedure(schema, "run", "z");
    final Routine earlier = newProcedure(schema, "run", "a");
    final Catalog projection = projection(List.of(schema), List.of(), List.of(later, earlier));

    assertThat(projection.lookupRoutine(schema, "run").orElseThrow(), sameInstance(earlier));
  }

  @Test
  public void collectionsFollowNaturalNamedObjectOrder() {
    final SchemaReference schema = new SchemaReference("CATALOG", "PUBLIC");
    final Table naturallyEarlierTable = newTable(schema, "Beta");
    final Table naturallyLaterTable = newTable(schema, "alpha");
    setTableType(naturallyEarlierTable, new TableType("TABLE"));
    setTableType(naturallyLaterTable, new TableType("TABLE"));
    setSortIndex(naturallyEarlierTable, 1);
    setSortIndex(naturallyLaterTable, 2);

    final Catalog projection =
        projection(List.of(schema), List.of(naturallyLaterTable, naturallyEarlierTable), List.of());

    assertThat(projection.getTables(), contains(naturallyEarlierTable, naturallyLaterTable));
  }

  private static Catalog projection(
      final Collection<Schema> schemas,
      final Collection<Table> tables,
      final Collection<Routine> routines) {
    final Catalog source = mock(Catalog.class);
    when(source.getSchemas()).thenReturn(schemas);
    when(source.getTables()).thenReturn(tables);
    when(source.getRoutines()).thenReturn(routines);
    return CatalogProjectionBuilder.builder(source)
        .withOptions(
            SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                .withLimitOptions(LimitOptionsBuilder.builder().includeAllRoutines().toOptions()))
        .build();
  }
}
