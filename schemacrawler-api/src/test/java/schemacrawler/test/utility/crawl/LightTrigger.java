/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import java.io.Serial;
import java.util.Set;
import schemacrawler.schema.ActionOrientationType;
import schemacrawler.schema.ConditionTimingType;
import schemacrawler.schema.EventManipulationType;
import schemacrawler.schema.Identifiers;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;
import schemacrawler.schema.Trigger;

public class LightTrigger extends AbstractLightDatabaseObject implements Trigger {

  @Serial private static final long serialVersionUID = -2552665161195438344L;

  private final Table table;
  private String actionStatement;

  public LightTrigger(final Table table, final String name) {
    super(table.getSchema(), name);
    this.table = table;
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
  public Table getParent() {
    return table;
  }

  @Override
  public String getShortName() {
    return getName();
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
