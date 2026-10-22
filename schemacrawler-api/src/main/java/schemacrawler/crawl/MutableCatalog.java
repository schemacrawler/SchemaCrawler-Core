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
import java.util.Optional;
import schemacrawler.namedobject.AbstractCatalog;
import schemacrawler.namedobject.BuildableDatabaseObjectIndex;
import schemacrawler.namedobject.BuildableNamedObjectIndex;
import schemacrawler.namedobject.NamedObjectIndex;
import schemacrawler.namedobject.NamedObjectIndexes;
import schemacrawler.schema.CrawlInfo;
import schemacrawler.schema.DatabaseUser;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;
import schemacrawler.schemacrawler.SchemaReference;
import us.fatehi.utility.jdbc.serverfingerprint.DatabaseServerFingerprint;
import us.fatehi.utility.jdbc.serverfingerprint.DatabaseServerFingerprintBuilder;

/**
 * Database and connection information. Created from metadata returned by a JDBC call, and other
 * sources of information.
 */
final class MutableCatalog
    extends AbstractCatalog<
        MutableColumnDataType, MutableTable, MutableRoutine, MutableSequence, MutableSynonym> {

  @Serial private static final long serialVersionUID = 1L;

  private final ImmutableCrawlInfo crawlInfo;
  private final MutableDatabaseInfo databaseInfo;
  private final MutableJdbcDriverInfo jdbcDriverInfo;
  private final BuildableNamedObjectIndex<DatabaseUser> databaseUsers;
  private final BuildableNamedObjectIndex<Schema> schemas;
  private final BuildableDatabaseObjectIndex<MutableColumnDataType> columnDataTypes;
  private final BuildableDatabaseObjectIndex<MutableTable> tables;
  private final BuildableDatabaseObjectIndex<MutableRoutine> routines;
  private final BuildableDatabaseObjectIndex<MutableSequence> sequences;
  private final BuildableDatabaseObjectIndex<MutableSynonym> synonyms;

  MutableCatalog(
      final String title,
      final MutableDatabaseInfo databaseInfo,
      final MutableJdbcDriverInfo jdbcDriverInfo) {
    this(
        title,
        databaseInfo,
        jdbcDriverInfo,
        NamedObjectIndexes.newBuildableNamedObjectIndex(),
        NamedObjectIndexes.newBuildableSchemaIndex(),
        NamedObjectIndexes.newBuildableDatabaseObjectIndex(),
        NamedObjectIndexes.newBuildableDatabaseObjectIndex(),
        NamedObjectIndexes.newBuildableRoutineIndex(),
        NamedObjectIndexes.newBuildableDatabaseObjectIndex(),
        NamedObjectIndexes.newBuildableDatabaseObjectIndex());
  }

  private MutableCatalog(
      final String title,
      final MutableDatabaseInfo databaseInfo,
      final MutableJdbcDriverInfo jdbcDriverInfo,
      final BuildableNamedObjectIndex<DatabaseUser> databaseUsers,
      final BuildableNamedObjectIndex<Schema> schemas,
      final BuildableDatabaseObjectIndex<MutableColumnDataType> columnDataTypes,
      final BuildableDatabaseObjectIndex<MutableTable> tables,
      final BuildableDatabaseObjectIndex<MutableRoutine> routines,
      final BuildableDatabaseObjectIndex<MutableSequence> sequences,
      final BuildableDatabaseObjectIndex<MutableSynonym> synonyms) {
    super(
        new SchemaReference(),
        databaseUsers,
        schemas,
        columnDataTypes,
        tables,
        routines,
        sequences,
        synonyms);
    this.databaseInfo = requireNonNull(databaseInfo, "No database information provided");
    this.jdbcDriverInfo = requireNonNull(jdbcDriverInfo, "No JDBC driver information provided");
    final DatabaseServerFingerprint databaseServerFingerprint =
        DatabaseServerFingerprintBuilder.builder(jdbcDriverInfo.getConnectionUrl())
            .withDatabaseProductVersion(databaseInfo)
            .build();
    crawlInfo =
        new ImmutableCrawlInfo(title, databaseInfo, jdbcDriverInfo, databaseServerFingerprint);

    this.databaseUsers = databaseUsers;
    this.schemas = schemas;
    this.columnDataTypes = columnDataTypes;
    this.tables = tables;
    this.routines = routines;
    this.sequences = sequences;
    this.synonyms = synonyms;
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
  public MutableJdbcDriverInfo getJdbcDriverInfo() {
    return jdbcDriverInfo;
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

  NamedObjectIndex<MutableRoutine> getAllRoutines() {
    return routines;
  }

  NamedObjectIndex<Schema> getAllSchemas() {
    return schemas;
  }

  NamedObjectIndex<MutableTable> getAllTables() {
    return tables;
  }

  Optional<MutableRoutine> lookupRoutine(final NamedObjectKey routineLookupKey) {
    return routines.lookup(routineLookupKey);
  }

  Optional<MutableTable> lookupTable(final NamedObjectKey tableLookupKey) {
    return tables.lookup(tableLookupKey);
  }
}
