/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.attributes;

import java.util.Collection;
import java.util.List;
import schemacrawler.tools.catalogloader.BaseCatalogLoaderProvider;
import schemacrawler.tools.executable.commandline.PluginCommand;
import schemacrawler.tools.options.Config;
import us.fatehi.utility.property.PropertyName;

public class AttributesCatalogLoaderProvider extends BaseCatalogLoaderProvider {

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
  public AttributesCatalogLoader newCommand(final String command, final Config config) {
    if (config == null) {
      throw new IllegalArgumentException("No config provided");
    }
    if (!NAME.getName().equals(command)) {
      throw new IllegalArgumentException("Bad catalog loader command <%s>".formatted(command));
    }

    final AttributesCatalogLoader loader = new AttributesCatalogLoader(NAME);
    final AttributesCatalogLoaderOptions options = createOptionsfromConfig(config);
    loader.configure(options);

    return loader;
  }

  private AttributesCatalogLoaderOptions createOptionsfromConfig(final Config config) {
    final String catalogAttributesFile = config.getStringValue(OPTION_ATTRIBUTES_FILE);
    return new AttributesCatalogLoaderOptions(catalogAttributesFile);
  }
}
