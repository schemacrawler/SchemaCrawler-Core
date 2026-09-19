/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import java.io.Serial;
import java.util.Collection;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.PartialDatabaseObject;
import schemacrawler.schema.Routine;
import schemacrawler.schema.RoutineBodyType;
import schemacrawler.schema.RoutineType;
import schemacrawler.schema.Schema;
import schemacrawler.schemacrawler.exceptions.NotLoadedException;

abstract sealed class RoutinePartial extends AbstractDatabaseObject
    implements Routine, PartialDatabaseObject permits FunctionPartial, ProcedurePartial {

  @Serial private static final long serialVersionUID = 1508498300413360531L;

  private transient NamedObjectKey key;

  /**
   * Effective Java - Item 17 - Minimize Mutability - Package-private constructors make a class
   * effectively final
   *
   * @param schema Schema of this object
   * @param name Name of the named object
   */
  RoutinePartial(final Schema schema, final String name) {
    super(schema, name);
  }

  /** {@inheritDoc} */
  @Override
  public final String getDefinition() {
    throw new NotLoadedException(this);
  }

  /** {@inheritDoc} */
  @Override
  public Collection<? extends DatabaseObject> getReferencedObjects() {
    throw new NotLoadedException(this);
  }

  /** {@inheritDoc} */
  @Override
  public final RoutineBodyType getRoutineBodyType() {
    throw new NotLoadedException(this);
  }

  /** {@inheritDoc} */
  @Override
  public final String getSpecificName() {
    throw new NotLoadedException(this);
  }

  /** {@inheritDoc} */
  @Override
  public final RoutineType getType() {
    return getRoutineType();
  }

  /** {@inheritDoc} */
  @Override
  public final boolean hasDefinition() {
    throw new NotLoadedException(this);
  }

  /** {@inheritDoc} */
  @Override
  public NamedObjectKey key() {
    buildKey();
    return key;
  }

  /**
   * Appends a trailing {@code null} part, so this key has the same 4-part shape (catalog, schema,
   * name, specific-name-or-null) as {@link MutableRoutine}'s key, even though a partial routine
   * never has a real specific name to report.
   */
  private void buildKey() {
    if (key != null) {
      return;
    }
    key = super.key().with(/* no specific name available */ null);
  }
}
