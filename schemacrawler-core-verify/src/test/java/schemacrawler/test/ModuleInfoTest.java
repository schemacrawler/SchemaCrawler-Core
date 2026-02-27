/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import schemacrawler.tools.catalogloader.CatalogLoaderRegistry;
import schemacrawler.tools.databaseconnector.DatabaseConnectorRegistry;
import schemacrawler.tools.executable.CommandRegistry;

public class ModuleInfoTest {

  @Test
  public void testRegistriesLoadable() {
    assertNotNull(CatalogLoaderRegistry.getCatalogLoaderRegistry());
    assertNotNull(DatabaseConnectorRegistry.getDatabaseConnectorRegistry());
    assertNotNull(CommandRegistry.getCommandRegistry());
  }
}
