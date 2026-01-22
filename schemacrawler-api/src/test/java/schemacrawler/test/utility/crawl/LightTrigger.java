/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import java.util.Set;
import schemacrawler.schema.ActionOrientationType;
import schemacrawler.schema.ConditionTimingType;
import schemacrawler.schema.EventManipulationType;
import schemacrawler.schema.Identifiers;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.Trigger;

public class LightTrigger extends AbstractLightNamedObject implements Trigger {

  @Serial private static final long serialVersionUID = -2552665161195438344L;

  private final Schema schema;
  private final String name;
  private final Table table;
  private String actionStatement;

  public LightTrigger(final Table table, final String name) {
    this.table = requireNonNull(table, "No table provided");
    schema = table.getSchema();
    this.name = name;
  }

  @Override
  public int compareTo(NamedObject o) {
    return 0;
  }

  @Override
  public String getActionCondition() {
    return null;
  }

  @Override
  public int getActionOrder() {
    return 0;
  }

  @Override
  public ActionOrientationType getActionOrientation() {
    return null;
  }

  @Override
  public String getActionStatement() {
    return actionStatement;
  }

  @Override
  public ConditionTimingType getConditionTiming() {
    return null;
  }

  @Override
  public Set<EventManipulationType> getEventManipulationTypes() {
    return null;
  }

  @Override
  public String getFullName() {
    return name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Table getParent() {
    return table;
  }

  @Override
  public Schema getSchema() {
    return schema;
  }

  @Override
  public String getShortName() {
    return name;
  }

  @Override
  public boolean isParentPartial() {
    return true;
  }

  @Override
  public NamedObjectKey key() {
    return null;
  }

  public void setActionStatement(String actionStatement) {
    this.actionStatement = actionStatement;
  }

  @Override
  public void withQuoting(Identifiers identifiers) {}
}
