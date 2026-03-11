/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.ermodel;

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
import schemacrawler.schemacrawler.exceptions.InternalRuntimeException;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.registry.BasePluginCommandRegistry;
import us.fatehi.utility.string.StringFormat;

/** Registry for registered ERModel loaders, in order of priority. */
public final class ERModelLoaderRegistry extends BasePluginCommandRegistry<ERModelLoaderProvider> {

  private static final Logger LOGGER = Logger.getLogger(ERModelLoaderRegistry.class.getName());

  private static final Comparator<ERModelLoader<?>> erModelLoaderComparator =
      nullsLast(comparingInt(ERModelLoader<?>::getPriority))
          .thenComparing(loader -> loader.getCommandName().getName());

  private static ERModelLoaderRegistry erModelLoaderRegistrySingleton;

  /**
   * Returns the singleton ERModel loader registry.
   *
   * @return ERModel loader registry
   */
  public static ERModelLoaderRegistry getERModelLoaderRegistry() {
    if (erModelLoaderRegistrySingleton == null) {
      erModelLoaderRegistrySingleton = new ERModelLoaderRegistry();
      erModelLoaderRegistrySingleton.log();
    }
    return erModelLoaderRegistrySingleton;
  }

  private static List<ERModelLoaderProvider> loadERModelLoaderRegistry() {
    // Use thread-safe list
    final List<ERModelLoaderProvider> erModelLoaderRegistry = new CopyOnWriteArrayList<>();

    try {
      final ServiceLoader<ERModelLoaderProvider> serviceLoader =
          ServiceLoader.load(
              ERModelLoaderProvider.class, ERModelLoaderRegistry.class.getClassLoader());
      for (final ERModelLoaderProvider erModelLoaderProvider : serviceLoader) {
        LOGGER.log(
            Level.CONFIG,
            new StringFormat(
                "Loading ERModel loader, %s", erModelLoaderProvider.getClass().getName()));
        erModelLoaderRegistry.add(erModelLoaderProvider);
      }
    } catch (final Throwable e) {
      throw new InternalRuntimeException("Could not load ERModel loader registry", e);
    }

    return erModelLoaderRegistry;
  }

  private ERModelLoaderRegistry() {
    super("ER Model Loaders", loadERModelLoaderRegistry());
  }

  /**
   * Creates a new chained ERModel loader from all registered providers.
   *
   * @return ChainedERModelLoader composed of all registered loaders
   */
  public ChainedERModelLoader newChainedERModelLoader(final Config additionalConfig) {
    final List<ERModelLoader<?>> loaders = configureERModelLoaders(additionalConfig);
    return new ChainedERModelLoader(loaders, additionalConfig);
  }

  private List<ERModelLoader<?>> configureERModelLoaders(final Config additionalConfig) {
    final List<ERModelLoader<?>> erModelLoaders = new ArrayList<>();
    for (final ERModelLoaderProvider provider : getCommandProviders()) {
      try {
        final ERModelLoader<?> erModelLoader = provider.newCommand(additionalConfig);
        if (erModelLoader == null) {
          LOGGER.log(
              Level.WARNING, new StringFormat("ERModel loader <%s> not instantiated", provider));
          continue;
        }
        erModelLoaders.add(erModelLoader);
      } catch (final Throwable e) {
        // Mainly catch NoClassDefFoundError, which is a Throwable,
        // for missing third-party jars
        LOGGER.log(Level.CONFIG, e.getMessage(), e);
        throw new InternalRuntimeException(
            "ERModel loader <%s> not instantiated".formatted(provider));
      }
    }

    Collections.sort(erModelLoaders, erModelLoaderComparator);
    return erModelLoaders;
  }
}
