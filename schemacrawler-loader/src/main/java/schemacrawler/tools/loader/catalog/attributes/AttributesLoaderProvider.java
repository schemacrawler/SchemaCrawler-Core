/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.catalog.attributes;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import schemacrawler.tools.executable.commandline.PluginCommand;
import schemacrawler.tools.loader.catalog.AbstractCatalogLoaderProvider;
import schemacrawler.tools.options.Config;
import us.fatehi.utility.property.PropertyName;

public class AttributesLoaderProvider extends AbstractCatalogLoaderProvider {

  private static final PropertyName NAME =
      new PropertyName(
          "attributesloader", "Loader for catalog attributes, such as remarks or tags");

  private static final String OPTION_ATTRIBUTES_FILE = "attributes-file";

  @Override
  public PluginCommand getCommandLineCommand() {
    final PluginCommand pluginCommand = PluginCommand.newCatalogLoaderCommand(NAME);
    pluginCommand.addOption(
        OPTION_ATTRIBUTES_FILE,
        String.class,
        "Path to a YAML file with table and column attributes to add to the schema");
    return pluginCommand;
  }

  @Override
  public Collection<PropertyName> getSupportedCommands() {
    return List.of(NAME);
  }

  @Override
  public AttributesLoader newCommand(final Config config) {
    requireNonNull(config, "No config provided");

    final AttributesLoader loader = new AttributesLoader(NAME);
    final AttributesLoaderOptions options = createOptionsfromConfig(config);
    loader.configure(options);

    return loader;
  }

  private AttributesLoaderOptions createOptionsfromConfig(final Config config) {
    final String catalogAttributesFile = config.getStringValue(OPTION_ATTRIBUTES_FILE);
    return new AttributesLoaderOptions(catalogAttributesFile);
  }
}
