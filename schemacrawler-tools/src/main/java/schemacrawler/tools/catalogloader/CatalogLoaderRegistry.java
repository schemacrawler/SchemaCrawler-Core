/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.catalogloader;

import static java.util.Comparator.naturalOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schemacrawler.exceptions.InternalRuntimeException;
import schemacrawler.tools.executable.commandline.PluginCommand;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.registry.BasePluginRegistry;
import schemacrawler.tools.registry.PluginCommandRegistry;
import us.fatehi.utility.property.PropertyName;
import us.fatehi.utility.string.StringFormat;

/** Registry for registered catalog orders, in order of priority. */
public final class CatalogLoaderRegistry extends BasePluginRegistry
    implements PluginCommandRegistry {

  private static final Logger LOGGER = Logger.getLogger(CatalogLoaderRegistry.class.getName());

  private static CatalogLoaderRegistry catalogLoaderRegistrySingleton;

  public static CatalogLoaderRegistry getCatalogLoaderRegistry() {
    if (catalogLoaderRegistrySingleton == null) {
      catalogLoaderRegistrySingleton = new CatalogLoaderRegistry();
      catalogLoaderRegistrySingleton.log();
    }
    return catalogLoaderRegistrySingleton;
  }

  private static List<CatalogLoaderProvider> loadCatalogLoaderRegistry() {

    // Use thread-safe list
    final List<CatalogLoaderProvider> catalogLoaderRegistry = new CopyOnWriteArrayList<>();

    try {
      final ServiceLoader<CatalogLoaderProvider> serviceLoader =
          ServiceLoader.load(
              CatalogLoaderProvider.class, CatalogLoaderRegistry.class.getClassLoader());
      for (final CatalogLoaderProvider catalogLoader : serviceLoader) {
        LOGGER.log(
            Level.CONFIG,
            new StringFormat("Loading catalog loader, %s", catalogLoader.getClass().getName()));

        catalogLoaderRegistry.add(catalogLoader);
      }
    } catch (final Throwable e) {
      throw new InternalRuntimeException("Could not load catalog loader registry", e);
    }

    return catalogLoaderRegistry;
  }

  private final List<CatalogLoaderProvider> catalogLoaderRegistry;

  private CatalogLoaderRegistry() {
    super("SchemaCrawler Catalog Loaders");
    catalogLoaderRegistry = loadCatalogLoaderRegistry();
  }

  @Override
  public Collection<PluginCommand> getCommandLineCommands() {
    final Collection<PluginCommand> commandLineCommands = new HashSet<>();
    for (final CatalogLoaderProvider catalogLoader : catalogLoaderRegistry) {
      commandLineCommands.add(catalogLoader.getCommandLineCommand());
    }
    return commandLineCommands;
  }

  @Override
  public Collection<PluginCommand> getHelpCommands() {
    final Collection<PluginCommand> commandLineCommands = new HashSet<>();
    for (final CatalogLoaderProvider catalogLoader : catalogLoaderRegistry) {
      commandLineCommands.add(catalogLoader.getHelpCommand());
    }
    return commandLineCommands;
  }

  @Override
  public Collection<PropertyName> getRegisteredPlugins() {
    final List<PropertyName> commandLineCommands = new ArrayList<>();
    for (final CatalogLoaderProvider catalogLoaderProvider : catalogLoaderRegistry) {
      commandLineCommands.addAll(catalogLoaderProvider.getSupportedCommands());
    }
    commandLineCommands.sort(naturalOrder());
    return commandLineCommands;
  }

  public ChainedCatalogLoader newChainedCatalogLoader(final Config additionalConfig) {
    // Make a defensive copy of the list of catalog loaders
    final List<CatalogLoaderProvider> chainedCatalogLoaders = List.copyOf(catalogLoaderRegistry);
    return new ChainedCatalogLoader(chainedCatalogLoaders, additionalConfig);
  }
}
