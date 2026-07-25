/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.databaseconnector;

import java.util.Collection;

/** Groups database connectors that should be registered individually. */
public interface DatabaseConnectorBundle {

  /**
   * Gets the bundled database connectors.
   *
   * @return Database connectors
   */
  Collection<DatabaseConnector> getDatabaseConnectors();
}
