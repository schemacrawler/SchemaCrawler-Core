/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.catalogloader;

import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schemacrawler.exceptions.InternalRuntimeException;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.registry.BasePluginCommandRegistry;
import us.fatehi.utility.string.StringFormat;

/** Registry for registered catalog orders, in order of priority. */
public final class CatalogLoaderRegistry extends BasePluginCommandRegistry<CatalogLoaderProvider> {

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

  private CatalogLoaderRegistry() {
    super("SchemaCrawler Catalog Loaders", loadCatalogLoaderRegistry());
  }

  public ChainedCatalogLoader newChainedCatalogLoader(final Config additionalConfig) {
    // Make a defensive copy of the list of catalog loaders
    final List<CatalogLoaderProvider> chainedCatalogLoaders = getCommandProviderRegistry();
    return new ChainedCatalogLoader(chainedCatalogLoaders, additionalConfig);
  }
}
