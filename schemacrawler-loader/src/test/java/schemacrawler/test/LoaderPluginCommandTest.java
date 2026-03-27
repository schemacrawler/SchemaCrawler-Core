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
import schemacrawler.tools.loader.catalog.counts.TableRowCountsLoaderProvider;
import schemacrawler.tools.loader.ermodel.attributes.AttributesLoaderProvider;

public class LoaderPluginCommandTest {

  @Test
  public void loaderPluginCommands() {

    final PluginCommand rowCountsPluginCommand =
        new TableRowCountsLoaderProvider().getCommandLineCommand();
    assertThat(
        rowCountsPluginCommand.toString(),
        is(
            """
            PluginCommand[name='countsloader', options=[\
            PluginCommandOption[name='load-row-counts', valueClass=java.lang.Boolean], \
            PluginCommandOption[name='no-empty-tables', valueClass=java.lang.Boolean]\
            ]]\
            """));

    final PluginCommand attributesPluginCommand =
        new AttributesLoaderProvider().getCommandLineCommand();
    assertThat(
        attributesPluginCommand.toString(),
        is(
            """
            PluginCommand[name='attributesloader', options=[\
            PluginCommandOption[name='attributes-file', valueClass=java.lang.String]\
            ]]\
            """));
  }
}
