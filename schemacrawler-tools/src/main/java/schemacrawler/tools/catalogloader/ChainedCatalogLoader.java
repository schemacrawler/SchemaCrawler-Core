/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.catalogloader;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.tools.catalogloader.ChainedCatalogLoader.ChainedCatalogLoaderOptions;
import schemacrawler.tools.executable.CommandOptions;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.options.ConfigUtility;
import schemacrawler.utility.MetaDataUtility;
import us.fatehi.utility.datasource.DatabaseConnectionSource;
import us.fatehi.utility.property.PropertyName;
import us.fatehi.utility.string.StringFormat;

public class ChainedCatalogLoader extends BaseCatalogLoader<ChainedCatalogLoaderOptions> {

  static record ChainedCatalogLoaderOptions() implements CommandOptions {}

  private static final Logger LOGGER = Logger.getLogger(ChainedCatalogLoader.class.getName());

  private final List<CatalogLoaderProvider> catalogLoaderProviders;
  private final Config additionalConfig;

  public ChainedCatalogLoader(
      final List<CatalogLoaderProvider> catalogLoaderProviders, final Config additionalConfig) {
    super(
        new PropertyName("chainloader", "Chain of all catalog loaders, called in turn by priority"),
        Integer.MIN_VALUE);
    requireNonNull(catalogLoaderProviders);
    this.catalogLoaderProviders = new ArrayList<>(catalogLoaderProviders);

    if (additionalConfig == null) {
      this.additionalConfig = ConfigUtility.newConfig();
    } else {
      this.additionalConfig = additionalConfig;
    }

    configure(new ChainedCatalogLoaderOptions());
  }

  @Override
  public void execute() {
    final DatabaseConnectionSource dataSource = getDataSource();
    final SchemaCrawlerOptions schemaCrawlerOptions = getSchemaCrawlerOptions();
    final SchemaRetrievalOptions schemaRetrievalOptions = getSchemaRetrievalOptions();
    final List<CatalogLoader<?>> catalogLoaders = getChainedCatalogLoaders();
    for (final CatalogLoader<?> nextCatalogLoader : catalogLoaders) {
      LOGGER.log(
          Level.CONFIG,
          new StringFormat(
              "Loading catalog with <%s>", nextCatalogLoader.getCommandName().getName()));
      if (catalog != null) {
        // Initially catalog will be null until it is first loaded
        nextCatalogLoader.setCatalog(catalog);
      }
      nextCatalogLoader.setDataSource(dataSource);
      nextCatalogLoader.setSchemaCrawlerOptions(schemaCrawlerOptions);
      nextCatalogLoader.setSchemaRetrievalOptions(schemaRetrievalOptions);

      nextCatalogLoader.execute();

      catalog = nextCatalogLoader.getCatalog();
    }
    MetaDataUtility.logCatalogSummary(catalog, Level.INFO);
    setCatalog(catalog);
  }

  public int size() {
    return catalogLoaderProviders.size();
  }

  @Override
  public Iterator<CatalogLoader> iterator() {
    return chainedCatalogLoaders.iterator();
  }

  @Override
  public void setAdditionalConfiguration(final Config additionalConfig) {
    setCommandOptions(new ChainedCatalogLoaderOptions());

    if (additionalConfig == null) {
      this.additionalConfig = ConfigUtility.newConfig();
    }
    this.additionalConfig = additionalConfig;
  }

  @Override
  public String toString() {
    return "CatalogLoaderProvider [" + catalogLoaderProviders + "]";
  }

  private List<CatalogLoader<?>> getChainedCatalogLoaders() {
    final List<CatalogLoader<?>> catalogLoaders = new ArrayList<>();
    for (final CatalogLoaderProvider catalogLoaderProvider : catalogLoaderProviders) {
      final List<PropertyName> supportedCommands =
          new ArrayList<>(catalogLoaderProvider.getSupportedCommands());
      final String command = supportedCommands.get(0).getName();
      final CatalogLoader<?> catalogLoader =
          catalogLoaderProvider.newCommand(command, additionalConfig);
      catalogLoaders.add(catalogLoader);
    }
    Collections.sort(catalogLoaders, BaseCatalogLoader.comparator);
    return catalogLoaders;
  }
}
