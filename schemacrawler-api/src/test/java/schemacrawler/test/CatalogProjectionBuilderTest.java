/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static schemacrawler.utility.TableRowCountsUtility.TABLE_ROW_COUNT_KEY;

import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import schemacrawler.filter.CatalogProjectionBuilder;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.DatabaseUser;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.PartialDatabaseObject;
import schemacrawler.schema.Routine;
import schemacrawler.schema.RoutineType;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableRelationshipType;
import schemacrawler.schema.TableType;
import schemacrawler.schemacrawler.FilterOptions;
import schemacrawler.schemacrawler.FilterOptionsBuilder;
import schemacrawler.schemacrawler.GrepOptions;
import schemacrawler.schemacrawler.GrepOptionsBuilder;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.test.utility.crawl.LightForeignKey;
import schemacrawler.test.utility.crawl.LightProcedure;
import schemacrawler.test.utility.crawl.LightProcedureParameter;
import schemacrawler.test.utility.crawl.LightTable;

public class CatalogProjectionBuilderTest {

  @Test
  public void noEmptyTablesFilterChangesOnlyProjectionMembership() {
    final SchemaReference schema = new SchemaReference("CATALOG", "PUBLIC");
    final Table emptyTable = new LightTable(schema, "empty");
    emptyTable.setAttribute(TABLE_ROW_COUNT_KEY, 0L);
    final Table nonEmptyTable = new LightTable(schema, "non_empty");
    nonEmptyTable.setAttribute(TABLE_ROW_COUNT_KEY, 5L);
    final Table unknownCountTable = new LightTable(schema, "unknown_count");
    final Catalog source = mockCatalogWithTables(emptyTable, nonEmptyTable, unknownCountTable);

    final FilterOptions filterOptions =
        FilterOptionsBuilder.builder().omitEmptyTables(true).toOptions();
    final Catalog projection =
        CatalogProjectionBuilder.builder(source)
            .withOptions(projectionOptions(filterOptions, null))
            .build();
    final Catalog unrestrictedProjection = CatalogProjectionBuilder.builder(source).build();

    assertThat(projection.getTables(), containsInAnyOrder(nonEmptyTable, unknownCountTable));
    assertThat(
        unrestrictedProjection.getTables(),
        containsInAnyOrder(emptyTable, nonEmptyTable, unknownCountTable));
    assertThat(
        source.getTables(), containsInAnyOrder(emptyTable, nonEmptyTable, unknownCountTable));
    assertThat(projection.lookupTable(schema, "empty").isEmpty(), is(true));
  }

  @Test
  public void limitOptionsFilterSchemasTablesAndRoutines() {
    final Schema includedSchema = new SchemaReference("CATALOG", "PUBLIC");
    final Schema excludedSchema = new SchemaReference("CATALOG", "INTERNAL");
    final Table matchingTable = new LightTable(includedSchema, "orders_archive");
    final Table wrongPatternTable = new LightTable(includedSchema, "customers_archive");
    final Table wrongTypeTable =
        new LightTable(includedSchema, "orders_view") {
          @Override
          public TableType getTableType() {
            return new TableType("view");
          }
        };
    final Table excludedSchemaTable = new LightTable(excludedSchema, "orders_archive");
    final LightProcedure includedProcedure = new LightProcedure(includedSchema, "allowed_proc");
    final LightProcedure excludedProcedure = new LightProcedure(includedSchema, "rejected_proc");
    final Routine wrongTypeRoutine =
        namedRoutine(includedSchema, "allowed_function", RoutineType.function);
    final Catalog source =
        mockCatalogWithTables(
            matchingTable, wrongPatternTable, wrongTypeTable, excludedSchemaTable);
    when(source.getSchemas()).thenReturn(List.of(includedSchema, excludedSchema));
    when(source.getRoutines())
        .thenReturn(List.of(includedProcedure, excludedProcedure, wrongTypeRoutine));

    final Catalog projection =
        CatalogProjectionBuilder.builder(source)
            .withOptions(
                SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                    .withLimitOptions(
                        LimitOptionsBuilder.builder()
                            .includeSchemas(Pattern.compile(".*PUBLIC$"))
                            .includeTables(Pattern.compile(".*_archive$"))
                            .tableNamePattern("%orders%")
                            .tableTypes(List.of("table"))
                            .includeRoutines(Pattern.compile(".*allowed.*"))
                            .routineTypes(List.of(RoutineType.procedure))
                            .toOptions()))
            .build();

    assertThat(projection.getSchemas(), contains(includedSchema));
    assertThat(projection.getTables(), containsInAnyOrder(matchingTable, wrongPatternTable));
    assertThat(projection.getRoutines(), contains(includedProcedure));
    assertThat(
        source.getTables(),
        containsInAnyOrder(matchingTable, wrongPatternTable, wrongTypeTable, excludedSchemaTable));
  }

  @Test
  public void relationshipExpansionCannotRestoreTablesExcludedByCurrentLimits() {
    final SchemaReference schema = new SchemaReference("CATALOG", "PUBLIC");
    final RelatedLightTable seed = new RelatedLightTable(schema, "included");
    final RelatedLightTable excludedRelatedTable = new RelatedLightTable(schema, "excluded");
    seed.setRelatedTables(TableRelationshipType.parent, List.of(excludedRelatedTable));
    final Catalog source = mockCatalogWithTables(seed, excludedRelatedTable);
    final SchemaCrawlerOptions options =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
            .withLimitOptions(
                LimitOptionsBuilder.builder()
                    .includeTables(Pattern.compile(".*included$"))
                    .toOptions())
            .withFilterOptions(
                FilterOptionsBuilder.builder().parentTableFilterDepth(1).toOptions());

    final Catalog projection =
        CatalogProjectionBuilder.builder(source).withOptions(options).build();

    assertThat(projection.getTables(), contains(seed));
    assertThat(source.getTables(), containsInAnyOrder(seed, excludedRelatedTable));
  }

  @Test
  public void limitOptionsFilterSequencesAndSynonyms() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Sequence includedSequence =
        namedDatabaseObject(Sequence.class, schema, "allowed_sequence");
    final Sequence excludedSequence =
        namedDatabaseObject(Sequence.class, schema, "excluded_sequence");
    final Synonym includedSynonym = namedDatabaseObject(Synonym.class, schema, "allowed_synonym");
    final Synonym excludedSynonym = namedDatabaseObject(Synonym.class, schema, "excluded_synonym");
    final Catalog source =
        mockCatalogWithTables(new LightTable(schema, "table"), new LightTable(schema, "other"));
    when(source.getSequences()).thenReturn(List.of(includedSequence, excludedSequence));
    when(source.getSynonyms()).thenReturn(List.of(includedSynonym, excludedSynonym));

    final Catalog projection =
        CatalogProjectionBuilder.builder(source)
            .withOptions(
                SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                    .withLimitOptions(
                        LimitOptionsBuilder.builder()
                            .includeSequences(Pattern.compile(".*allowed_sequence$"))
                            .includeSynonyms(Pattern.compile(".*allowed_synonym$"))
                            .toOptions()))
            .build();

    assertThat(projection.getSequences(), contains(includedSequence));
    assertThat(projection.getSynonyms(), contains(includedSynonym));
    assertThat(source.getSequences(), contains(includedSequence, excludedSequence));
    assertThat(source.getSynonyms(), contains(includedSynonym, excludedSynonym));
  }

  @Test
  public void independentImmutableProjection() {
    final Table firstTable = new LightTable("first");
    final Table secondTable = new LightTable("second");
    final Catalog source = mockCatalogWithTables(firstTable, secondTable);

    final Catalog firstProjection =
        CatalogProjectionBuilder.builder(source)
            .withOptions(tableSelectionOptions(".*first$"))
            .build();
    final Catalog secondProjection =
        CatalogProjectionBuilder.builder(source)
            .withOptions(tableSelectionOptions(".*second$"))
            .build();

    assertThat(firstProjection.getTables(), contains(firstTable));
    assertThat(secondProjection.getTables(), contains(secondTable));
    assertThat(source.getTables(), contains(firstTable, secondTable));
    assertThrows(UnsupportedOperationException.class, () -> firstProjection.getTables().clear());
  }

  @Test
  public void collectionsAreImmutableAndNaturallyOrdered() {
    final Table secondTable = new LightTable("second");
    final Table firstTable = new LightTable("first");
    final Catalog source = mockCatalogWithTables(secondTable, firstTable);
    final Catalog projection =
        CatalogProjectionBuilder.builder(source).withOptions(projectionOptions(null, null)).build();

    assertThat(projection.getTables(), is(List.of(firstTable, secondTable)));
    assertUnmodifiable(projection.getSchemas());
    assertUnmodifiable(projection.getTables());
    assertUnmodifiable(projection.getRoutines());
    assertUnmodifiable(projection.getSequences());
    assertUnmodifiable(projection.getSynonyms());
    assertUnmodifiable(projection.getColumnDataTypes());
    assertUnmodifiable(projection.getDatabaseUsers());
  }

  @Test
  public void collectionsFollowNaturalNamedObjectOrder() {
    final SchemaReference schema = new SchemaReference("CATALOG", "PUBLIC");
    final Table naturallyEarlierTable = new LightTable(schema, "Beta");
    final Table naturallyLaterTable = new LightTable(schema, "alpha");
    final Catalog source = mockCatalogWithTables(naturallyLaterTable, naturallyEarlierTable);

    final Catalog projection =
        CatalogProjectionBuilder.builder(source).withOptions(projectionOptions(null, null)).build();

    assertThat(projection.getTables(), contains(naturallyEarlierTable, naturallyLaterTable));
  }

  @Test
  public void lookupsFollowSelectedCollections() {
    final LightTable selectedTable = new LightTable("selected");
    selectedTable.addColumn("id");
    final Table hiddenTable = new LightTable("hidden");
    final Catalog source = mockCatalogWithTables(selectedTable, hiddenTable);
    final Catalog projection =
        CatalogProjectionBuilder.builder(source)
            .withOptions(tableSelectionOptions(".*selected$"))
            .build();

    assertThat(
        projection.lookupTable(selectedTable.getSchema(), "selected").orElseThrow(),
        is(selectedTable));
    assertThat(projection.lookupTable(hiddenTable.getSchema(), "hidden").isEmpty(), is(true));
    assertThat(
        source.lookupTable(hiddenTable.getSchema(), "hidden").orElseThrow(), is(hiddenTable));
    assertThat(
        projection.lookupColumn(selectedTable.getSchema(), "selected", "id").isPresent(), is(true));
    assertThat(
        projection.lookupColumn(hiddenTable.getSchema(), "hidden", "id").isEmpty(), is(true));
  }

  @Test
  public void lookupsFollowSelectedCollectionsForEachObjectType() {
    final SchemaReference schema = new SchemaReference("CATALOG", "PUBLIC");
    final SchemaReference systemSchema = new SchemaReference();
    final Table table = new LightTable(schema, "table");
    final Routine routine = new LightProcedure(schema, "routine");
    final Sequence sequence = namedDatabaseObject(Sequence.class, schema, "sequence");
    final Synonym synonym = namedDatabaseObject(Synonym.class, schema, "synonym");
    final ColumnDataType columnDataType = databaseObject(ColumnDataType.class, schema, "data_type");
    final ColumnDataType systemColumnDataType =
        databaseObject(ColumnDataType.class, systemSchema, "system_type");
    final DatabaseUser databaseUser = namedObject(DatabaseUser.class, "database_user");
    final Catalog source = mock(Catalog.class);
    when(source.getSchemas()).thenReturn(List.of(schema));
    when(source.getTables()).thenReturn(List.of(table));
    when(source.getRoutines()).thenReturn(List.of(routine));
    when(source.getSequences()).thenReturn(List.of(sequence));
    when(source.getSynonyms()).thenReturn(List.of(synonym));
    when(source.getColumnDataTypes()).thenReturn(List.of(columnDataType, systemColumnDataType));
    when(source.getSystemColumnDataTypes()).thenReturn(List.of(systemColumnDataType));
    when(source.getDatabaseUsers()).thenReturn(List.of(databaseUser));

    final Catalog projection =
        CatalogProjectionBuilder.builder(source).withOptions(projectionOptions(null, null)).build();

    assertThat(projection.lookupSchema(schema.getFullName()).orElseThrow(), is(schema));
    assertThat(projection.lookupTable(schema, "table").orElseThrow(), is(table));
    assertThat(projection.lookupRoutine(schema, "routine").orElseThrow(), is(routine));
    assertThat(projection.lookupSequence(schema, "sequence").orElseThrow(), is(sequence));
    assertThat(projection.lookupSynonym(schema, "synonym").orElseThrow(), is(synonym));
    assertThat(
        projection.lookupColumnDataType(schema, "data_type").orElseThrow(), is(columnDataType));
    assertThat(
        projection.lookupSystemColumnDataType("system_type").orElseThrow(),
        is(systemColumnDataType));
    assertThat(projection.getRoutines(schema), contains(routine));
    assertThat(projection.getSequences(schema), contains(sequence));
    assertThat(projection.getSynonyms(schema), contains(synonym));
    assertThat(projection.getColumnDataTypes(schema), contains(columnDataType));
  }

  @Test
  public void schemaBoundObjectsAreExcludedWhenSourceHasNoSchemas() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Table table = new LightTable(schema, "table");
    final Catalog source = mockCatalogWithTables(table);
    when(source.getSchemas()).thenReturn(List.of());

    final Catalog projection = CatalogProjectionBuilder.builder(source).build();

    assertThat(projection.getTables(), is(List.of()));
    assertThat(projection.lookupTable(schema, "table").isEmpty(), is(true));
  }

  @Test
  public void noMatchingSchemaReturnsAnEmptyProjection() {
    final Schema schema = new SchemaReference("CATALOG", "PUBLIC");
    final Table firstTable = new LightTable(schema, "first_table");
    final Table secondTable = new LightTable(schema, "second_table");
    final ColumnDataType columnDataType = databaseObject(ColumnDataType.class, schema, "data_type");
    final DatabaseUser databaseUser = namedObject(DatabaseUser.class, "user");
    final Catalog source = mockCatalogWithTables(firstTable, secondTable);
    when(source.getColumnDataTypes()).thenReturn(List.of(columnDataType));
    when(source.getDatabaseUsers()).thenReturn(List.of(databaseUser));

    final Catalog projection =
        CatalogProjectionBuilder.builder(source)
            .withOptions(
                SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                    .withLimitOptions(
                        LimitOptionsBuilder.builder()
                            .includeSchemas(Pattern.compile(".*OTHER$"))
                            .toOptions()))
            .build();

    assertThat(projection.getSchemas(), is(List.of()));
    assertThat(projection.getTables(), is(List.of()));
    assertThat(projection.getRoutines(), is(List.of()));
    assertThat(projection.getSequences(), is(List.of()));
    assertThat(projection.getSynonyms(), is(List.of()));
    assertThat(projection.getColumnDataTypes(), is(List.of()));
    assertThat(projection.getDatabaseUsers(), is(List.of()));
  }

  @Test
  public void publicColumnGrepSelectsMatchingTables() {
    final LightTable matchingTable = new LightTable("matching");
    matchingTable.addColumn("wanted_column");
    final LightTable nonMatchingTable = new LightTable("non_matching");
    nonMatchingTable.addColumn("other_column");
    final Catalog source = mockCatalogWithTables(matchingTable, nonMatchingTable);

    final Catalog projection =
        CatalogProjectionBuilder.builder(source)
            .withOptions(
                projectionOptions(
                    null,
                    GrepOptionsBuilder.builder()
                        .includeGreppedColumns(
                            new RegularExpressionInclusionRule(Pattern.compile(".*wanted_column")))
                        .toOptions()))
            .build();

    assertThat(projection.getTables(), contains(matchingTable));
    assertThat(source.getTables(), contains(matchingTable, nonMatchingTable));
  }

  @Test
  public void publicTableNameGrepAndInversionSelectMatchingTables() {
    final SchemaReference schema = new SchemaReference("CATALOG", "PUBLIC");
    final Table matchingTable = new LightTable(schema, "matching");
    final Table nonMatchingTable = new LightTable(schema, "non_matching");
    final Catalog source = mockCatalogWithTables(matchingTable, nonMatchingTable);
    final GrepOptionsBuilder grepOptionsBuilder =
        GrepOptionsBuilder.builder()
            .includeGreppedTables(
                new RegularExpressionInclusionRule(Pattern.compile(".*\\.matching$")));

    final Catalog matchingProjection =
        CatalogProjectionBuilder.builder(source)
            .withOptions(projectionOptions(null, grepOptionsBuilder.toOptions()))
            .build();
    final Catalog invertedProjection =
        CatalogProjectionBuilder.builder(source)
            .withOptions(
                projectionOptions(null, grepOptionsBuilder.invertGrepMatch(true).toOptions()))
            .build();

    assertThat(matchingProjection.getTables(), contains(matchingTable));
    assertThat(invertedProjection.getTables(), contains(nonMatchingTable));
  }

  @Test
  public void publicQuotedNameAndTypeFiltersCompose() {
    final SchemaReference schema = new SchemaReference(null, "books");
    final Table matchingTable = mock(Table.class);
    when(matchingTable.getSchema()).thenReturn(schema);
    when(matchingTable.getName()).thenReturn("Celebrity Updates");
    when(matchingTable.getFullName()).thenReturn("books.\"Celebrity Updates\"");
    when(matchingTable.key()).thenReturn(new NamedObjectKey("books", "Celebrity Updates"));
    when(matchingTable.getTableType()).thenReturn(new TableType("table"));
    final LightTable wrongTypeName =
        new LightTable(new SchemaReference("CATALOG", "PUBLIC"), "Other");
    final Catalog source = mockCatalogWithTables(matchingTable, wrongTypeName);

    final Catalog projection =
        CatalogProjectionBuilder.builder(source)
            .withOptions(
                SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                    .withLimitOptions(
                        LimitOptionsBuilder.builder()
                            .includeTables(Pattern.compile(".*Celebrity Updates$"))
                            .tableTypes(List.of("table"))
                            .toOptions()))
            .build();

    assertThat(projection.getTables(), contains(matchingTable));
  }

  @Test
  public void routineParameterGrepIsAppliedToProjection() {
    final SchemaReference schema = new SchemaReference("CATALOG", "PUBLIC");
    final LightProcedure matchingRoutine = new LightProcedure(schema, "matching");
    matchingRoutine.addParameter(new LightProcedureParameter(matchingRoutine, "wanted_parameter"));
    final LightProcedure nonMatchingRoutine = new LightProcedure(schema, "other");
    nonMatchingRoutine.addParameter(
        new LightProcedureParameter(nonMatchingRoutine, "other_parameter"));
    final Catalog source = mockCatalogWithRoutines(schema, matchingRoutine, nonMatchingRoutine);

    final Catalog projection =
        CatalogProjectionBuilder.builder(source)
            .withOptions(
                projectionOptions(
                    null,
                    GrepOptionsBuilder.builder()
                        .includeGreppedRoutineParameters(
                            new RegularExpressionInclusionRule(
                                Pattern.compile(".*wanted_parameter")))
                        .toOptions()))
            .build();

    assertThat(projection.getRoutines(), contains(matchingRoutine));
  }

  @Test
  public void routineCollectionLookupRetainsOverloads() {
    final SchemaReference schema = new SchemaReference("CATALOG", "PUBLIC");
    final Routine firstOverload = mock(Routine.class);
    when(firstOverload.getSchema()).thenReturn(schema);
    when(firstOverload.getName()).thenReturn("overloaded");
    when(firstOverload.getRoutineType()).thenReturn(RoutineType.procedure);
    when(firstOverload.getFullName()).thenReturn("CATALOG.PUBLIC.overloaded");
    when(firstOverload.key()).thenReturn(schema.key().with("overloaded").with("specific_one"));
    final Routine secondOverload = mock(Routine.class);
    when(secondOverload.getSchema()).thenReturn(schema);
    when(secondOverload.getName()).thenReturn("overloaded");
    when(secondOverload.getRoutineType()).thenReturn(RoutineType.procedure);
    when(secondOverload.getFullName()).thenReturn("CATALOG.PUBLIC.overloaded");
    when(secondOverload.key()).thenReturn(schema.key().with("overloaded").with("specific_two"));
    final Catalog source = mockCatalogWithRoutines(schema, firstOverload, secondOverload);
    final Catalog projection =
        CatalogProjectionBuilder.builder(source).withOptions(projectionOptions(null, null)).build();

    assertThat(
        projection.getRoutines(schema, "overloaded"),
        containsInAnyOrder(firstOverload, secondOverload));
  }

  @Test
  public void definitionAndRemarksGrepAndInversionAreApplied() {
    final LightTable remarksMatch = new LightTable("remarks_match");
    remarksMatch.setRemarks("contains secret text");
    final LightTable definitionMatch = new LightTable("definition_match");
    definitionMatch.setDefinition("contains secret text");
    final LightTable nonMatch = new LightTable("non_match");
    final Catalog source = mockCatalogWithTables(remarksMatch, definitionMatch, nonMatch);
    final GrepOptionsBuilder grepOptionsBuilder =
        GrepOptionsBuilder.builder()
            .includeGreppedDefinitions(
                new RegularExpressionInclusionRule(Pattern.compile(".*secret.*")));

    final Catalog matches =
        CatalogProjectionBuilder.builder(source)
            .withOptions(projectionOptions(null, grepOptionsBuilder.toOptions()))
            .build();
    final Catalog invertedMatches =
        CatalogProjectionBuilder.builder(source)
            .withOptions(
                projectionOptions(null, grepOptionsBuilder.invertGrepMatch(true).toOptions()))
            .build();

    assertThat(matches.getTables(), containsInAnyOrder(remarksMatch, definitionMatch));
    assertThat(invertedMatches.getTables(), contains(nonMatch));
  }

  @Test
  public void nestedProjectionCannotRestoreHiddenTables() {
    final Table firstTable = new LightTable("first");
    final Table secondTable = new LightTable("second");
    final Catalog baseline = mockCatalogWithTables(firstTable, secondTable);
    final Catalog firstProjection =
        CatalogProjectionBuilder.builder(baseline)
            .withOptions(tableSelectionOptions(".*first$"))
            .build();

    final Catalog nestedProjection = CatalogProjectionBuilder.builder(firstProjection).build();
    final Catalog independentProjection = CatalogProjectionBuilder.builder(baseline).build();

    assertThat(nestedProjection.getTables(), contains(firstTable));
    assertThat(independentProjection.getTables(), containsInAnyOrder(firstTable, secondTable));
  }

  @Test
  public void relationshipExpansionUsesOriginalSeedsAndKeepsForeignKeyEndpoints() {
    final SchemaReference schema = new SchemaReference("CATALOG", "PUBLIC");
    final RelatedLightTable seed = new RelatedLightTable(schema, "seed");
    final RelatedLightTable parent = new RelatedLightTable(schema, "parent");
    final RelatedLightTable grandparent = new RelatedLightTable(schema, "grandparent");
    final RelatedLightTable grandchild = new RelatedLightTable(schema, "grandchild");
    final RelatedLightTable directChild = new RelatedLightTable(schema, "direct_child");
    final PartialRelatedLightTable partial = new PartialRelatedLightTable(schema, "partial");
    final ForeignKey foreignKey = new LightForeignKey("fk_seed_parent", seed, parent);
    seed.setRelatedTables(TableRelationshipType.parent, List.of(parent, partial));
    seed.setRelatedTables(TableRelationshipType.child, List.of(directChild));
    seed.setImportedForeignKeys(List.of(foreignKey));
    parent.setRelatedTables(TableRelationshipType.parent, List.of(seed, grandparent));
    parent.setRelatedTables(TableRelationshipType.child, List.of(grandchild));
    directChild.setRelatedTables(TableRelationshipType.child, List.of(seed));
    final Catalog source =
        mockCatalogWithTables(seed, parent, grandparent, grandchild, directChild, partial);

    final Catalog projection =
        CatalogProjectionBuilder.builder(source)
            .withOptions(
                projectionOptions(
                    FilterOptionsBuilder.builder()
                        .parentTableFilterDepth(2)
                        .childTableFilterDepth(2)
                        .toOptions(),
                    GrepOptionsBuilder.builder()
                        .includeGreppedTables(
                            new RegularExpressionInclusionRule(Pattern.compile(".*\\.seed$")))
                        .toOptions()))
            .build();

    assertThat(projection.getTables(), containsInAnyOrder(seed, parent, grandparent, directChild));
    assertThat(seed.getImportedForeignKeys(), contains(foreignKey));
    assertThat(foreignKey.getPrimaryKeyTable(), is(parent));
    assertThat(projection.lookupTable(schema, "grandchild").isEmpty(), is(true));
    assertThat(projection.lookupTable(schema, "partial").isEmpty(), is(true));
  }

  @Test
  public void sharedObjectSettersDoNotChangeMembership() {
    final Table table = new LightTable("table");
    final Catalog source = mockCatalogWithTables(table);
    table.setRemarks("before update");
    final GrepOptions includeBeforeUpdate =
        GrepOptionsBuilder.builder()
            .includeGreppedDefinitions(
                new RegularExpressionInclusionRule(Pattern.compile(".*before update.*")))
            .toOptions();
    final Catalog emptyProjection =
        CatalogProjectionBuilder.builder(source)
            .withOptions(
                projectionOptions(
                    null,
                    GrepOptionsBuilder.builder()
                        .includeGreppedDefinitions(
                            new RegularExpressionInclusionRule(
                                Pattern.compile(".*before update.*")))
                        .invertGrepMatch(true)
                        .toOptions()))
            .build();
    final Catalog selectedProjection =
        CatalogProjectionBuilder.builder(source)
            .withOptions(projectionOptions(null, includeBeforeUpdate))
            .build();

    table.setRemarks("after update");

    assertThat(emptyProjection.getTables(), is(List.of()));
    assertThat(selectedProjection.getTables().iterator().next().getRemarks(), is("after update"));
    assertThat(
        CatalogProjectionBuilder.builder(source)
            .withOptions(projectionOptions(null, includeBeforeUpdate))
            .build()
            .getTables(),
        is(List.of()));
  }

  @Test
  public void projectionAttributesAreIndependentFromSourceCatalog() {
    final Catalog source = mockCatalogWithTables(new LightTable("table"), new LightTable("unused"));
    when(source.getAttributes())
        .thenReturn(Map.of("baseline", "before", "REMARKS", "baseline remarks"));
    final Catalog projection = CatalogProjectionBuilder.builder(source).build();

    assertThrows(
        UnsupportedOperationException.class, () -> projection.setAttribute("new", "value"));
    assertThrows(UnsupportedOperationException.class, () -> projection.removeAttribute("baseline"));
    assertThrows(UnsupportedOperationException.class, () -> projection.setRemarks("changed"));
    assertThrows(
        UnsupportedOperationException.class, () -> projection.getAttributes().put("new", "value"));
    assertThat(source.hasAttribute("projection"), is(false));
    assertThat(projection.getAttribute("baseline", ""), is("before"));
    assertThat(projection.hasAttribute("REMARKS"), is(true));
    assertThat(projection.getRemarks(), is("baseline remarks"));
  }

  private void assertUnmodifiable(final Collection<?> collection) {
    assertThrows(UnsupportedOperationException.class, collection::clear);
  }

  private SchemaCrawlerOptions projectionOptions(
      final FilterOptions filterOptions, final GrepOptions grepOptions) {
    return SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
        .withLimitOptions(
            LimitOptionsBuilder.builder()
                .includeAllRoutines()
                .includeAllSequences()
                .includeAllSynonyms()
                .toOptions())
        .withFilterOptions(filterOptions)
        .withGrepOptions(grepOptions);
  }

  private SchemaCrawlerOptions tableSelectionOptions(final String tableNamePattern) {
    return SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
        .withLimitOptions(
            LimitOptionsBuilder.builder()
                .includeTables(Pattern.compile(tableNamePattern))
                .toOptions());
  }

  private <T extends DatabaseObject> T databaseObject(
      final Class<T> objectClass, final Schema schema, final String name) {
    final T databaseObject = mock(objectClass);
    when(databaseObject.getSchema()).thenReturn(schema);
    when(databaseObject.getName()).thenReturn(name);
    return databaseObject;
  }

  private <T extends DatabaseObject> T namedDatabaseObject(
      final Class<T> objectClass, final Schema schema, final String name) {
    final T databaseObject = databaseObject(objectClass, schema, name);
    when(databaseObject.getFullName()).thenReturn(schema.getFullName() + "." + name);
    when(databaseObject.key()).thenReturn(schema.key().with(name));
    return databaseObject;
  }

  private Routine namedRoutine(
      final Schema schema, final String name, final RoutineType routineType) {
    final Routine routine = namedDatabaseObject(Routine.class, schema, name);
    when(routine.getRoutineType()).thenReturn(routineType);
    return routine;
  }

  private <T extends NamedObject> T namedObject(final Class<T> objectClass, final String name) {
    final T namedObject = mock(objectClass);
    when(namedObject.getName()).thenReturn(name);
    when(namedObject.key()).thenReturn(new NamedObjectKey(name));
    return namedObject;
  }

  private Catalog mockCatalogWithRoutines(final Schema schema, final Routine... routines) {
    final Catalog catalog = mock(Catalog.class);
    when(catalog.getSchemas()).thenReturn(List.of(schema));
    when(catalog.getTables()).thenReturn(List.of());
    when(catalog.getRoutines()).thenReturn(List.of(routines));
    when(catalog.getSequences()).thenReturn(List.of());
    when(catalog.getSynonyms()).thenReturn(List.of());
    when(catalog.getColumnDataTypes()).thenReturn(List.of());
    when(catalog.getDatabaseUsers()).thenReturn(List.of());
    return catalog;
  }

  private Catalog mockCatalogWithTables(final Table... tables) {
    final Catalog catalog = mock(Catalog.class);
    final Map<NamedObjectKey, Schema> schemas = new LinkedHashMap<>();
    for (final Table table : tables) {
      if (table.getSchema() != null) {
        schemas.putIfAbsent(table.getSchema().key(), table.getSchema());
      }
    }
    when(catalog.getSchemas()).thenReturn(List.copyOf(schemas.values()));
    when(catalog.getTables()).thenReturn(List.of(tables));
    when(catalog.getRoutines()).thenReturn(List.of());
    when(catalog.getSequences()).thenReturn(List.of());
    when(catalog.getSynonyms()).thenReturn(List.of());
    when(catalog.getColumnDataTypes()).thenReturn(List.of());
    when(catalog.getDatabaseUsers()).thenReturn(List.of());
    for (final Table table : tables) {
      when(catalog.lookupTable(table.getSchema(), table.getName())).thenReturn(Optional.of(table));
    }
    return catalog;
  }

  private static class RelatedLightTable extends LightTable {

    private final Map<TableRelationshipType, List<Table>> relatedTables =
        new EnumMap<>(TableRelationshipType.class);
    private List<ForeignKey> importedForeignKeys = List.of();

    RelatedLightTable(final Schema schema, final String name) {
      super(schema, name);
    }

    @Override
    public Collection<ForeignKey> getImportedForeignKeys() {
      return importedForeignKeys;
    }

    @Override
    public Collection<Table> getRelatedTables(final TableRelationshipType relationshipType) {
      return relatedTables.getOrDefault(relationshipType, List.of());
    }

    void setImportedForeignKeys(final List<ForeignKey> importedForeignKeys) {
      this.importedForeignKeys = List.copyOf(importedForeignKeys);
    }

    void setRelatedTables(
        final TableRelationshipType relationshipType, final List<Table> relatedTables) {
      this.relatedTables.put(relationshipType, List.copyOf(relatedTables));
    }
  }

  private static final class PartialRelatedLightTable extends RelatedLightTable
      implements PartialDatabaseObject {

    PartialRelatedLightTable(final Schema schema, final String name) {
      super(schema, name);
    }
  }
}
