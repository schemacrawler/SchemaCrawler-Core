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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.tools.catalogloader.ChainedCatalogLoader.ChainedCatalogLoaderOptions;
import schemacrawler.tools.executable.CommandOptions;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.options.ConfigUtility;
import schemacrawler.utility.MetaDataUtility;
import us.fatehi.utility.datasource.DatabaseConnectionSource;
import us.fatehi.utility.property.PropertyName;
import us.fatehi.utility.string.ObjectToStringFormat;
import us.fatehi.utility.string.StringFormat;

public class ChainedCatalogLoader extends BaseCatalogLoader<ChainedCatalogLoaderOptions> {

  static record ChainedCatalogLoaderOptions() implements CommandOptions {}

  private static final Logger LOGGER = Logger.getLogger(ChainedCatalogLoader.class.getName());

  private final List<CatalogLoader<?>> catalogLoaders;
  private final Config additionalConfig;

  public ChainedCatalogLoader(
      final List<CatalogLoader<?>> catalogLoaders, final Config additionalConfig) {
    super(
        new PropertyName("chainloader", "Chain of all catalog loaders, called in turn by priority"),
        Integer.MIN_VALUE);
    requireNonNull(catalogLoaders);
    this.catalogLoaders = new ArrayList<>(catalogLoaders);

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
    getSchemaCrawlerOptions();
    final SchemaRetrievalOptions schemaRetrievalOptions = getSchemaRetrievalOptions();
    for (final CatalogLoader<?> catalogLoader : catalogLoaders) {

      // Initialize, and check if the command is available
      catalogLoader.initialize();

      if (catalog != null) {
        // Initially catalog will be null until it is first loaded
        catalogLoader.setCatalog(catalog);
      }

      if (catalogLoader.usesConnection()) {
        catalogLoader.setDataSource(dataSource);
      }
      catalogLoader.setSchemaRetrievalOptions(schemaRetrievalOptions);

      // Execute
      LOGGER.log(Level.INFO, new StringFormat("Executing catalog loader <%s>", command));
      LOGGER.log(Level.CONFIG, new ObjectToStringFormat(catalogLoader.getCommandOptions()));
      catalogLoader.execute();

      catalog = catalogLoader.getCatalog();
    }
    MetaDataUtility.logCatalogSummary(catalog, Level.INFO);
  }

  public int size() {
    return catalogLoaders.size();
  }

  @Override
  public String toString() {
    return "CatalogLoaderProvider [" + catalogLoaders + "]";
  }

  @Override
  public boolean usesConnection() {
    return true;
  }
}
