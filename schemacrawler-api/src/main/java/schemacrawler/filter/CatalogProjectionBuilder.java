/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static java.util.Objects.requireNonNull;
import static schemacrawler.schema.TableRelationshipType.child;
import static schemacrawler.schema.TableRelationshipType.parent;
import static schemacrawler.utility.MetaDataUtility.isPartial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import schemacrawler.schema.Catalog;
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
import schemacrawler.schema.TableRelationshipType;
import schemacrawler.schemacrawler.FilterOptions;
import schemacrawler.schemacrawler.ProjectionOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;

/** Builds immutable catalog projections from an already loaded catalog. */
public final class CatalogProjectionBuilder {

  /** Creates a projection builder for a catalog. */
  public static CatalogProjectionBuilder builder(final Catalog catalog) {
    return new CatalogProjectionBuilder(catalog);
  }

  private static <N extends NamedObject> Set<NamedObjectKey> keys(
      final Collection<N> namedObjects) {
    final Set<NamedObjectKey> keys = new HashSet<>();
    namedObjects.stream().map(NamedObject::key).forEach(keys::add);
    return Set.copyOf(keys);
  }

  private static <N extends NamedObject> List<N> sortedCopy(final Collection<N> objects) {
    final List<N> sortedObjects = new ArrayList<>(objects);
    sortedObjects.sort(Comparator.naturalOrder());
    return List.copyOf(sortedObjects);
  }

  private final Catalog catalog;
  private ProjectionOptions projectionOptions;
  private Predicate<Schema> schemaPredicate;
  private boolean schemaPredicateSpecified;
  private Predicate<Table> tablePredicate;
  private Predicate<Routine> routinePredicate;
  private Predicate<Sequence> sequencePredicate;

  private Predicate<Synonym> synonymPredicate;

  private CatalogProjectionBuilder(final Catalog catalog) {
    this.catalog = requireNonNull(catalog, "No catalog provided");
    projectionOptions = SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions().projectionOptions();
    schemaPredicate = schema -> true;
    schemaPredicateSpecified = false;
    tablePredicate = table -> true;
    routinePredicate = routine -> true;
    sequencePredicate = sequence -> true;
    synonymPredicate = synonym -> true;
  }

  /** Builds an immutable projection from the source catalog. */
  public Catalog build() {
    final List<Schema> eligibleSchemas = sortedCopy(catalog.getSchemas());
    final List<Schema> schemas = selectSchemas(eligibleSchemas);
    final Set<NamedObjectKey> schemaKeys = keys(schemas);
    final boolean sourceHasSchemas = !eligibleSchemas.isEmpty();
    final List<Table> tables = selectTables(schemaKeys, sourceHasSchemas);
    final List<Routine> routines =
        selectDatabaseObjects(catalog.getRoutines(), schemaKeys, sourceHasSchemas, routinePredicate)
            .stream()
            .filter(NamedObjectFilters.routineGrep(projectionOptions.grepOptions()))
            .toList();
    final List<Sequence> sequences =
        selectDatabaseObjects(
            catalog.getSequences(), schemaKeys, sourceHasSchemas, sequencePredicate);
    final List<Synonym> synonyms =
        selectDatabaseObjects(
            catalog.getSynonyms(), schemaKeys, sourceHasSchemas, synonymPredicate);
    final List<ColumnDataType> columnDataTypes = sortedCopy(catalog.getColumnDataTypes());
    final List<DatabaseUser> databaseUsers = sortedCopy(catalog.getDatabaseUsers());

    return new CatalogProjection(
        catalog.getName(),
        catalog.getCrawlInfo(),
        catalog.getDatabaseInfo(),
        catalog.getJdbcDriverInfo(),
        catalog.getAttributes(),
        schemas,
        tables,
        routines,
        sequences,
        synonyms,
        columnDataTypes,
        databaseUsers);
  }

  /** Sets composed filter and grep options for the projection. */
  public CatalogProjectionBuilder withOptions(final ProjectionOptions projectionOptions) {
    if (projectionOptions != null) {
      this.projectionOptions = projectionOptions;
    }
    return this;
  }

  /** Adds a predicate for routines. */
  public CatalogProjectionBuilder withRoutinePredicate(
      final Predicate<? super Routine> routinePredicate) {
    this.routinePredicate = this.routinePredicate.and(requireNonNull(routinePredicate));
    return this;
  }

  /** Adds a predicate for schemas. */
  public CatalogProjectionBuilder withSchemaPredicate(
      final Predicate<? super Schema> schemaPredicate) {
    this.schemaPredicate = this.schemaPredicate.and(requireNonNull(schemaPredicate));
    schemaPredicateSpecified = true;
    return this;
  }

  /** Adds a predicate for sequences. */
  public CatalogProjectionBuilder withSequencePredicate(
      final Predicate<? super Sequence> sequencePredicate) {
    this.sequencePredicate = this.sequencePredicate.and(requireNonNull(sequencePredicate));
    return this;
  }

  /** Adds a predicate for synonyms. */
  public CatalogProjectionBuilder withSynonymPredicate(
      final Predicate<? super Synonym> synonymPredicate) {
    this.synonymPredicate = this.synonymPredicate.and(requireNonNull(synonymPredicate));
    return this;
  }

  /** Adds a predicate for tables. */
  public CatalogProjectionBuilder withTablePredicate(
      final Predicate<? super Table> tablePredicate) {
    this.tablePredicate = this.tablePredicate.and(requireNonNull(tablePredicate));
    return this;
  }

  private Set<Table> includeRelatedTables(
      final Set<Table> seedTables,
      final Set<NamedObjectKey> eligibleTableKeys,
      final Set<NamedObjectKey> schemaKeys,
      final boolean sourceHasSchemas,
      final TableRelationshipType relationshipType,
      final int depth) {
    final Set<NamedObjectKey> visited = new HashSet<>(keys(seedTables));
    final Set<Table> relatedTables = new LinkedHashSet<>();
    Set<Table> frontier = new HashSet<>(seedTables);
    for (int currentDepth = 0; currentDepth < depth; currentDepth++) {
      final Set<Table> nextFrontier = new HashSet<>();
      for (final Table table : frontier) {
        for (final Table relatedTable : table.getRelatedTables(relationshipType)) {
          if (relatedTable != null
              && eligibleTableKeys.contains(relatedTable.key())
              && isSchemaSelected(relatedTable, schemaKeys, sourceHasSchemas)
              && !isPartial(relatedTable)
              && visited.add(relatedTable.key())) {
            nextFrontier.add(relatedTable);
            relatedTables.add(relatedTable);
          }
        }
      }
      frontier = nextFrontier;
    }
    return relatedTables;
  }

  private boolean isSchemaSelected(
      final DatabaseObject databaseObject,
      final Set<NamedObjectKey> schemaKeys,
      final boolean sourceHasSchemas) {
    final Schema schema = databaseObject.getSchema();
    if (schema == null) {
      return !sourceHasSchemas && !schemaPredicateSpecified;
    }
    if (sourceHasSchemas && !schemaKeys.contains(schema.key())) {
      return false;
    }
    return !schemaPredicateSpecified || schemaPredicate.test(schema);
  }

  private <N extends DatabaseObject> List<N> selectDatabaseObjects(
      final Collection<N> databaseObjects,
      final Set<NamedObjectKey> schemaKeys,
      final boolean sourceHasSchemas,
      final Predicate<? super N> predicate) {
    return sortedCopy(
        databaseObjects.stream()
            .filter(
                databaseObject -> isSchemaSelected(databaseObject, schemaKeys, sourceHasSchemas))
            .filter(predicate)
            .toList());
  }

  private List<Schema> selectSchemas(final Collection<Schema> eligibleSchemas) {
    return sortedCopy(eligibleSchemas.stream().filter(schemaPredicate).toList());
  }

  private List<Table> selectTables(
      final Set<NamedObjectKey> schemaKeys, final boolean sourceHasSchemas) {
    final FilterOptions filterOptions = projectionOptions.filterOptions();
    final Predicate<Table> eligibleTableFilter =
        filterOptions.noEmptyTables() ? new NoEmptyTablesFilter() : table -> true;
    final List<Table> allTables =
        sortedCopy(catalog.getTables().stream().filter(eligibleTableFilter).toList());
    final Set<NamedObjectKey> eligibleTableKeys = keys(allTables);
    final Predicate<Table> tableFilter =
        tablePredicate.and(NamedObjectFilters.tableGrep(projectionOptions.grepOptions()));
    final Set<Table> seedTables =
        allTables.stream()
            .filter(table -> isSchemaSelected(table, schemaKeys, sourceHasSchemas))
            .filter(tableFilter)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    final Set<Table> parentTables =
        includeRelatedTables(
            seedTables,
            eligibleTableKeys,
            schemaKeys,
            sourceHasSchemas,
            parent,
            filterOptions.parentTableFilterDepth());
    final Set<Table> childTables =
        includeRelatedTables(
            seedTables,
            eligibleTableKeys,
            schemaKeys,
            sourceHasSchemas,
            child,
            filterOptions.childTableFilterDepth());

    final Set<Table> selectedTables = new LinkedHashSet<>(seedTables);
    selectedTables.addAll(parentTables);
    selectedTables.addAll(childTables);
    return sortedCopy(selectedTables);
  }
}
