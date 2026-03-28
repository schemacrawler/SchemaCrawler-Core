/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import schemacrawler.tools.executable.commandline.PluginCommand;
import schemacrawler.tools.loader.catalog.CatalogLoaderProvider;
import schemacrawler.tools.loader.catalog.counts.TableRowCountsLoaderProvider;
import schemacrawler.tools.loader.catalog.offline.OfflineCatalogLoaderProvider;
import schemacrawler.tools.loader.ermodel.ERModelLoaderProvider;
import schemacrawler.tools.loader.ermodel.attributes.AttributesLoaderProvider;
import schemacrawler.tools.loader.ermodel.implicitassociations.ImplicitAssociationsLoaderProvider;

public class LoaderPluginCommandTest {

  @Test
  public void catalogLoaderPluginCommands() {

    catalogLoaderPluginCommandLine(
        new TableRowCountsLoaderProvider(),
        """
        PluginCommand[name='countsloader', options=[\
        PluginCommandOption[name='load-row-counts', valueClass=java.lang.Boolean], \
        PluginCommandOption[name='no-empty-tables', valueClass=java.lang.Boolean]\
        ]]\
        """);

    catalogLoaderPluginCommandLine(
        new OfflineCatalogLoaderProvider(),
        """
        PluginCommand[name='unknown', options=[]]\
        """);
  }

  @Test
  public void erModelLoaderPluginCommands() {

    erModelLoaderPluginCommandLine(
        new AttributesLoaderProvider(),
        """
        PluginCommand[name='attributesloader', options=[\
        PluginCommandOption[name='attributes-file', valueClass=java.lang.String]\
        ]]\
        """);

    erModelLoaderPluginCommandLine(
        new ImplicitAssociationsLoaderProvider(),
        """
        PluginCommand[name='implicitassociationsloader', options=[\
        PluginCommandOption[name='implicit-associations', valueClass=java.lang.Boolean], \
        PluginCommandOption[name='infer-extension-tables', valueClass=java.lang.Boolean]]]\
        """);
  }

  private void catalogLoaderPluginCommandLine(
      final CatalogLoaderProvider loaderProvider, final String expectedToString) {
    final PluginCommand pluginCommand = loaderProvider.getCommandLineCommand();
    assertThat(pluginCommand.toString(), is(expectedToString));
  }

  private void erModelLoaderPluginCommandLine(
      final ERModelLoaderProvider loaderProvider, final String expectedToString) {
    final PluginCommand pluginCommand = loaderProvider.getCommandLineCommand();
    assertThat(pluginCommand.toString(), is(expectedToString));
  }
}
