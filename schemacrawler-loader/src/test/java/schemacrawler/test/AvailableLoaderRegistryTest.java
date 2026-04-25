/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import schemacrawler.loader.catalog.CatalogLoaderRegistry;
import schemacrawler.loader.ermodel.ERModelLoaderRegistry;
import schemacrawler.tools.registry.PluginRegistry;
import us.fatehi.utility.property.PropertyName;

public class AvailableLoaderRegistryTest {

  @Test
  public void availableCatalogLoaders() {
    assertThat(
        getRegisteredPlugins(CatalogLoaderRegistry.getCatalogLoaderRegistry()),
        arrayContainingInAnyOrder("countsloader", "offlineloader", "primarycatalogloader"));
  }

  @Test
  public void availableERModelLoaders() {
    assertThat(
        getRegisteredPlugins(ERModelLoaderRegistry.getERModelLoaderRegistry()),
        arrayContainingInAnyOrder(
            "attributesloader", "implicitassociationsloader", "primarymodelloader"));
  }

  private String[] getRegisteredPlugins(final PluginRegistry registry) {
    final List<String> commands = new ArrayList<>();
    final Collection<PropertyName> registeredPlugins = registry.getRegisteredPlugins();
    for (final PropertyName registeredPlugin : registeredPlugins) {
      commands.add(registeredPlugin.getName());
    }
    return commands.toArray(new String[0]);
  }
}
