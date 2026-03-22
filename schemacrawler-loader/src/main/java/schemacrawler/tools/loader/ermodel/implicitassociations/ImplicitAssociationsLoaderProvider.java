/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.ermodel.implicitassociations;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import schemacrawler.tools.executable.commandline.PluginCommand;
import schemacrawler.tools.loader.ermodel.AbstractERModelLoaderProvider;
import schemacrawler.tools.options.Config;
import us.fatehi.utility.property.PropertyName;

/**
 * Provider for {@link ImplicitAssociationsLoader}.
 *
 * <p>This provider registers the implicit associations ER model loader, which enriches an existing
 * ER model with implicit relationships discovered from table and column naming patterns.
 */
public class ImplicitAssociationsLoaderProvider extends AbstractERModelLoaderProvider {

  private static final PropertyName NAME =
      new PropertyName(
          "implicitassociationsloader", "Loader for implicit associations in ER Model");

  private static final String OPTION_IMPLICIT_ASSOCIATIONS = "implicit-associations";

  // NOTE: For backward compatibility only
  @Deprecated(forRemoval = true)
  private static final String OPTION_WEAK_ASSOCIATIONS = "weak-associations";

  @Override
  public PluginCommand getCommandLineCommand() {
    final PluginCommand pluginCommand = PluginCommand.newCatalogLoaderCommand(NAME);
    pluginCommand.addOption(
        OPTION_IMPLICIT_ASSOCIATIONS,
        Boolean.class,
        "Analyzes the schema to find implicit associations between entities, based on naming"
            + " patterns",
        "This can be a time consuming operation",
        "Optional, defaults to false");
    return pluginCommand;
  }

  @Override
  public Collection<PropertyName> getSupportedCommands() {
    return List.of(NAME);
  }

  @Override
  public ImplicitAssociationsLoader newCommand(final Config config) {
    requireNonNull(config, "No config provided");

    final ImplicitAssociationsLoader loader = new ImplicitAssociationsLoader(NAME);
    final ImplicitAssociationsLoaderOptions options = createOptionsFromConfig(config);
    loader.configure(options);

    return loader;
  }

  private ImplicitAssociationsLoaderOptions createOptionsFromConfig(final Config config) {
    // NOTE: Check weak associations - for backward compatibility only
    final boolean loadWeakAssociations = config.getBooleanValue(OPTION_WEAK_ASSOCIATIONS);
    final boolean loadImplicitAssociations =
        config.getBooleanValue(OPTION_IMPLICIT_ASSOCIATIONS) || loadWeakAssociations;
    return new ImplicitAssociationsLoaderOptions(loadImplicitAssociations);
  }
}
