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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schemacrawler.exceptions.InternalRuntimeException;
import us.fatehi.utility.property.PropertyName;
import us.fatehi.utility.string.StringFormat;

/** Registry for registered ERModel loaders, in order of priority. */
public final class ERModelLoaderRegistry {

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

  private final List<ERModelLoaderProvider> erModelLoaderProviders;
  private final String registryName;

  private ERModelLoaderRegistry() {
    registryName = "SchemaCrawler ERModel Loaders";
    erModelLoaderProviders = loadERModelLoaderRegistry();
  }

  /**
   * Returns the name of this registry.
   *
   * @return Registry name
   */
  public String getName() {
    return registryName;
  }

  /**
   * Returns the names of all registered ERModel loaders.
   *
   * @return Registered ERModel loader names
   */
  public Collection<PropertyName> getRegisteredPlugins() {
    final Collection<PropertyName> supportedLoaders = new HashSet<>();
    for (final ERModelLoaderProvider provider : erModelLoaderProviders) {
      if (provider != null) {
        supportedLoaders.addAll(provider.getSupportedLoaders());
      }
    }
    final List<PropertyName> orderedLoaders = new ArrayList<>(supportedLoaders);
    Collections.sort(orderedLoaders);
    return Collections.unmodifiableList(orderedLoaders);
  }

  /** Logs the registered ERModel loaders at CONFIG level. */
  public void log() {
    if (!LOGGER.isLoggable(Level.CONFIG)) {
      return;
    }
    final StringBuilder sb = new StringBuilder();
    sb.append("Registered ").append(registryName).append(":\n");
    for (final PropertyName name : getRegisteredPlugins()) {
      sb.append("  ").append(name).append("\n");
    }
    LOGGER.log(Level.CONFIG, sb.toString());
  }

  /**
   * Creates a new chained ERModel loader from all registered providers.
   *
   * @return ChainedERModelLoader composed of all registered loaders
   */
  public ChainedERModelLoader newChainedERModelLoader() {
    final List<ERModelLoader<?>> loaders = buildERModelLoaders();
    return new ChainedERModelLoader(loaders);
  }

  private List<ERModelLoader<?>> buildERModelLoaders() {
    final List<ERModelLoader<?>> erModelLoaders = new ArrayList<>();
    for (final ERModelLoaderProvider provider : erModelLoaderProviders) {
      try {
        final ERModelLoader<?> erModelLoader = provider.newLoader();
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
