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

  private final String name;
  private final String fullName;
  private final NamedObjectKey key;
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
      final String name,
      final String fullName,
      final NamedObjectKey key,
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
    this.name = name;
    this.fullName = fullName;
    this.key = key;
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
    this.attributes = Map.copyOf(attributesCopy);
    this.schemas = indexSchemas(schemas);
    this.tables = indexBySchemaAndName(tables);
    this.routines = indexRoutines(routines);
    this.sequences = indexBySchemaAndName(sequences);
    this.synonyms = indexBySchemaAndName(synonyms);
    this.columnDataTypes = indexBySchemaAndName(columnDataTypes);
    this.databaseUsers = indexByKey(databaseUsers);
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
  public JdbcDriverInfo getJdbcDriverInfo() {
    return jdbcDriverInfo;
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
        .filter(routine -> schema != null && schema.equals(routine.getSchema()))
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
  public String getFullName() {
    return fullName;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int compareTo(final NamedObject other) {
    return compare(this, other, alphabetical);
  }

  @Override
  public NamedObjectKey key() {
    return key;
  }

  @Override
  public String getRemarks() {
    return remarks;
  }

  @Override
  public boolean hasRemarks() {
    return !isBlank(remarks);
  }

  @Override
  public void setRemarks(final String remarks) {
    throw new UnsupportedOperationException("Catalog projection remarks are immutable");
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
  public boolean hasAttribute(final String name) {
    return attributes.containsKey(name);
  }

  @Override
  public <T> Optional<T> lookupAttribute(final String name) {
    if (name == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(getAttribute(name));
  }

  @Override
  public void removeAttribute(final String name) {
    throw new UnsupportedOperationException("Catalog projection attributes are immutable");
  }

  @Override
  public <T> void setAttribute(final String name, final T value) {
    throw new UnsupportedOperationException("Catalog projection attributes are immutable");
  }

  private static LinkedHashMap<String, Schema> indexSchemas(final Collection<Schema> values) {
    final Map<String, Schema> sorted = new TreeMap<>();
    values.forEach(schema -> sorted.putIfAbsent(schema.getFullName(), schema));
    return new LinkedHashMap<>(sorted);
  }

  private static <N extends DatabaseObject> LinkedHashMap<NamedObjectKey, N> indexBySchemaAndName(
      final Collection<N> values) {
    final Map<NamedObjectKey, N> sorted = new TreeMap<>();
    values.forEach(
        value -> {
          final Schema schema = value.getSchema();
          if (schema != null) {
            sorted.putIfAbsent(schema.key().with(value.getName()), value);
          }
        });
    return new LinkedHashMap<>(sorted);
  }

  private static LinkedHashMap<NamedObjectKey, Routine> indexRoutines(
      final Collection<Routine> values) {
    final Map<NamedObjectKey, Routine> sorted = new TreeMap<>();
    values.forEach(routine -> sorted.putIfAbsent(routine.key(), routine));
    return new LinkedHashMap<>(sorted);
  }

  private static LinkedHashMap<NamedObjectKey, DatabaseUser> indexByKey(
      final Collection<DatabaseUser> values) {
    final Map<NamedObjectKey, DatabaseUser> sorted = new TreeMap<>();
    values.forEach(databaseUser -> sorted.putIfAbsent(databaseUser.key(), databaseUser));
    return new LinkedHashMap<>(sorted);
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
    return index.values().stream().filter(value -> schema.equals(value.getSchema())).toList();
  }
}
