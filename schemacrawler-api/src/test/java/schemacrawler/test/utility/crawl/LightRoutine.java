/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.ProcedureReturnType;
import schemacrawler.schema.Routine;
import schemacrawler.schema.RoutineBodyType;
import schemacrawler.schema.RoutineParameter;
import schemacrawler.schema.RoutineReturnType;
import schemacrawler.schema.RoutineType;
import schemacrawler.schema.Schema;

public class LightRoutine extends AbstractLightDatabaseObject implements Routine {

  @Serial private static final long serialVersionUID = 1L;

  public LightRoutine(final Schema schema, final String name) {
    super(schema, name);
  }

  @Override
  public String getDefinition() {
    return "";
  }

  @Override
  public <C extends RoutineParameter<? extends Routine>> List<C> getParameters() {
    return Collections.emptyList();
  }

  @Override
  public Collection<? extends DatabaseObject> getReferencedObjects() {
    return Collections.emptyList();
  }

  @Override
  public RoutineReturnType getReturnType() {
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
    return false;
  }

  @Override
  public <C extends RoutineParameter<? extends Routine>> Optional<C> lookupParameter(
      final String name) {
    return Optional.empty();
  }
}
