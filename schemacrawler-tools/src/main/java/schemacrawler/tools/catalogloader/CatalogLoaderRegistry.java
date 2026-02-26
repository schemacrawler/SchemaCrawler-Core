/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.catalogloader;

import static java.util.Comparator.comparingInt;
import static java.util.Comparator.nullsLast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.exceptions.ExecutionRuntimeException;
import schemacrawler.schemacrawler.exceptions.InternalRuntimeException;
import schemacrawler.schemacrawler.exceptions.SchemaCrawlerException;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.registry.BasePluginCommandRegistry;
import us.fatehi.utility.string.StringFormat;

/** Registry for registered catalog orders, in order of priority. */
public final class CatalogLoaderRegistry extends BasePluginCommandRegistry<CatalogLoaderProvider> {

  private static final Logger LOGGER = Logger.getLogger(CatalogLoaderRegistry.class.getName());

  private static Comparator<CatalogLoader<?>> catalogLoaderComparator =
      nullsLast(comparingInt(CatalogLoader<?>::getPriority))
          .thenComparing(loader -> loader.getCommandName().getName());

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

  public ChainedCatalogLoader newChainedCatalogLoader(
      final SchemaCrawlerOptions schemaCrawlerOptions, final Config additionalConfig) {
    // Make a defensive copy of the list of catalog loaders
    final List<CatalogLoader<?>> chainedCatalogLoaders =
        configureCatalogLoaders(schemaCrawlerOptions, additionalConfig);
    return new ChainedCatalogLoader(chainedCatalogLoaders, additionalConfig);
  }

  private List<CatalogLoader<?>> configureCatalogLoaders(
      final SchemaCrawlerOptions schemaCrawlerOptions, final Config additionalConfig) {
    final List<CatalogLoader<?>> catalogLoaders = new ArrayList<>();
    for (final CatalogLoaderProvider catalogLoaderProvider : getCommandProviders()) {
      try {
        final CatalogLoader<?> catalogLoader = catalogLoaderProvider.newCommand(additionalConfig);
        if (catalogLoader == null) {
          LOGGER.log(
              Level.WARNING,
              new StringFormat("Catalog loader <%s> not instantiated", catalogLoaderProvider));
          continue;
        }
        catalogLoader.setSchemaCrawlerOptions(schemaCrawlerOptions);

        catalogLoaders.add(catalogLoader);
      } catch (final SchemaCrawlerException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
        throw new ExecutionRuntimeException(
            "Catalog loader <%s> not instantiated".formatted(catalogLoaderProvider), e);
      } catch (final Throwable e) {
        // Mainly catch NoClassDefFoundError, which is a Throwable,
        // for missing third-party jars
        LOGGER.log(Level.CONFIG, e.getMessage(), e);
        throw new InternalRuntimeException(
            "Catalog loader <%s> not instantiated".formatted(catalogLoaderProvider));
      }
    }

    Collections.sort(catalogLoaders, catalogLoaderComparator);
    return catalogLoaders;
  }
}
