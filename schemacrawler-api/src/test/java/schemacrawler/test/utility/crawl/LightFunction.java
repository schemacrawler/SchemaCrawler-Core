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
import java.util.List;
import java.util.Optional;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.Function;
import schemacrawler.schema.FunctionParameter;
import schemacrawler.schema.FunctionReturnType;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.RoutineBodyType;
import schemacrawler.schema.RoutineType;
import schemacrawler.schema.Schema;
import schemacrawler.schemacrawler.SchemaReference;

/** A light function, which has a name and a specific name, and holds no parameters. */
public class LightFunction extends AbstractLightDatabaseObject implements Function {

  @Serial private static final long serialVersionUID = 1L;

  private final String specificName;

  public LightFunction(final Schema schema, final String name) {
    this(schema, name, name + "_specific");
  }

  public LightFunction(final Schema schema, final String name, final String specificName) {
    super(schema, name);
    this.specificName = specificName;
  }

  public LightFunction(final String name) {
    this(new SchemaReference(), name);
  }

  @Override
  public String getDefinition() {
    return "";
  }

  @Override
  public List<FunctionParameter> getParameters() {
    return List.of();
  }

  @Override
  public Collection<? extends DatabaseObject> getReferencedObjects() {
    return List.of();
  }

  @Override
  public FunctionReturnType getReturnType() {
    return FunctionReturnType.unknown;
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
    return specificName;
  }

  @Override
  public RoutineType getType() {
    return RoutineType.function;
  }

  @Override
  public boolean hasDefinition() {
    return false;
  }

  @Override
  public NamedObjectKey key() {
    return super.key().with(specificName);
  }

  @Override
  public Optional<? extends FunctionParameter> lookupParameter(final String name) {
    return Optional.empty();
  }
}
