/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.namedobject;

import java.util.Collection;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.DependantObject;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import us.fatehi.utility.UtilityMarker;

/**
 * Creates named-object indexes with explicit membership and lookup policies.
 *
 * <p>Concurrent factories create empty, mutable indexes. Immutable factories copy membership but
 * share object references. Schema factories support blank full-name lookup and distinct structured
 * keys with the same displayed name. Routine factories enable overload-aware schema/name lookup.
 * Other database-object factories use exact-key schema/name lookup.
 */
@UtilityMarker
public final class NamedObjectIndexes {

  /**
   * Creates an empty concurrent database-object index with exact-key schema/name lookup.
   *
   * @param <D> Database-object type
   * @return A new mutable index
   */
  public static <D extends DatabaseObject>
      BuildableDatabaseObjectIndex<D> newBuildableDatabaseObjectIndex() {
    return new ConcurrentDatabaseObjectIndex<>();
  }

  /**
   * Creates an empty concurrent dependent-object index with exact parent/name lookup.
   *
   * @param <DO> Dependent-object type
   * @param <D> Parent database-object type
   * @return A new mutable index
   */
  public static <DO extends DependantObject<D>, D extends DatabaseObject>
      BuildableDependantObjectIndex<DO, D> newBuildableDependantObjectIndex() {
    return new ConcurrentDependantObjectIndex<>();
  }

  /**
   * Creates an empty concurrent named-object index.
   *
   * @param <N> Named-object type
   * @return A new mutable index
   */
  public static <N extends NamedObject>
      BuildableNamedObjectIndex<N> newBuildableNamedObjectIndex() {
    return new MapBackedNamedObjectIndex<>();
  }

  /**
   * Creates an empty concurrent routine index with overload-aware schema/name lookup.
   *
   * @param <R> Routine type
   * @return A new mutable index
   */
  public static <R extends Routine> BuildableDatabaseObjectIndex<R> newBuildableRoutineIndex() {
    return new ConcurrentDatabaseObjectIndex<>(true);
  }

  /**
   * Creates an empty concurrent schema index, including support for blank full-name lookup.
   *
   * @param <S> Schema type
   * @return A new mutable index
   */
  public static <S extends Schema> BuildableNamedObjectIndex<S> newBuildableSchemaIndex() {
    return new MapBackedNamedObjectIndex<>();
  }

  /**
   * Copies database objects into immutable membership with exact-key schema/name lookup.
   *
   * @param <D> Database-object type
   * @param values Objects to index; the first object for each complete key is retained
   * @return A new immutable index
   * @throws NullPointerException if the collection, an object, or its key is null
   */
  public static <D extends DatabaseObject> DatabaseObjectIndex<D> newImmutableDatabaseObjectIndex(
      final Collection<? extends D> values) {
    return new ImmutableDatabaseObjectIndex<>(values);
  }

  /**
   * Copies named objects into immutable membership.
   *
   * @param <N> Named-object type
   * @param values Objects to index; null creates empty membership, and the last object for each
   *     complete key is retained
   * @return A new immutable index
   * @throws NullPointerException if an object or its key is null
   */
  public static <N extends NamedObject> NamedObjectIndex<N> newImmutableNamedObjectIndex(
      final Collection<N> values) {
    return new MapBackedNamedObjectIndex<>(values);
  }

  /**
   * Copies routines into immutable membership with overload-aware schema/name lookup.
   *
   * @param <R> Routine type
   * @param values Routines to index; the first routine for each complete key is retained
   * @return A new immutable index
   * @throws NullPointerException if the collection, a routine, or its key is null
   */
  public static <R extends Routine> DatabaseObjectIndex<R> newImmutableRoutineIndex(
      final Collection<? extends R> values) {
    return new ImmutableDatabaseObjectIndex<>(values, true);
  }

  /**
   * Copies schemas into immutable membership, including support for blank full-name lookup.
   *
   * @param <S> Schema type
   * @param values Schemas to index; null creates empty membership, and the last schema for each
   *     complete key is retained
   * @return A new immutable index
   * @throws NullPointerException if a schema or its key is null
   */
  public static <S extends Schema> NamedObjectIndex<S> newImmutableSchemaIndex(
      final Collection<S> values) {
    return new MapBackedNamedObjectIndex<>(values);
  }

  private NamedObjectIndexes() {
    // Prevent instantiation
  }
}
