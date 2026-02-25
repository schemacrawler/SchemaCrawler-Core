/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.weakassociations;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import schemacrawler.tools.catalogloader.BaseCatalogLoaderProvider;
import schemacrawler.tools.executable.commandline.PluginCommand;
import schemacrawler.tools.options.Config;
import us.fatehi.utility.property.PropertyName;

public class WeakAssociationsCatalogLoaderProvider extends BaseCatalogLoaderProvider {

  private static final PropertyName NAME =
      new PropertyName("weakassociationsloader", "Loader for weak associations");

  private static final String OPTION_WEAK_ASSOCIATIONS = "weak-associations";
  private static final String OPTION_INFER_EXTENSION_TABLES = "infer-extension-tables";

  @Override
  public PluginCommand getCommandLineCommand() {
    final PluginCommand pluginCommand = PluginCommand.newCatalogLoaderCommand(NAME);
    pluginCommand.addOption(
        OPTION_WEAK_ASSOCIATIONS,
        Boolean.class,
        "Analyzes the schema to find weak associations between tables, based on table and column"
            + " naming patterns",
        "This can be a time consuming operation",
        "Optional, defaults to false");
    pluginCommand.addOption(
        OPTION_INFER_EXTENSION_TABLES,
        Boolean.class,
        "Infers extension tables that have similarly named primary keys, and reports them as weak"
            + " associations",
        "Optional, defaults to false");
    return pluginCommand;
  }

  @Override
  public Collection<PropertyName> getSupportedCommands() {
    return List.of(NAME);
  }

  @Override
  public WeakAssociationsCatalogLoader newCommand(final String command, final Config config) {
    requireNonNull(config, "No config provided");
    if (!NAME.getName().equals(command)) {
      throw new IllegalArgumentException("Bad catalog loader command <%s>".formatted(command));
    }

    final WeakAssociationsCatalogLoader loader = new WeakAssociationsCatalogLoader(NAME);
    final WeakAssociationsCatalogLoaderOptions options = createOptionsfromConfig(config);
    loader.configure(options);

    return loader;
  }

  private WeakAssociationsCatalogLoaderOptions createOptionsfromConfig(final Config config) {
    final boolean findWeakAssociations = config.getBooleanValue(OPTION_WEAK_ASSOCIATIONS, false);
    final boolean inferExtensionTables =
        config.getBooleanValue(OPTION_INFER_EXTENSION_TABLES, false);
    return new WeakAssociationsCatalogLoaderOptions(findWeakAssociations, inferExtensionTables);
  }
}
