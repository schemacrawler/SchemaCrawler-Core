/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import org.jspecify.annotations.NonNull;
import schemacrawler.test.utility.TestCatalogLoader.TestCatalogLoaderOptions;
import schemacrawler.tools.catalogloader.BaseCatalogLoaderProvider;
import schemacrawler.tools.executable.commandline.PluginCommand;
import schemacrawler.tools.options.Config;
import us.fatehi.utility.SystemExitException;
import us.fatehi.utility.property.PropertyName;

public class TestCatalogLoaderProvider extends BaseCatalogLoaderProvider {

  private static final @NonNull PropertyName NAME =
      new PropertyName("testloader", "Loader for testing");

  public TestCatalogLoaderProvider() {
    forceInstantiationFailureIfConfigured();
  }

  @Override
  public PluginCommand getCommandLineCommand() {
    final PluginCommand pluginCommand = PluginCommand.newCatalogLoaderCommand(NAME);
    pluginCommand.addOption(
        "test-load-option",
        Boolean.class,
        "Check that the test option is added to the load command");
    return pluginCommand;
  }

  @Override
  public Collection<PropertyName> getSupportedCommands() {
    return List.of(NAME);
  }

  @Override
  public TestCatalogLoader newCommand(final Config config) {
    forceLoadFailureIfConfigured();
    requireNonNull(config, "No config provided");
    final TestCatalogLoader loader = new TestCatalogLoader(NAME);
    loader.configure(new TestCatalogLoaderOptions());
    return loader;
  }

  private void forceInstantiationFailureIfConfigured() {
    final String propertyValue =
        System.getProperty(this.getClass().getName() + ".force-instantiation-failure");
    if (propertyValue != null) {
      throw new RuntimeException("Forced instantiation error");
    }
  }

  private void forceLoadFailureIfConfigured() {
    final String key = this.getClass().getName() + ".force-load-failure";
    final String propertyValue = System.getProperty(key);
    if (propertyValue != null) {
      throw new SystemExitException(2, key);
    }
  }
}
