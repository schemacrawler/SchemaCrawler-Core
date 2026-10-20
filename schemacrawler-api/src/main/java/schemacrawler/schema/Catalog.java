/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schema;

import java.util.Collection;
import java.util.Optional;

/**
 * Database and connection information.
 *
 * <p>Catalogs have reference equality and use {@link System#identityHashCode(Object)} for hashing,
 * independently of their metadata, members, or attributes. Names and full names are blank; natural
 * comparison is not an identity test. Identity belongs to each instance, including a deserialized
 * instance.
 *
 * <p>Lookup result subtypes are determined by the catalog implementation, not selected by callers.
 * Callers using this interface receive bounded-wildcard optionals; implementations may specialize
 * their return types.
 */
public interface Catalog extends NamedObject, AttributedObject, DescribedObject {

  /**
   * Gets the column data types
   *
   * @return Column data types
   */
  Collection<ColumnDataType> getColumnDataTypes();

  /**
   * Gets the column data types defined in the schema, by name.
   *
   * @return Column data types
   */
  Collection<ColumnDataType> getColumnDataTypes(Schema schema);

  CrawlInfo getCrawlInfo();

  DatabaseInfo getDatabaseInfo();

  /**
   * Gets the database users
   *
   * @return Database users
   */
  Collection<DatabaseUser> getDatabaseUsers();

  JdbcDriverInfo getJdbcDriverInfo();

  /**
   * Gets the routine.
   *
   * @return Routines
   */
  Collection<Routine> getRoutines();

  /**
   * Gets the routine.
   *
   * @return Routines
   */
  Collection<Routine> getRoutines(Schema schema);

  /**
   * Gets a routine by unqualified name.
   *
   * @param schema Schema
   * @param routineName Unqualified routine name
   * @return Routine.
   */
  Collection<Routine> getRoutines(Schema schema, String routineName);

  /**
   * Gets the schemas.
   *
   * @return Schemas
   */
  Collection<Schema> getSchemas();

  /**
   * Gets the sequences.
   *
   * @return Sequences
   */
  Collection<Sequence> getSequences();

  /**
   * Gets the sequences.
   *
   * @return Sequences
   */
  Collection<Sequence> getSequences(Schema schema);

  /**
   * Gets the synonyms.
   *
   * @return Synonyms
   */
  Collection<Synonym> getSynonyms();

  /**
   * Gets the synonyms.
   *
   * @return Synonyms
   */
  Collection<Synonym> getSynonyms(Schema schema);

  /**
   * Gets the column data types defined by the RDBMS system.
   *
   * @return Column data types
   */
  Collection<ColumnDataType> getSystemColumnDataTypes();

  /**
   * Gets the tables.
   *
   * @return Tables
   */
  Collection<Table> getTables();

  /**
   * Gets the tables.
   *
   * @return Tables
   */
  Collection<Table> getTables(Schema schema);

  /**
   * Gets a table column by unqualified name.
   *
   * @param schema Schema
   * @param tableName Unqualified table name
   * @param name Unqualified column name
   * @return Column.
   */
  Optional<Column> lookupColumn(Schema schema, String tableName, String name);

  /**
   * Gets the column data types defined in the schema, by name.
   *
   * @param schema Schema
   * @param dataTypeName Unqualified column data-type name
   * @return Column data type
   */
  Optional<? extends ColumnDataType> lookupColumnDataType(Schema schema, String dataTypeName);

  /**
   * Gets a routine by unqualified name.
   *
   * @param schema Schema
   * @param routineName Unqualified routine name
   * @return Routine.
   */
  Optional<? extends Routine> lookupRoutine(Schema schema, String routineName);

  /**
   * Gets a schema by name.
   *
   * @param name Schema name
   * @return Schema.
   */
  Optional<? extends Schema> lookupSchema(String name);

  /**
   * Gets the sequence by unqualified name.
   *
   * @param schema Schema
   * @param sequenceName Unqualified sequence name
   * @return Sequence.
   */
  Optional<? extends Sequence> lookupSequence(Schema schema, String sequenceName);

  /**
   * Gets the synonym by unqualified name.
   *
   * @param schema Schema
   * @param synonymName Unqualified synonym name
   * @return Synonym.
   */
  Optional<? extends Synonym> lookupSynonym(Schema schema, String synonymName);

  /**
   * Gets the column data types defined by the RDBMS system, by name.
   *
   * @param name Column data type name
   * @return Column data type
   */
  Optional<? extends ColumnDataType> lookupSystemColumnDataType(String name);

  /**
   * Gets a table by unqualified name.
   *
   * @param schema Schema
   * @param tableName Unqualified table name
   * @return Table.
   */
  Optional<? extends Table> lookupTable(Schema schema, String tableName);
}
