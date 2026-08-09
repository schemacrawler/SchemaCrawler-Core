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
import schemacrawler.schema.Procedure;
import schemacrawler.schema.ProcedureParameter;
import schemacrawler.schema.Schema;

public final class LightProcedureParameter extends AbstractLightDatabaseObject
    implements ProcedureParameter {

  @Serial private static final long serialVersionUID = 1L;

  private final Procedure procedure;

  public LightProcedureParameter(final Procedure procedure, final String name) {
    super(procedure.getSchema(), name);
    this.procedure = procedure;
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
    return procedure.getFullName() + "." + getName();
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
  public Procedure getParent() {
    return procedure;
  }

  @Override
  public int getPrecision() {
    return 0;
  }

  @Override
  public Schema getSchema() {
    return procedure.getSchema();
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
    return procedure.key().with(getName());
  }

  @Override
  public void withQuoting(final Identifiers identifiers) {
    // No-op
  }
}
