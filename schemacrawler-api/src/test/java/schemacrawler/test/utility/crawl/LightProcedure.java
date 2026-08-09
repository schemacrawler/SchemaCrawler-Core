/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import static us.fatehi.utility.Utility.trimToEmpty;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.Procedure;
import schemacrawler.schema.ProcedureParameter;
import schemacrawler.schema.ProcedureReturnType;
import schemacrawler.schema.RoutineBodyType;
import schemacrawler.schema.RoutineType;
import schemacrawler.schema.Schema;
import schemacrawler.schemacrawler.SchemaReference;

public final class LightProcedure extends AbstractLightDatabaseObject implements Procedure {

  @Serial private static final long serialVersionUID = 1L;

  private final List<ProcedureParameter> parameters = new ArrayList<>();
  private String definition;

  public LightProcedure(final Schema schema, final String name) {
    super(schema, name);
  }

  public LightProcedure(final String name) {
    this(new SchemaReference(), name);
  }

  public void addParameter(final LightProcedureParameter parameter) {
    if (parameter != null) {
      parameters.add(parameter);
    }
  }

  @Override
  public String getDefinition() {
    return trimToEmpty(definition);
  }

  @Override
  public List<ProcedureParameter> getParameters() {
    return List.copyOf(parameters);
  }

  @Override
  public Collection<? extends DatabaseObject> getReferencedObjects() {
    return List.of();
  }

  @Override
  public ProcedureReturnType getReturnType() {
    return ProcedureReturnType.noResult;
  }

  @Override
  public RoutineBodyType getRoutineBodyType() {
    return RoutineBodyType.sql;
  }

  @Override
  public RoutineType getRoutineType() {
    return getType();
  }

  @Override
  public String getSpecificName() {
    return null;
  }

  @Override
  public RoutineType getType() {
    return RoutineType.procedure;
  }

  @Override
  public boolean hasDefinition() {
    return definition != null && !definition.isEmpty();
  }

  @Override
  public Optional<? extends ProcedureParameter> lookupParameter(final String name) {
    return Optional.empty();
  }

  public void setDefinition(final String definition) {
    this.definition = definition;
  }
}
