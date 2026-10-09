/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static java.util.Objects.requireNonNull;
import static schemacrawler.schemacrawler.DatabaseObjectRuleForInclusion.ruleForRoutineInclusion;
import static schemacrawler.schemacrawler.DatabaseObjectRuleForInclusion.ruleForSchemaInclusion;
import static schemacrawler.schemacrawler.DatabaseObjectRuleForInclusion.ruleForSequenceInclusion;
import static schemacrawler.schemacrawler.DatabaseObjectRuleForInclusion.ruleForSynonymInclusion;
import static schemacrawler.schemacrawler.DatabaseObjectRuleForInclusion.ruleForTableInclusion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
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
import schemacrawler.schemacrawler.FilterOptions;
import schemacrawler.schemacrawler.GrepOptions;
import schemacrawler.schemacrawler.LimitOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;

/** Builds immutable catalog projections from an already loaded catalog. */
public final class CatalogProjectionBuilder {

  static final class SchemaFilter<D extends DatabaseObject> implements NamedObjectFilter<D> {

    private final Set<NamedObjectKey> schemaKeys;

    public SchemaFilter(final Set<NamedObjectKey> schemaKeys) {
      this.schemaKeys = requireNonNull(schemaKeys, "No schema keys provided");
    }

    @Override
    public boolean test(final D databaseObject) {
      final Schema schema = databaseObject.getSchema();
      return schemaKeys.contains(schema.key());
    }
  }

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
    Collections.sort(sortedObjects);
    return List.copyOf(sortedObjects);
  }

  private final Catalog catalog;

  private SchemaCrawlerOptions schemaCrawlerOptions;

  private CatalogProjectionBuilder(final Catalog catalog) {
    this.catalog = requireNonNull(catalog, "No catalog provided");
    schemaCrawlerOptions = SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions();
  }

  /** Builds an immutable projection from the source catalog. */
  public Catalog build() {
    final List<Schema> eligibleSchemas = sortedCopy(catalog.getSchemas());
    final List<Schema> schemas = selectSchemas(eligibleSchemas);
    final Set<NamedObjectKey> schemaKeys = keys(schemas);
    final boolean sourceHasSchemas = !eligibleSchemas.isEmpty();
    if (sourceHasSchemas && schemas.isEmpty()) {
      return new CatalogProjection(
          catalog.getName(),
          catalog.getCrawlInfo(),
          catalog.getDatabaseInfo(),
          catalog.getJdbcDriverInfo(),
          catalog.getAttributes());
    }

    @NonNull final LimitOptions limitOptions = schemaCrawlerOptions.limitOptions();
    @NonNull final GrepOptions grepOptions = schemaCrawlerOptions.grepOptions();

    final List<Table> tables = selectTables(schemaKeys);

    final Predicate<Routine> routineFilter =
        new SchemaFilter<Routine>(schemaKeys)
            .and(new DatabaseObjectFilter<>(limitOptions, ruleForRoutineInclusion))
            .and(new RoutineTypesFilter(limitOptions))
            .and(NamedObjectFilters.routineGrep(grepOptions));
    final List<Routine> routines =
        sortedCopy(catalog.getRoutines().stream().filter(routineFilter).toList());

    final Predicate<Sequence> sequenceFilter =
        new SchemaFilter<Sequence>(schemaKeys)
            .and(new DatabaseObjectFilter<>(limitOptions, ruleForSequenceInclusion));
    final List<Sequence> sequences =
        sortedCopy(catalog.getSequences().stream().filter(sequenceFilter).toList());

    final Predicate<Synonym> synonymFilter =
        new SchemaFilter<Synonym>(schemaKeys)
            .and(new DatabaseObjectFilter<>(limitOptions, ruleForSynonymInclusion));
    final List<Synonym> synonyms =
        sortedCopy(catalog.getSynonyms().stream().filter(synonymFilter).toList());

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
  public CatalogProjectionBuilder withOptions(final SchemaCrawlerOptions schemaCrawlerOptions) {
    if (schemaCrawlerOptions != null) {
      this.schemaCrawlerOptions = schemaCrawlerOptions;
    }
    return this;
  }

  private List<Schema> selectSchemas(final Collection<Schema> eligibleSchemas) {
    final Predicate<Schema> schemaLimitFilter =
        NamedObjectFilters.fullName(
            schemaCrawlerOptions.limitOptions().get(ruleForSchemaInclusion));
    return sortedCopy(eligibleSchemas.stream().filter(schemaLimitFilter).toList());
  }

  private List<Table> selectTables(final Set<NamedObjectKey> schemaKeys) {
    @NonNull final LimitOptions limitOptions = schemaCrawlerOptions.limitOptions();
    @NonNull final FilterOptions filterOptions = schemaCrawlerOptions.filterOptions();
    @NonNull final GrepOptions grepOptions = schemaCrawlerOptions.grepOptions();

    final Predicate<Table> limitTableFilter =
        new SchemaFilter<Table>(schemaKeys)
            .and(new DatabaseObjectFilter<>(limitOptions, ruleForTableInclusion))
            .and(new TableTypesFilter(limitOptions))
            .and(filterOptions.omitEmptyTables() ? new OmitEmptyTablesFilter() : table -> true);
    final List<Table> limitedTables =
        sortedCopy(catalog.getTables().stream().filter(limitTableFilter).toList());

    final Set<Table> greppedTables =
        limitedTables.stream()
            .filter(NamedObjectFilters.tableGrep(grepOptions))
            .collect(Collectors.toSet());

    final NamedObjectFilter<Table> relatedTableFilter =
        new RelatedTableFilter(
            limitedTables,
            greppedTables,
            filterOptions.parentTableFilterDepth(),
            filterOptions.childTableFilterDepth());
    return sortedCopy(limitedTables.stream().filter(relatedTableFilter).toList());
  }
}
