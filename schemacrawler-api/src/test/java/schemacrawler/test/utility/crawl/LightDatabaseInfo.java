/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import schemacrawler.schema.DatabaseInfo;
import us.fatehi.utility.database.DatabaseInformation;
import us.fatehi.utility.property.Property;

public class LightDatabaseInfo implements DatabaseInfo {

  @Serial private static final long serialVersionUID = -7659631401272737847L;

  private DatabaseInformation databaseInformation;

  public LightDatabaseInfo() {
    databaseInformation = new DatabaseInformation("Mocked Database", "0.0.1", "");
  }

  @Override
  public String getDatabaseProductName() {
    return databaseInformation.getDatabaseProductName();
  }

  @Override
  public String getDatabaseProductVersion() {
    return databaseInformation.getDatabaseProductVersion();
  }

  @Override
  public final String getDescription() {
    return databaseInformation.getDescription();
  }

  @Override
  public final String getName() {
    return databaseInformation.getName();
  }

  @Override
  public String getProductName() {
    return databaseInformation.getProductName();
  }

  @Override
  public String getProductVersion() {
    return databaseInformation.getProductVersion();
  }

  @Override
  public Collection<Property> getProperties() {
    return Collections.emptySet();
  }

  @Override
  public Collection<Property> getServerInfo() {
    return Collections.emptySet();
  }

  @Override
  public String getUserName() {
    return databaseInformation.getUserName();
  }

  @Override
  public Serializable getValue() {
    return databaseInformation.getValue();
  }

  @Override
  public boolean hasDescription() {
    return databaseInformation.hasDescription();
  }

  @Override
  public boolean hasValue() {
    return databaseInformation.hasValue();
  }

  @Override
  public String toString() {
    return databaseInformation.toString();
  }
}
