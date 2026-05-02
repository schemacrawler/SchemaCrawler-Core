/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import java.io.Serial;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.Identifiers;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.ParameterModeType;
import schemacrawler.schema.Routine;
import schemacrawler.schema.RoutineParameter;
import schemacrawler.schema.Schema;

public final class LightRoutineParameter extends AbstractLightDatabaseObject
    implements RoutineParameter<Routine> {

  @Serial private static final long serialVersionUID = 1L;

  private final Routine routine;

  public LightRoutineParameter(final Routine routine, final String name) {
    super(routine.getSchema(), name);
    this.routine = routine;
  }

  @Override
  public int compareTo(final NamedObject o) {
    return 0;
  }

  @Override
  public ColumnDataType getColumnDataType() {
    return null;
  }

  @Override
  public int getDecimalDigits() {
    return 0;
  }

  @Override
  public String getFullName() {
    return routine.getFullName() + "." + getName();
  }

  @Override
  public int getOrdinalPosition() {
    return 0;
  }

  @Override
  public ParameterModeType getParameterMode() {
    return null;
  }

  @Override
  public Routine getParent() {
    return routine;
  }

  @Override
  public int getPrecision() {
    return 0;
  }

  @Override
  public Schema getSchema() {
    return routine.getSchema();
  }

  @Override
  public String getShortName() {
    return getName();
  }

  @Override
  public int getSize() {
    return 0;
  }

  @Override
  public ColumnDataType getType() {
    return null;
  }

  @Override
  public String getWidth() {
    return "";
  }

  @Override
  public boolean isColumnDataTypeKnown() {
    return false;
  }

  @Override
  public boolean isNullable() {
    return false;
  }

  @Override
  public boolean isParentPartial() {
    return false;
  }

  @Override
  public NamedObjectKey key() {
    return routine.key().with(getName());
  }

  @Override
  public void withQuoting(final Identifiers identifiers) {
    // No-op
  }
}
