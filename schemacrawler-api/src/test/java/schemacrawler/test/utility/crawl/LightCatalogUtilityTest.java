/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static schemacrawler.test.utility.crawl.LightCatalogUtility.lightCatalog;

import java.util.List;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaReference;

public class LightCatalogUtilityTest {

  private final Schema schemaA = new SchemaReference("catalog", "A");
  private final Schema schemaB = new SchemaReference("catalog", "B");
  private final Schema schemaC = new SchemaReference("catalog", "C");

  @Test
  public void emptyCatalogHoldsNothing() {
    final Catalog catalog = lightCatalog();

    assertThat(catalog.getTables(), is(empty()));
    assertThat(catalog.getRoutines(), is(empty()));
    assertThat(catalog.getSequences(), is(empty()));
    assertThat(catalog.getSynonyms(), is(empty()));
    assertThat(catalog.getSchemas(), is(empty()));
    assertThat(catalog.hasTables(), is(false));
    assertThat(catalog.hasRoutines(), is(false));
    assertThat(catalog.hasSequences(), is(false));
    assertThat(catalog.hasSynonyms(), is(false));
    assertThat(catalog.hasSchemas(), is(false));
  }

  @Test
  public void addedObjectsAreReturned() {
    final Table table = new LightTable(schemaA, "customer");
    final Routine routine = new LightProcedure(schemaA, "refresh_customer");
    final Sequence sequence = new LightSequence(schemaA, "customer_seq");
    final Synonym synonym = new LightSynonym(schemaA, "customer_alias");

    final Catalog catalog =
        lightCatalog(List.of(table), List.of(routine), List.of(sequence), List.of(synonym));

    assertThat(catalog.getTables(), contains(table));
    assertThat(catalog.getRoutines(), contains(routine));
    assertThat(catalog.getSequences(), contains(sequence));
    assertThat(catalog.getSynonyms(), contains(synonym));
    assertThat(catalog.hasTables(), is(true));
    assertThat(catalog.hasRoutines(), is(true));
    assertThat(catalog.hasSequences(), is(true));
    assertThat(catalog.hasSynonyms(), is(true));
  }

  @Test
  public void tablesOnlyFormHoldsTables() {
    final Table table = new LightTable(schemaA, "customer");

    final Catalog catalog = lightCatalog(table);

    assertThat(catalog.getTables(), contains(table));
    assertThat(catalog.getSchemas(), hasSize(1));
  }

  @Test
  public void schemasAreDistinctAndInTheOrderFirstSeen() {
    final Table tableB = new LightTable(schemaB, "order");
    final Table tableA = new LightTable(schemaA, "customer");
    final Routine routineA = new LightProcedure(schemaA, "refresh_customer");
    final Sequence sequenceC = new LightSequence(schemaC, "customer_seq");

    final Catalog catalog =
        lightCatalog(List.of(tableB, tableA), List.of(routineA), List.of(sequenceC), List.of());

    assertThat(catalog.getSchemas(), contains(schemaB, schemaA, schemaC));
    assertThat(catalog.hasSchemas(), is(true));
  }

  @Test
  public void equalSchemaReferencesCountOnce() {
    final Table first = new LightTable(new SchemaReference("catalog", "A"), "customer");
    final Table second = new LightTable(new SchemaReference("catalog", "A"), "order");

    final Catalog catalog = lightCatalog(first, second);

    assertThat(catalog.getSchemas(), hasSize(1));
  }

  @Test
  public void lookupsBySchemaReturnTheObjectsOfThatSchema() {
    final Table tableB = new LightTable(schemaB, "order");
    final Table tableA = new LightTable(schemaA, "customer");
    final Routine routineA = new LightProcedure(schemaA, "refresh_customer");
    final Routine routineB = new LightProcedure(schemaB, "refresh_order");
    final Sequence sequenceB = new LightSequence(schemaB, "order_seq");
    final Synonym synonymA = new LightSynonym(schemaA, "customer_alias");

    final Catalog catalog =
        lightCatalog(
            List.of(tableB, tableA),
            List.of(routineA, routineB),
            List.of(sequenceB),
            List.of(synonymA));

    assertThat(catalog.getTables(schemaA), contains(tableA));
    assertThat(catalog.getTables(schemaB), contains(tableB));
    assertThat(catalog.getRoutines(schemaA), contains(routineA));
    assertThat(catalog.getRoutines(schemaB), contains(routineB));
    assertThat(catalog.getSequences(schemaA), is(empty()));
    assertThat(catalog.getSequences(schemaB), contains(sequenceB));
    assertThat(catalog.getSynonyms(schemaA), contains(synonymA));
    assertThat(catalog.getSynonyms(schemaB), is(empty()));
    assertThat(catalog.hasTables(schemaA), is(true));
    assertThat(catalog.hasSequences(schemaA), is(false));
    assertThat(catalog.getTables(), containsInAnyOrder(tableA, tableB));
  }

  @Test
  public void routineLookupBySchemaAndName() {
    final Routine first = new LightProcedure(schemaA, "refresh", "refresh_1");
    final Routine second = new LightProcedure(schemaA, "refresh", "refresh_2");
    final Routine other = new LightProcedure(schemaA, "archive");

    final Catalog catalog =
        lightCatalog(List.of(), List.of(first, second, other), List.of(), List.of());

    assertThat(catalog.getRoutines(schemaA, "refresh"), contains(first, second));
    assertThat(catalog.getRoutines(schemaA, "missing"), is(empty()));
  }

  @Test
  public void schemaWithNoObjectsGivesEmptyLookups() {
    final Catalog catalog = lightCatalog(new LightTable(schemaA, "customer"));

    assertThat(catalog.getTables(schemaC), is(empty()));
    assertThat(catalog.getRoutines(schemaC), is(empty()));
    assertThat(catalog.getSequences(schemaC), is(empty()));
    assertThat(catalog.getSynonyms(schemaC), is(empty()));
    assertThat(catalog.hasTables(schemaC), is(false));
  }

  @Test
  public void methodsThatAreNotCoveredReturnEmptyValues() {
    final Catalog catalog = lightCatalog(new LightTable(schemaA, "customer"));

    assertThat(catalog.getDatabaseUsers(), is(empty()));
    assertThat(catalog.getColumnDataTypes(), is(empty()));
    assertThat(catalog.lookupTable(schemaA, "customer").isPresent(), is(false));
  }
}
