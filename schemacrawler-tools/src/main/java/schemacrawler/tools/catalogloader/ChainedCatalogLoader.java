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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schema.Catalog;
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

public class ChainedCatalogLoader extends BaseCatalogLoader<ChainedCatalogLoaderOptions>
    implements Iterable<CatalogLoader> {

  static record ChainedCatalogLoaderOptions() implements CommandOptions {}

  private static final Logger LOGGER = Logger.getLogger(ChainedCatalogLoader.class.getName());

  private final List<CatalogLoader> chainedCatalogLoaders;
  private Config additionalConfig;

  public ChainedCatalogLoader(final List<CatalogLoader> chainedCatalogLoaders) {
    super(
        new PropertyName("chainloader", "Chain of all catalog loaders, called in turn by priority"),
        Integer.MIN_VALUE);
    requireNonNull(chainedCatalogLoaders);
    this.chainedCatalogLoaders = new ArrayList<>(chainedCatalogLoaders);
  }

  @Override
  public void execute() {
    Catalog catalog = null;
    final DatabaseConnectionSource dataSource = getDataSource();
    final SchemaCrawlerOptions schemaCrawlerOptions = getSchemaCrawlerOptions();
    final SchemaRetrievalOptions schemaRetrievalOptions = getSchemaRetrievalOptions();
    for (final CatalogLoader nextCatalogLoader : chainedCatalogLoaders) {
      LOGGER.log(
          Level.CONFIG,
          new StringFormat("Loading catalog with <%s>", nextCatalogLoader.getClass()));
      nextCatalogLoader.setCatalog(catalog);
      nextCatalogLoader.setDataSource(dataSource);
      nextCatalogLoader.setSchemaCrawlerOptions(schemaCrawlerOptions);
      nextCatalogLoader.setSchemaRetrievalOptions(schemaRetrievalOptions);
      nextCatalogLoader.setAdditionalConfiguration(additionalConfig);

      nextCatalogLoader.execute();

      catalog = nextCatalogLoader.getCatalog();
    }
    MetaDataUtility.logCatalogSummary(catalog, Level.INFO);
    setCatalog(catalog);
  }

  @Override
  public Iterator<CatalogLoader> iterator() {
    return chainedCatalogLoaders.iterator();
  }

  @Override
  public void setAdditionalConfiguration(final Config additionalConfig) {
    if (additionalConfig == null) {
      this.additionalConfig = ConfigUtility.newConfig();
    }
    this.additionalConfig = additionalConfig;
  }

  @Override
  public String toString() {
    return "CatalogLoader [" + chainedCatalogLoaders + "]";
  }
}
