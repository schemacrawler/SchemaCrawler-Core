/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static java.util.Objects.compare;
import static java.util.Objects.requireNonNullElse;
import static schemacrawler.utility.NamedObjectSort.alphabetical;
import static us.fatehi.utility.Utility.isBlank;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
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
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.utility.ImmutableNamedObjectIndex;

final class CatalogProjection implements Catalog {

  @Serial private static final long serialVersionUID = -8541112200327727237L;

  private static final String REMARKS_ATTRIBUTE = "REMARKS";
  private static final NamedObjectKey SYSTEM_SCHEMA_KEY = new SchemaReference().key();

  private final CrawlInfo crawlInfo;
  private final DatabaseInfo databaseInfo;
  private final JdbcDriverInfo jdbcDriverInfo;
  private final Map<String, Object> attributesMap;
  private final ImmutableNamedObjectIndex<Schema> schemas;
  private final ImmutableNamedObjectIndex<Table> tables;
  private final ImmutableNamedObjectIndex<Routine> routines;
  private final ImmutableNamedObjectIndex<Sequence> sequences;
  private final ImmutableNamedObjectIndex<Synonym> synonyms;
  private final ImmutableNamedObjectIndex<ColumnDataType> columnDataTypes;
  private final ImmutableNamedObjectIndex<DatabaseUser> databaseUsers;

  CatalogProjection(
      final String name,
      final CrawlInfo crawlInfo,
      final DatabaseInfo databaseInfo,
      final JdbcDriverInfo jdbcDriverInfo,
      final Map<String, Object> attributesMap) {
    this(
        crawlInfo,
        databaseInfo,
        jdbcDriverInfo,
        attributesMap,
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of());
  }

  CatalogProjection(
      final CrawlInfo crawlInfo,
      final DatabaseInfo databaseInfo,
      final JdbcDriverInfo jdbcDriverInfo,
      final Map<String, Object> attributesMap,
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
    this.attributesMap = Map.copyOf(requireNonNullElse(attributesMap, Map.of()));
    this.schemas = new ImmutableNamedObjectIndex<>(schemas);
    this.tables = new ImmutableNamedObjectIndex<>(tables);
    this.routines = new ImmutableNamedObjectIndex<>(routines);
    this.sequences = new ImmutableNamedObjectIndex<>(sequences);
    this.synonyms = new ImmutableNamedObjectIndex<>(synonyms);
    this.columnDataTypes = new ImmutableNamedObjectIndex<>(columnDataTypes);
    this.databaseUsers = new ImmutableNamedObjectIndex<>(databaseUsers);
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
    return (T) attributesMap.getOrDefault(name, defaultValue);
  }

  @Override
  public Map<String, Object> getAttributes() {
    return Collections.unmodifiableMap(new TreeMap<>(attributesMap));
  }

  /** {@inheritDoc} */
  @Override
  public Collection<ColumnDataType> getColumnDataTypes() {
    return List.copyOf(columnDataTypes.getObjects());
  }

  /** {@inheritDoc} */
  @Override
  public Collection<ColumnDataType> getColumnDataTypes(final Schema schema) {
    return List.copyOf(columnDataTypes.getObjects(schema));
  }

  @Override
  public CrawlInfo getCrawlInfo() {
    return crawlInfo;
  }

  @Override
  public DatabaseInfo getDatabaseInfo() {
    return databaseInfo;
  }

  /** {@inheritDoc} */
  @Override
  public Collection<DatabaseUser> getDatabaseUsers() {
    return List.copyOf(databaseUsers.getObjects());
  }

  @Override
  public String getFullName() {
    return getName();
  }

  /** {@inheritDoc} */
  @Override
  public JdbcDriverInfo getJdbcDriverInfo() {
    return jdbcDriverInfo;
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public String getRemarks() {
    return getAttribute(REMARKS_ATTRIBUTE, "");
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Routine> getRoutines() {
    return List.copyOf(routines.getObjects());
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Routine> getRoutines(final Schema schema) {
    return List.copyOf(routines.getObjects(schema));
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Routine> getRoutines(final Schema schema, final String routineName) {
    Predicate<DatabaseObject> filter = routine -> true;
    if (!isBlank(routineName)) {
      filter = filter.and(routine -> routine.getName().equals(routineName));
    }
    final Collection<Routine> routines = new ArrayList<>();
    for (final Routine routine : getRoutines(schema)) {
      if (filter.test(routine)) {
        routines.add(routine);
      }
    }
    return routines;
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Schema> getSchemas() {
    return List.copyOf(schemas.getObjects());
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Sequence> getSequences() {
    return List.copyOf(sequences.getObjects());
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Sequence> getSequences(final Schema schema) {
    return List.copyOf(sequences.getObjects(schema));
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Synonym> getSynonyms() {
    return List.copyOf(synonyms.getObjects());
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Synonym> getSynonyms(final Schema schema) {
    return List.copyOf(synonyms.getObjects(schema));
  }

  /** {@inheritDoc} */
  @Override
  public Collection<ColumnDataType> getSystemColumnDataTypes() {
    return getColumnDataTypes(new SchemaReference());
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Table> getTables() {
    return List.copyOf(tables.getObjects());
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Table> getTables(final Schema schema) {
    return List.copyOf(tables.getObjects(schema));
  }

  @Override
  public boolean hasAttribute(final String name) {
    return attributesMap.containsKey(name);
  }

  @Override
  public boolean hasRemarks() {
    return !isBlank(getRemarks());
  }

  @Override
  public NamedObjectKey key() {
    return new NamedObjectKey(getName());
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
    return tables.lookup(schema, tableName).flatMap(table -> table.lookupColumn(name));
  }

  @Override
  public <C extends ColumnDataType> Optional<C> lookupColumnDataType(
      final Schema schema, final String dataTypeName) {
    return (Optional<C>) columnDataTypes.lookup(schema, dataTypeName);
  }

  @Override
  public <R extends Routine> Optional<R> lookupRoutine(final Schema schema, final String name) {
    return (Optional<R>) routines.lookup(schema, name);
  }

  @Override
  public <S extends Schema> Optional<S> lookupSchema(final String name) {
    if (name == null) {
      return Optional.empty();
    }
    return (Optional<S>)
        schemas.snapshot().stream().filter(schema -> name.equals(schema.getFullName())).findFirst();
  }

  @Override
  public <S extends Sequence> Optional<S> lookupSequence(final Schema schema, final String name) {
    return (Optional<S>) sequences.lookup(schema, name);
  }

  @Override
  public <S extends Synonym> Optional<S> lookupSynonym(final Schema schema, final String name) {
    return (Optional<S>) synonyms.lookup(schema, name);
  }

  @Override
  public <C extends ColumnDataType> Optional<C> lookupSystemColumnDataType(final String name) {
    return (Optional<C>) columnDataTypes.lookup(new SchemaReference(), name);
  }

  @Override
  public <T extends Table> Optional<T> lookupTable(final Schema schema, final String name) {
    return (Optional<T>) tables.lookup(schema, name);
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
