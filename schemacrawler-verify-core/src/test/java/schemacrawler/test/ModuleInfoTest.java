/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import schemacrawler.loader.catalog.CatalogLoaderRegistry;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.tools.command.CommandRegistry;
import schemacrawler.tools.databaseconnector.DatabaseConnectorRegistry;

public class ModuleInfoTest {

  @Test
  @Disabled("Does not use the SchemaCrawler core jar as a module")
  public void testModuleVisibility() {
    final Module module = SchemaCrawlerOptions.class.getModule();

    // Convert previous prints to assertions (negative tests) using Hamcrest matchers
    assertThat(
        "Unexpected module name", module.getName(), is("us.fatehi.schemacrawler.schemacrawler"));
    assertThat(
        "Internal package schemacrawler.crawl must not be exported",
        module.isExported("schemacrawler.crawl"),
        is(false));
    assertThat(
        "Internal package schemacrawler.ermodel.implementation must not be exported",
        module.isExported("schemacrawler.ermodel.implementation"),
        is(false));
    assertThat(
        "Public API package schemacrawler.schemacrawler must be exported",
        module.isExported("schemacrawler.schemacrawler"),
        is(true));
  }

  @Test
  public void testRegistriesLoadable() {
    assertThat(CatalogLoaderRegistry.getRegistry(), is(notNullValue()));
    assertThat(DatabaseConnectorRegistry.getRegistry(), is(notNullValue()));
    assertThat(CommandRegistry.getRegistry(), is(notNullValue()));
  }
}
