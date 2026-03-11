/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import schemacrawler.tools.executable.commandline.PluginCommand;
import schemacrawler.tools.loader.ermodel.ChainedERModelLoader;
import schemacrawler.tools.loader.ermodel.ERModelLoaderRegistry;
import schemacrawler.tools.options.ConfigUtility;
import us.fatehi.utility.property.PropertyName;

public class ERModelLoaderRegistryTest {

  private static final int NUM_LOADERS = 2;

  @Test
  public void chainedLoaders() {
    final ChainedERModelLoader chainedLoaders =
        ERModelLoaderRegistry.getERModelLoaderRegistry()
            .newChainedERModelLoader(ConfigUtility.newConfig());

    assertThat(chainedLoaders.size(), is(NUM_LOADERS));
  }

  @Test
  public void commandLineCommands() throws Exception {
    final Collection<PluginCommand> commandLineCommands =
        ERModelLoaderRegistry.getERModelLoaderRegistry().getCommandLineCommands();
    assertThat(String.valueOf(commandLineCommands), commandLineCommands.size(), is(greaterThan(0)));
    final List<String> names =
        commandLineCommands.stream().map(PluginCommand::getName).collect(toList());
    assertThat(names, containsInAnyOrder("loader:implicitassociationsloader"));
  }

  @Test
  public void helpCommands() throws Exception {
    final Collection<PluginCommand> helpCommands =
        ERModelLoaderRegistry.getERModelLoaderRegistry().getHelpCommands();
    assertThat(String.valueOf(helpCommands), helpCommands.size(), is(greaterThan(0)));
    final List<String> names = helpCommands.stream().map(PluginCommand::getName).collect(toList());
    assertThat(names, containsInAnyOrder("loader:implicitassociationsloader"));
  }

  @Test
  public void name() {
    final ERModelLoaderRegistry registry = ERModelLoaderRegistry.getERModelLoaderRegistry();
    assertThat(registry.getName(), is("ER Model Loaders"));
  }

  @Test
  public void registeredPlugins() {
    final Collection<PropertyName> supportedLoaders =
        ERModelLoaderRegistry.getERModelLoaderRegistry().getRegisteredPlugins();
    assertThat(supportedLoaders, hasSize(NUM_LOADERS));
    final List<String> names =
        supportedLoaders.stream().map(PropertyName::getName).collect(toList());
    assertThat(names, containsInAnyOrder("primarymodelloader", "implicitassociationsloader"));
  }
}
