/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static us.fatehi.utility.Utility.isBlank;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import schemacrawler.namedobject.AbstractNamedObjectWithAttributes;
import schemacrawler.namedobject.ImmutableNamedObjectIndex;
import schemacrawler.namedobject.MapBackedNamedObjectIndex;
import schemacrawler.namedobject.NamedObjectIndex;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.CrawlInfo;
import schemacrawler.schema.DatabaseInfo;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.DatabaseUser;
import schemacrawler.schema.JdbcDriverInfo;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaReference;

/** Fixed membership with independently mutable, shallowly copied catalog attributes. */
final class CatalogProjection extends AbstractNamedObjectWithAttributes implements Catalog {

  @Serial private static final long serialVersionUID = -8541112200327727237L;

  private final CrawlInfo crawlInfo;
  private final DatabaseInfo databaseInfo;
  private final JdbcDriverInfo jdbcDriverInfo;
  private final NamedObjectIndex<DatabaseUser> databaseUsers;
  private final NamedObjectIndex<Schema> schemas;
  private final ImmutableNamedObjectIndex<ColumnDataType> columnDataTypes;
  private final ImmutableNamedObjectIndex<Table> tables;
  private final ImmutableNamedObjectIndex<Routine> routines;
  private final ImmutableNamedObjectIndex<Sequence> sequences;
  private final ImmutableNamedObjectIndex<Synonym> synonyms;

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
    super("");
    this.crawlInfo = crawlInfo;
    this.databaseInfo = databaseInfo;
    this.jdbcDriverInfo = jdbcDriverInfo;
    addAttributes(attributesMap);
    this.databaseUsers = new MapBackedNamedObjectIndex<>(databaseUsers);
    this.schemas = new MapBackedNamedObjectIndex<>(schemas);
    this.tables = new ImmutableNamedObjectIndex<>(tables);
    this.routines = new ImmutableNamedObjectIndex<>(routines);
    this.sequences = new ImmutableNamedObjectIndex<>(sequences);
    this.synonyms = new ImmutableNamedObjectIndex<>(synonyms);
    this.columnDataTypes = new ImmutableNamedObjectIndex<>(columnDataTypes);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj;
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

  /** {@inheritDoc} */
  @Override
  public JdbcDriverInfo getJdbcDriverInfo() {
    return jdbcDriverInfo;
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
  public int hashCode() {
    return System.identityHashCode(this);
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

  /**
   * Looks up a schema by its complete displayed name.
   *
   * <p>When multiple schemas have the same displayed name, the matching schema with the minimum
   * structured key is returned.
   *
   * @param name Complete displayed schema name
   * @return The matching schema, if present
   */
  @Override
  public Optional<Schema> lookupSchema(final String name) {
    // Schema names may be blank, but cannot be null
    if (name == null) {
      return Optional.empty();
    }
    // Schemas need to be looked up by full name in sorted order
    // Schema full names can be the same, since either the
    // catalog or schema may be null, depending on the database
    final Collection<Schema> sortedSchemas = getSchemas();
    for (final Schema schema : sortedSchemas) {
      if (name.equals(schema.getFullName())) {
        // Match the first schema found, searching in sorted order
        return Optional.of(schema);
      }
    }
    return Optional.empty();
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
}
