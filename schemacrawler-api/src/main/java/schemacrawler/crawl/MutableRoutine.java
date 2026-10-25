/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static us.fatehi.utility.Utility.isBlank;
import static us.fatehi.utility.Utility.requireNotBlank;

import java.io.Serial;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import schemacrawler.namedobject.NamedObjectComparators;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Routine;
import schemacrawler.schema.RoutineBodyType;
import schemacrawler.schema.RoutineType;
import schemacrawler.schema.Schema;

/** Represents a database routine. Created from metadata returned by a JDBC call. */
abstract sealed class MutableRoutine extends AbstractDatabaseObject implements Routine
    permits MutableFunction, MutableProcedure {

  @Serial private static final long serialVersionUID = 3906925686089134130L;

  private static final Comparator<Routine> ROUTINE_COMPARATOR =
      Comparator.comparing(
              (Routine routine) -> routine.getParameters(),
              NamedObjectComparators.ROUTINE_PARAMETER_LIST_COMPARATOR)
          .thenComparing(Routine::getSpecificName, nullsLast(naturalOrder()))
          .thenComparing(NamedObjectComparators.EXACT_KEY_COMPARATOR);

  private transient NamedObjectKey key;
  private final String specificName;
  private RoutineBodyType routineBodyType;
  private final Collection<DatabaseObject> referencedObjects;
  private String definition;

  /**
   * Effective Java - Item 17 - Minimize Mutability - Package-private constructors make a class
   * effectively final
   *
   * @param schema Schema of this object
   * @param name Name of the named object
   */
  MutableRoutine(final Schema schema, final String name, final String specificName) {
    super(schema, name);
    requireNotBlank(name, "No routine name provided");

    this.specificName = specificName;
    routineBodyType = RoutineBodyType.unknown;
    referencedObjects = new HashSet<>();
    definition = "";
  }

  /**
   * {@inheritDoc}
   *
   * <p>NOTE: compareTo is not compatible with equals. equals compares the full name of a database
   * object, but compareTo uses more fields to define a "natural" sorting order. compareTo may
   * return incorrect results until the object is fully built by SchemaCrawler.
   */
  @Override
  public int compareTo(final NamedObject obj) {
    final int comparison =
        NamedObjectComparators.DATABASE_OBJECT_FIELDS_COMPARATOR.compare(this, obj);
    if (comparison != 0) {
      return comparison;
    }
    if (obj instanceof final Routine other) {
      return ROUTINE_COMPARATOR.compare(this, other);
    }
    return NamedObjectComparators.EXACT_KEY_COMPARATOR.compare(this, obj);
  }

  /** {@inheritDoc} */
  @Override
  public final String getDefinition() {
    return definition;
  }

  @Override
  public Collection<DatabaseObject> getReferencedObjects() {
    return Set.copyOf(referencedObjects);
  }

  /** {@inheritDoc} */
  @Override
  public final RoutineBodyType getRoutineBodyType() {
    return routineBodyType;
  }

  @Override
  public RoutineType getRoutineType() {
    return null;
  }

  @Override
  public final String getSpecificName() {
    if (isBlank(specificName)) {
      return getName();
    }
    return specificName;
  }

  /** {@inheritDoc} */
  @Override
  public final RoutineType getType() {
    return getRoutineType();
  }

  @Override
  public final boolean hasDefinition() {
    return !isBlank(definition);
  }

  @Override
  public final NamedObjectKey key() {
    buildKey();
    return key;
  }

  final void addReferencedObject(final DatabaseObject referencedObject) {
    if (referencedObject != null) {
      referencedObjects.add(referencedObject);
    }
  }

  final void setDefinition(final String definition) {
    if (!hasDefinition() && !isBlank(definition)) {
      this.definition = definition;
    }
  }

  final void setRoutineBodyType(final RoutineBodyType routineBodyType) {
    this.routineBodyType = routineBodyType;
  }

  private void buildKey() {
    if (key != null) {
      return;
    }
    key = super.key().with(specificName);
  }
}
