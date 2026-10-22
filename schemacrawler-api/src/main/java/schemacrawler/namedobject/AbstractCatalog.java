/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.DatabaseUser;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;

/**
 * Shared catalog reads over caller-owned indexes, with reference identity and mutable attributes.
 *
 * <p>The indexes determine membership and lookup semantics. This base neither mutates membership
 * nor copies its elements. Subclasses supply metadata and the schema used for system data types.
 *
 * @param <D> Column data-type type
 * @param <T> Table type
 * @param <R> Routine type
 * @param <S> Sequence type
 * @param <Y> Synonym type
 */
public abstract class AbstractCatalog<
        D extends ColumnDataType,
        T extends Table,
        R extends Routine,
        S extends Sequence,
        Y extends Synonym>
    extends AbstractNamedObjectWithAttributes implements Catalog {

  @Serial private static final long serialVersionUID = 7179487369193613934L;

  private final NamedObjectIndex<DatabaseUser> databaseUsers;
  private final Schema systemSchema;
  private final NamedObjectIndex<Schema> schemas;
  private final DatabaseObjectIndex<D> columnDataTypes;
  private final DatabaseObjectIndex<T> tables;
  private final DatabaseObjectIndex<R> routines;
  private final DatabaseObjectIndex<S> sequences;
  private final DatabaseObjectIndex<Y> synonyms;

  /**
   * Creates a blank-named catalog backed by the supplied indexes.
   *
   * @param systemSchema Schema identifying system data types
   * @param databaseUsers Database-user index
   * @param schemas Schema index
   * @param columnDataTypes Column data-type index
   * @param tables Table index
   * @param routines Routine index
   * @param sequences Sequence index
   * @param synonyms Synonym index
   * @throws NullPointerException if the system schema or any index is null
   */
  protected AbstractCatalog(
      final Schema systemSchema,
      final NamedObjectIndex<DatabaseUser> databaseUsers,
      final NamedObjectIndex<Schema> schemas,
      final DatabaseObjectIndex<D> columnDataTypes,
      final DatabaseObjectIndex<T> tables,
      final DatabaseObjectIndex<R> routines,
      final DatabaseObjectIndex<S> sequences,
      final DatabaseObjectIndex<Y> synonyms) {
    super("");
    this.systemSchema = requireNonNull(systemSchema, "No system schema provided");
    this.databaseUsers = requireNonNull(databaseUsers, "No database-user index provided");
    this.schemas = requireNonNull(schemas, "No schema index provided");
    this.columnDataTypes = requireNonNull(columnDataTypes, "No column data-type index provided");
    this.tables = requireNonNull(tables, "No table index provided");
    this.routines = requireNonNull(routines, "No routine index provided");
    this.sequences = requireNonNull(sequences, "No sequence index provided");
    this.synonyms = requireNonNull(synonyms, "No synonym index provided");
  }

  /** {@inheritDoc} */
  @Override
  public final boolean equals(final Object obj) {
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

  /** {@inheritDoc} */
  @Override
  public Collection<DatabaseUser> getDatabaseUsers() {
    return List.copyOf(databaseUsers.getObjects());
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
    return getColumnDataTypes(systemSchema);
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

  /** {@inheritDoc} */
  @Override
  public final int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public boolean hasRoutines() {
    return !routines.isEmpty();
  }

  @Override
  public boolean hasRoutines(final Schema schema) {
    return routines.hasObjects(schema);
  }

  @Override
  public boolean hasSchemas() {
    return !schemas.isEmpty();
  }

  @Override
  public boolean hasSequences() {
    return !sequences.isEmpty();
  }

  @Override
  public boolean hasSequences(final Schema schema) {
    return sequences.hasObjects(schema);
  }

  @Override
  public boolean hasSynonyms() {
    return !synonyms.isEmpty();
  }

  @Override
  public boolean hasSynonyms(final Schema schema) {
    return synonyms.hasObjects(schema);
  }

  @Override
  public boolean hasTables() {
    return !tables.isEmpty();
  }

  @Override
  public boolean hasTables(final Schema schema) {
    return tables.hasObjects(schema);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Column> lookupColumn(
      final Schema schema, final String tableName, final String name) {
    return tables.lookup(schema, tableName).flatMap(table -> table.lookupColumn(name));
  }

  /** {@inheritDoc} */
  @Override
  public Optional<D> lookupColumnDataType(final Schema schema, final String name) {
    return columnDataTypes.lookup(schema, name);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<R> lookupRoutine(final Schema schema, final String name) {
    return routines.lookup(schema, name);
  }

  /**
   * Looks up a schema by its complete displayed name.
   *
   * <p>If displayed names collide, selects the natural-order first schema using its structured key,
   * as specified by the schema index.
   *
   * @param name Complete displayed schema name
   * @return The matching schema, if present
   */
  @Override
  public Optional<Schema> lookupSchema(final String name) {
    return schemas.lookup(name);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<S> lookupSequence(final Schema schema, final String name) {
    return sequences.lookup(schema, name);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Y> lookupSynonym(final Schema schema, final String name) {
    return synonyms.lookup(schema, name);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<D> lookupSystemColumnDataType(final String name) {
    return lookupColumnDataType(systemSchema, name);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<T> lookupTable(final Schema schema, final String name) {
    return tables.lookup(schema, name);
  }
}
