/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import schemacrawler.namedobject.AbstractNamedObjectWithAttributes;
import schemacrawler.namedobject.MapBackedNamedObjectIndex;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.CrawlInfo;
import schemacrawler.schema.DatabaseUser;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaReference;
import us.fatehi.utility.jdbc.serverfingerprint.DatabaseServerFingerprint;
import us.fatehi.utility.jdbc.serverfingerprint.DatabaseServerFingerprintBuilder;

/**
 * Database and connection information. Created from metadata returned by a JDBC call, and other
 * sources of information.
 */
final class MutableCatalog extends AbstractNamedObjectWithAttributes implements Catalog {

  @Serial private static final long serialVersionUID = 4051323422934251828L;

  private final ImmutableCrawlInfo crawlInfo;
  private final MutableDatabaseInfo databaseInfo;
  private final MutableJdbcDriverInfo jdbcDriverInfo;
  private final MapBackedNamedObjectIndex<DatabaseUser> databaseUsers;
  private final MapBackedNamedObjectIndex<Schema> schemas;
  private final NamedObjectList<MutableColumnDataType> columnDataTypes;
  private final NamedObjectList<MutableTable> tables;
  private final NamedObjectList<MutableRoutine> routines;
  private final NamedObjectList<MutableSequence> sequences;
  private final NamedObjectList<MutableSynonym> synonyms;

  MutableCatalog(
      final String title,
      final MutableDatabaseInfo databaseInfo,
      final MutableJdbcDriverInfo jdbcDriverInfo) {
    super("");

    this.databaseInfo = requireNonNull(databaseInfo, "No database information provided");
    this.jdbcDriverInfo = requireNonNull(jdbcDriverInfo, "No JDBC driver information provided");
    final DatabaseServerFingerprint databaseServerFingerprint =
        DatabaseServerFingerprintBuilder.builder(jdbcDriverInfo.getConnectionUrl())
            .withDatabaseProductVersion(databaseInfo)
            .build();
    crawlInfo =
        new ImmutableCrawlInfo(title, databaseInfo, jdbcDriverInfo, databaseServerFingerprint);

    databaseUsers = new MapBackedNamedObjectIndex<>();
    schemas = new MapBackedNamedObjectIndex<>();
    columnDataTypes = new NamedObjectList<>();
    tables = new NamedObjectList<>();
    routines = new NamedObjectList<>();
    sequences = new NamedObjectList<>();
    synonyms = new NamedObjectList<>();
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
  public MutableDatabaseInfo getDatabaseInfo() {
    return databaseInfo;
  }

  /** {@inheritDoc} */
  @Override
  public Collection<DatabaseUser> getDatabaseUsers() {
    return List.copyOf(databaseUsers.getObjects());
  }

  /** {@inheritDoc} */
  @Override
  public MutableJdbcDriverInfo getJdbcDriverInfo() {
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
    return List.copyOf(routines.getObjects(schema, routineName));
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

  /** {@inheritDoc} */
  @Override
  public Optional<Column> lookupColumn(
      final Schema schema, final String tableName, final String name) {
    return tables.lookup(schema, tableName).flatMap(table -> table.lookupColumn(name));
  }

  /** {@inheritDoc} */
  @Override
  public Optional<MutableColumnDataType> lookupColumnDataType(
      final Schema schema, final String name) {
    return columnDataTypes.lookup(schema, name);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<MutableRoutine> lookupRoutine(final Schema schema, final String name) {
    return routines.lookup(schema, name);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Schema> lookupSchema(final String name) {
    return schemas.lookup(name);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<MutableSequence> lookupSequence(final Schema schemaRef, final String name) {
    return sequences.lookup(schemaRef, name);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<MutableSynonym> lookupSynonym(final Schema schemaRef, final String name) {
    return synonyms.lookup(schemaRef, name);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<MutableColumnDataType> lookupSystemColumnDataType(final String name) {
    return lookupColumnDataType(new SchemaReference(), name);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<MutableTable> lookupTable(final Schema schemaRef, final String name) {
    return tables.lookup(schemaRef, name);
  }

  void addColumnDataType(final MutableColumnDataType columnDataType) {
    columnDataTypes.add(columnDataType);
  }

  void addDatabaseUser(final ImmutableDatabaseUser databaseUser) {
    databaseUsers.add(databaseUser);
  }

  void addRoutine(final MutableRoutine routine) {
    routines.add(routine);
  }

  Schema addSchema(final Schema schema) {
    schemas.add(schema);
    return schema;
  }

  void addSequence(final MutableSequence sequence) {
    sequences.add(sequence);
  }

  void addSynonym(final MutableSynonym synonym) {
    synonyms.add(synonym);
  }

  void addTable(final MutableTable table) {
    tables.add(table);
  }

  NamedObjectList<MutableRoutine> getAllRoutines() {
    return routines;
  }

  NamedObjectList<MutableTable> getAllTables() {
    return tables;
  }

  Optional<MutableRoutine> lookupRoutine(final NamedObjectKey routineLookupKey) {
    return routines.lookup(routineLookupKey);
  }

  Optional<MutableTable> lookupTable(final NamedObjectKey tableLookupKey) {
    return tables.lookup(tableLookupKey);
  }
}
