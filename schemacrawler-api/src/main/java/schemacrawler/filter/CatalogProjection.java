/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static java.util.Objects.compare;
import static schemacrawler.utility.NamedObjectSort.alphabetical;
import static us.fatehi.utility.Utility.isBlank;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
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
import schemacrawler.schemacrawler.SchemaReference;

final class CatalogProjection implements Catalog {

  @Serial private static final long serialVersionUID = 1L;

  private static final String REMARKS_ATTRIBUTE = "REMARKS";
  private static final NamedObjectKey SYSTEM_SCHEMA_KEY = new SchemaReference().key();

  private static LinkedHashMap<NamedObjectKey, DatabaseUser> indexByKey(
      final Collection<DatabaseUser> values) {
    final LinkedHashMap<NamedObjectKey, DatabaseUser> index = new LinkedHashMap<>();
    values.forEach(databaseUser -> index.putIfAbsent(databaseUser.key(), databaseUser));
    return index;
  }

  private static <N extends DatabaseObject> LinkedHashMap<NamedObjectKey, N> indexBySchemaAndName(
      final Collection<N> values) {
    final LinkedHashMap<NamedObjectKey, N> index = new LinkedHashMap<>();
    values.forEach(
        value -> {
          final Schema schema = value.getSchema();
          if (schema != null) {
            index.putIfAbsent(schema.key().with(value.getName()), value);
          }
        });
    return index;
  }

  private static LinkedHashMap<NamedObjectKey, Routine> indexRoutines(
      final Collection<Routine> values) {
    final LinkedHashMap<NamedObjectKey, Routine> index = new LinkedHashMap<>();
    values.forEach(routine -> index.putIfAbsent(routine.key(), routine));
    return index;
  }

  private static LinkedHashMap<String, Schema> indexSchemas(final Collection<Schema> values) {
    final LinkedHashMap<String, Schema> index = new LinkedHashMap<>();
    values.forEach(schema -> index.putIfAbsent(schema.getFullName(), schema));
    return index;
  }

  private static <N extends DatabaseObject> Optional<N> lookupBySchemaAndName(
      final Map<NamedObjectKey, N> index, final Schema schema, final String name) {
    if (schema == null || name == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(index.get(schema.key().with(name)));
  }

  private static <N extends DatabaseObject> List<N> valuesForSchema(
      final Map<NamedObjectKey, N> index, final Schema schema) {
    if (schema == null) {
      return List.of();
    }
    return index.values().stream()
        .filter(value -> value.getSchema() != null && schema.key().equals(value.getSchema().key()))
        .toList();
  }

  private final CrawlInfo crawlInfo;
  private final DatabaseInfo databaseInfo;
  private final JdbcDriverInfo jdbcDriverInfo;
  private final String remarks;
  private final Map<String, Object> attributes;
  private final LinkedHashMap<String, Schema> schemas;
  private final LinkedHashMap<NamedObjectKey, Table> tables;
  private final LinkedHashMap<NamedObjectKey, Routine> routines;
  private final LinkedHashMap<NamedObjectKey, Sequence> sequences;
  private final LinkedHashMap<NamedObjectKey, Synonym> synonyms;
  private final LinkedHashMap<NamedObjectKey, ColumnDataType> columnDataTypes;
  private final LinkedHashMap<NamedObjectKey, DatabaseUser> databaseUsers;

  CatalogProjection(
      final CrawlInfo crawlInfo,
      final DatabaseInfo databaseInfo,
      final JdbcDriverInfo jdbcDriverInfo,
      final Map<String, Object> sourceAttributes,
      final Collection<Schema> schemas,
      final Collection<Table> tables,
      final Collection<Routine> routines,
      final Collection<Sequence> sequences,
      final Collection<Synonym> synonyms,
      final Collection<ColumnDataType> columnDataTypes,
      final Collection<DatabaseUser> databaseUsers) {
    this.crawlInfo = crawlInfo;
    this.databaseInfo = databaseInfo;
    this.jdbcDriverInfo = jdbcDriverInfo;
    final Object sourceRemarks =
        sourceAttributes == null ? null : sourceAttributes.get(REMARKS_ATTRIBUTE);
    remarks = sourceRemarks == null ? "" : String.valueOf(sourceRemarks);
    final Map<String, Object> attributesCopy = new TreeMap<>();
    if (sourceAttributes != null) {
      sourceAttributes.forEach(
          (attributeName, value) -> {
            if (attributeName != null
                && value != null
                && !REMARKS_ATTRIBUTE.equals(attributeName)) {
              attributesCopy.put(attributeName, value);
            }
          });
    }
    attributes = Map.copyOf(attributesCopy);
    this.schemas = indexSchemas(schemas);
    this.tables = indexBySchemaAndName(tables);
    this.routines = indexRoutines(routines);
    this.sequences = indexBySchemaAndName(sequences);
    this.synonyms = indexBySchemaAndName(synonyms);
    this.columnDataTypes = indexBySchemaAndName(columnDataTypes);
    this.databaseUsers = indexByKey(databaseUsers);
  }

  @Override
  public int compareTo(final NamedObject other) {
    return compare(this, other, alphabetical);
  }

  @Override
  public <T> T getAttribute(final String name) {
    return getAttribute(name, null);
  }

  @Override
  public <T> T getAttribute(final String name, final T defaultValue) {
    return (T) attributes.getOrDefault(name, defaultValue);
  }

  @Override
  public Map<String, Object> getAttributes() {
    return Collections.unmodifiableMap(new TreeMap<>(attributes));
  }

  @Override
  public Collection<ColumnDataType> getColumnDataTypes() {
    return List.copyOf(columnDataTypes.values());
  }

  @Override
  public Collection<ColumnDataType> getColumnDataTypes(final Schema schema) {
    return valuesForSchema(columnDataTypes, schema);
  }

  @Override
  public CrawlInfo getCrawlInfo() {
    return crawlInfo;
  }

  @Override
  public DatabaseInfo getDatabaseInfo() {
    return databaseInfo;
  }

  @Override
  public Collection<DatabaseUser> getDatabaseUsers() {
    return List.copyOf(databaseUsers.values());
  }

  @Override
  public String getFullName() {
    return getName();
  }

  @Override
  public JdbcDriverInfo getJdbcDriverInfo() {
    return jdbcDriverInfo;
  }

  @Override
  public String getName() {
    return "catalog-projection";
  }

  @Override
  public String getRemarks() {
    return remarks;
  }

  @Override
  public Collection<Routine> getRoutines() {
    return List.copyOf(routines.values());
  }

  @Override
  public Collection<Routine> getRoutines(final Schema schema) {
    return getRoutines(schema, null);
  }

  @Override
  public Collection<Routine> getRoutines(final Schema schema, final String routineName) {
    return routines.values().stream()
        .filter(
            routine ->
                schema != null
                    && routine.getSchema() != null
                    && schema.key().equals(routine.getSchema().key()))
        .filter(routine -> isBlank(routineName) || routineName.equals(routine.getName()))
        .toList();
  }

  @Override
  public Collection<Schema> getSchemas() {
    return List.copyOf(schemas.values());
  }

  @Override
  public Collection<Sequence> getSequences() {
    return List.copyOf(sequences.values());
  }

  @Override
  public Collection<Sequence> getSequences(final Schema schema) {
    return valuesForSchema(sequences, schema);
  }

  @Override
  public Collection<Synonym> getSynonyms() {
    return List.copyOf(synonyms.values());
  }

  @Override
  public Collection<Synonym> getSynonyms(final Schema schema) {
    return valuesForSchema(synonyms, schema);
  }

  @Override
  public Collection<ColumnDataType> getSystemColumnDataTypes() {
    return columnDataTypes.values().stream()
        .filter(
            columnDataType ->
                columnDataType.getSchema() != null
                    && SYSTEM_SCHEMA_KEY.equals(columnDataType.getSchema().key()))
        .toList();
  }

  @Override
  public Collection<Table> getTables() {
    return List.copyOf(tables.values());
  }

  @Override
  public Collection<Table> getTables(final Schema schema) {
    return valuesForSchema(tables, schema);
  }

  @Override
  public boolean hasAttribute(final String name) {
    return attributes.containsKey(name);
  }

  @Override
  public boolean hasRemarks() {
    return !isBlank(remarks);
  }

  @Override
  public NamedObjectKey key() {
    return new NamedObjectKey();
  }

  @Override
  public <T> Optional<T> lookupAttribute(final String name) {
    if (name == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(getAttribute(name));
  }

  @Override
  public Optional<Column> lookupColumn(
      final Schema schema, final String tableName, final String name) {
    return lookupBySchemaAndName(tables, schema, tableName)
        .flatMap(table -> table.lookupColumn(name));
  }

  @Override
  public <C extends ColumnDataType> Optional<C> lookupColumnDataType(
      final Schema schema, final String dataTypeName) {
    return (Optional<C>) lookupBySchemaAndName(columnDataTypes, schema, dataTypeName);
  }

  @Override
  public <R extends Routine> Optional<R> lookupRoutine(final Schema schema, final String name) {
    return (Optional<R>) getRoutines(schema, name).stream().findFirst();
  }

  @Override
  public <S extends Schema> Optional<S> lookupSchema(final String name) {
    return (Optional<S>) Optional.ofNullable(schemas.get(name));
  }

  @Override
  public <S extends Sequence> Optional<S> lookupSequence(final Schema schema, final String name) {
    return (Optional<S>) lookupBySchemaAndName(sequences, schema, name);
  }

  @Override
  public <S extends Synonym> Optional<S> lookupSynonym(final Schema schema, final String name) {
    return (Optional<S>) lookupBySchemaAndName(synonyms, schema, name);
  }

  @Override
  public <C extends ColumnDataType> Optional<C> lookupSystemColumnDataType(final String name) {
    return (Optional<C>) lookupBySchemaAndName(columnDataTypes, new SchemaReference(), name);
  }

  @Override
  public <T extends Table> Optional<T> lookupTable(final Schema schema, final String name) {
    return (Optional<T>) lookupBySchemaAndName(tables, schema, name);
  }

  @Override
  public void removeAttribute(final String name) {
    throw new UnsupportedOperationException("Catalog projection attributes are immutable");
  }

  @Override
  public <T> void setAttribute(final String name, final T value) {
    throw new UnsupportedOperationException("Catalog projection attributes are immutable");
  }

  @Override
  public void setRemarks(final String remarks) {
    throw new UnsupportedOperationException("Catalog projection remarks are immutable");
  }
}
