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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.CrawlInfo;
import schemacrawler.schema.DatabaseInfo;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.DatabaseUser;
import schemacrawler.schema.JdbcDriverInfo;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableRelationshipType;
import schemacrawler.schemacrawler.CatalogProjectionOptions;
import schemacrawler.schemacrawler.CatalogProjectionOptionsBuilder;
import schemacrawler.schemacrawler.FilterOptions;
import schemacrawler.schemacrawler.GrepOptions;
import schemacrawler.schemacrawler.SchemaReference;

/** Builds immutable catalog projections from an already loaded catalog. */
public final class CatalogProjectionBuilder {

  private static <N extends NamedObject> List<N> sortedCopy(final Collection<N> objects) {
    final List<N> sortedObjects = new ArrayList<>(objects);
    sortedObjects.sort(Comparator.naturalOrder());
    return List.copyOf(sortedObjects);
  }

  /** Creates a projection builder for a catalog. */
  public static CatalogProjectionBuilder builder(final Catalog catalog) {
    return new CatalogProjectionBuilder(catalog);
  }

  private final Catalog catalog;
  private CatalogProjectionOptions projectionOptions;
  private Predicate<Schema> schemaPredicate;
  private boolean schemaPredicateSpecified;
  private Predicate<Table> tablePredicate;
  private Predicate<Routine> routinePredicate;
  private Predicate<Sequence> sequencePredicate;
  private Predicate<Synonym> synonymPredicate;
  private Predicate<ColumnDataType> columnDataTypePredicate;
  private Predicate<DatabaseUser> databaseUserPredicate;

  private CatalogProjectionBuilder(final Catalog catalog) {
    this.catalog = requireNonNull(catalog, "No catalog provided");
    projectionOptions = CatalogProjectionOptionsBuilder.newCatalogProjectionOptions();
    schemaPredicate = schema -> true;
    schemaPredicateSpecified = false;
    tablePredicate = table -> true;
    routinePredicate = routine -> true;
    sequencePredicate = sequence -> true;
    synonymPredicate = synonym -> true;
    columnDataTypePredicate = columnDataType -> true;
    databaseUserPredicate = databaseUser -> true;
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
    final List<ColumnDataType> columnDataTypes =
        sortedCopy(catalog.getColumnDataTypes().stream().filter(columnDataTypePredicate).toList());
    final List<DatabaseUser> databaseUsers =
        sortedCopy(catalog.getDatabaseUsers().stream().filter(databaseUserPredicate).toList());

    return new ImmutableCatalogProjection(
        catalog, schemas, tables, routines, sequences, synonyms, columnDataTypes, databaseUsers);
  }

  /** Sets filter and grep options for the projection. */
  public CatalogProjectionBuilder withOptions(final CatalogProjectionOptions projectionOptions) {
    if (projectionOptions != null) {
      this.projectionOptions = projectionOptions;
    }
    return this;
  }

  /** Sets filter options for related-table expansion. */
  public CatalogProjectionBuilder withFilterOptions(final FilterOptions filterOptions) {
    if (filterOptions != null) {
      projectionOptions =
          CatalogProjectionOptionsBuilder.builder()
              .fromOptions(projectionOptions)
              .withFilterOptions(filterOptions)
              .toOptions();
    }
    return this;
  }

  /** Sets grep options for object selection. */
  public CatalogProjectionBuilder withGrepOptions(final GrepOptions grepOptions) {
    if (grepOptions != null) {
      projectionOptions =
          CatalogProjectionOptionsBuilder.builder()
              .fromOptions(projectionOptions)
              .withGrepOptions(grepOptions)
              .toOptions();
    }
    return this;
  }

  /** Adds a predicate for schemas. */
  public CatalogProjectionBuilder withSchemaPredicate(
      final Predicate<? super Schema> schemaPredicate) {
    this.schemaPredicate = this.schemaPredicate.and(requireNonNull(schemaPredicate));
    schemaPredicateSpecified = true;
    return this;
  }

  /** Adds a predicate for tables. */
  public CatalogProjectionBuilder withTablePredicate(
      final Predicate<? super Table> tablePredicate) {
    this.tablePredicate = this.tablePredicate.and(requireNonNull(tablePredicate));
    return this;
  }

  /** Adds a predicate for routines. */
  public CatalogProjectionBuilder withRoutinePredicate(
      final Predicate<? super Routine> routinePredicate) {
    this.routinePredicate = this.routinePredicate.and(requireNonNull(routinePredicate));
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

  /** Adds a predicate for column data types. */
  public CatalogProjectionBuilder withColumnDataTypePredicate(
      final Predicate<? super ColumnDataType> columnDataTypePredicate) {
    this.columnDataTypePredicate =
        this.columnDataTypePredicate.and(requireNonNull(columnDataTypePredicate));
    return this;
  }

  /** Adds a predicate for database users. */
  public CatalogProjectionBuilder withDatabaseUserPredicate(
      final Predicate<? super DatabaseUser> databaseUserPredicate) {
    this.databaseUserPredicate =
        this.databaseUserPredicate.and(requireNonNull(databaseUserPredicate));
    return this;
  }

  private List<Schema> selectSchemas(final Collection<Schema> eligibleSchemas) {
    return sortedCopy(eligibleSchemas.stream().filter(schemaPredicate).toList());
  }

  private List<Table> selectTables(
      final Set<NamedObjectKey> schemaKeys, final boolean sourceHasSchemas) {
    final List<Table> allTables = sortedCopy(catalog.getTables());
    final Set<NamedObjectKey> eligibleTableKeys = keys(allTables);
    final Predicate<Table> tableFilter =
        tablePredicate.and(NamedObjectFilters.tableGrep(projectionOptions.grepOptions()));
    final Set<Table> seedTables =
        allTables.stream()
            .filter(table -> isSchemaSelected(table, schemaKeys, sourceHasSchemas))
            .filter(tableFilter)
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

    final FilterOptions filterOptions = projectionOptions.filterOptions();
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

  private static <N extends DatabaseObject> Map<NamedObjectKey, N> indexBySchemaAndName(
      final Collection<N> objects) {
    final Map<NamedObjectKey, N> index = new java.util.HashMap<>();
    for (final N object : objects) {
      final Schema schema = object.getSchema();
      if (schema != null) {
        index.putIfAbsent(schema.key().with(object.getName()), object);
      }
    }
    return Map.copyOf(index);
  }

  private static <N extends DatabaseObject> Optional<N> lookupDatabaseObject(
      final Map<NamedObjectKey, N> index, final Schema schema, final String name) {
    if (schema == null || name == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(index.get(schema.key().with(name)));
  }

  private static <N extends NamedObject> Set<NamedObjectKey> keys(
      final Collection<N> namedObjects) {
    final Set<NamedObjectKey> keys = new HashSet<>();
    namedObjects.stream().map(NamedObject::key).forEach(keys::add);
    return Set.copyOf(keys);
  }

  private static final class ImmutableCatalogProjection implements Catalog {

    private final Catalog source;
    private final List<Schema> schemas;
    private final Map<String, Schema> schemasByName;
    private final List<Table> tables;
    private final Map<NamedObjectKey, Table> tablesBySchemaAndName;
    private final List<Routine> routines;
    private final Map<NamedObjectKey, Routine> routinesBySchemaAndName;
    private final List<Sequence> sequences;
    private final Map<NamedObjectKey, Sequence> sequencesBySchemaAndName;
    private final List<Synonym> synonyms;
    private final Map<NamedObjectKey, Synonym> synonymsBySchemaAndName;
    private final List<ColumnDataType> columnDataTypes;
    private final Map<NamedObjectKey, ColumnDataType> columnDataTypesBySchemaAndName;
    private final List<DatabaseUser> databaseUsers;

    ImmutableCatalogProjection(
        final Catalog source,
        final List<Schema> schemas,
        final List<Table> tables,
        final List<Routine> routines,
        final List<Sequence> sequences,
        final List<Synonym> synonyms,
        final List<ColumnDataType> columnDataTypes,
        final List<DatabaseUser> databaseUsers) {
      this.source = source;
      this.schemas = List.copyOf(schemas);
      schemasByName =
          this.schemas.stream()
              .collect(
                  java.util.stream.Collectors.toUnmodifiableMap(
                      Schema::getFullName, schema -> schema, (first, second) -> first));
      this.tables = List.copyOf(tables);
      tablesBySchemaAndName = indexBySchemaAndName(this.tables);
      this.routines = List.copyOf(routines);
      routinesBySchemaAndName = indexBySchemaAndName(this.routines);
      this.sequences = List.copyOf(sequences);
      sequencesBySchemaAndName = indexBySchemaAndName(this.sequences);
      this.synonyms = List.copyOf(synonyms);
      synonymsBySchemaAndName = indexBySchemaAndName(this.synonyms);
      this.columnDataTypes = List.copyOf(columnDataTypes);
      columnDataTypesBySchemaAndName = indexBySchemaAndName(this.columnDataTypes);
      this.databaseUsers = List.copyOf(databaseUsers);
    }

    @Override
    public Collection<ColumnDataType> getColumnDataTypes() {
      return columnDataTypes;
    }

    @Override
    public Collection<ColumnDataType> getColumnDataTypes(final Schema schema) {
      return columnDataTypes.stream()
          .filter(columnDataType -> schema != null && schema.equals(columnDataType.getSchema()))
          .toList();
    }

    @Override
    public CrawlInfo getCrawlInfo() {
      return source.getCrawlInfo();
    }

    @Override
    public DatabaseInfo getDatabaseInfo() {
      return source.getDatabaseInfo();
    }

    @Override
    public Collection<DatabaseUser> getDatabaseUsers() {
      return databaseUsers;
    }

    @Override
    public JdbcDriverInfo getJdbcDriverInfo() {
      return source.getJdbcDriverInfo();
    }

    @Override
    public Collection<Routine> getRoutines() {
      return routines;
    }

    @Override
    public Collection<Routine> getRoutines(final Schema schema) {
      return routines.stream()
          .filter(routine -> schema != null && schema.equals(routine.getSchema()))
          .toList();
    }

    @Override
    public Collection<Routine> getRoutines(final Schema schema, final String routineName) {
      return routines.stream()
          .filter(routine -> schema != null && schema.equals(routine.getSchema()))
          .filter(routine -> routineName != null && routineName.equals(routine.getName()))
          .toList();
    }

    @Override
    public Collection<Schema> getSchemas() {
      return schemas;
    }

    @Override
    public Collection<Sequence> getSequences() {
      return sequences;
    }

    @Override
    public Collection<Sequence> getSequences(final Schema schema) {
      return sequences.stream()
          .filter(sequence -> schema != null && schema.equals(sequence.getSchema()))
          .toList();
    }

    @Override
    public Collection<Synonym> getSynonyms() {
      return synonyms;
    }

    @Override
    public Collection<Synonym> getSynonyms(final Schema schema) {
      return synonyms.stream()
          .filter(synonym -> schema != null && schema.equals(synonym.getSchema()))
          .toList();
    }

    @Override
    public Collection<ColumnDataType> getSystemColumnDataTypes() {
      return source.getSystemColumnDataTypes().stream()
          .filter(
              columnDataType ->
                  columnDataTypesBySchemaAndName.containsKey(
                      columnDataType.getSchema().key().with(columnDataType.getName())))
          .toList();
    }

    @Override
    public Collection<Table> getTables() {
      return tables;
    }

    @Override
    public Collection<Table> getTables(final Schema schema) {
      return tables.stream()
          .filter(table -> schema != null && schema.equals(table.getSchema()))
          .toList();
    }

    @Override
    public Optional<Column> lookupColumn(
        final Schema schema, final String tableName, final String name) {
      return lookupDatabaseObject(tablesBySchemaAndName, schema, tableName)
          .flatMap(table -> table.lookupColumn(name));
    }

    @Override
    public <C extends ColumnDataType> Optional<C> lookupColumnDataType(
        final Schema schema, final String dataTypeName) {
      return (Optional<C>)
          lookupDatabaseObject(columnDataTypesBySchemaAndName, schema, dataTypeName);
    }

    @Override
    public <R extends Routine> Optional<R> lookupRoutine(final Schema schema, final String name) {
      return (Optional<R>) lookupDatabaseObject(routinesBySchemaAndName, schema, name);
    }

    @Override
    public <S extends Schema> Optional<S> lookupSchema(final String name) {
      return (Optional<S>) Optional.ofNullable(schemasByName.get(name));
    }

    @Override
    public <S extends Sequence> Optional<S> lookupSequence(final Schema schema, final String name) {
      return (Optional<S>) lookupDatabaseObject(sequencesBySchemaAndName, schema, name);
    }

    @Override
    public <S extends Synonym> Optional<S> lookupSynonym(final Schema schema, final String name) {
      return (Optional<S>) lookupDatabaseObject(synonymsBySchemaAndName, schema, name);
    }

    @Override
    public <C extends ColumnDataType> Optional<C> lookupSystemColumnDataType(final String name) {
      return (Optional<C>)
          lookupDatabaseObject(columnDataTypesBySchemaAndName, new SchemaReference(), name);
    }

    @Override
    public <T extends Table> Optional<T> lookupTable(final Schema schema, final String name) {
      return (Optional<T>) lookupDatabaseObject(tablesBySchemaAndName, schema, name);
    }

    @Override
    public String getFullName() {
      return source.getFullName();
    }

    @Override
    public String getName() {
      return source.getName();
    }

    @Override
    public int compareTo(final NamedObject other) {
      return source.compareTo(other);
    }

    @Override
    public NamedObjectKey key() {
      return source.key();
    }

    @Override
    public String getRemarks() {
      return source.getRemarks();
    }

    @Override
    public boolean hasRemarks() {
      return source.hasRemarks();
    }

    @Override
    public void setRemarks(final String remarks) {
      source.setRemarks(remarks);
    }

    @Override
    public <T> T getAttribute(final String name) {
      return source.getAttribute(name);
    }

    @Override
    public <T> T getAttribute(final String name, final T defaultValue) {
      return source.getAttribute(name, defaultValue);
    }

    @Override
    public java.util.Map<String, Object> getAttributes() {
      return source.getAttributes();
    }

    @Override
    public boolean hasAttribute(final String name) {
      return source.hasAttribute(name);
    }

    @Override
    public <T> Optional<T> lookupAttribute(final String name) {
      return source.lookupAttribute(name);
    }

    @Override
    public void removeAttribute(final String name) {
      source.removeAttribute(name);
    }

    @Override
    public <T> void setAttribute(final String name, final T value) {
      source.setAttribute(name, value);
    }
  }
}
