/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.catalog;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.loader.catalog.ChainedCatalogLoader.ChainedCatalogLoaderOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.tools.command.CommandOptions;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.options.ConfigUtility;
import schemacrawler.tools.utility.ExecutionStateUtility;
import schemacrawler.utility.MetaDataUtility;
import us.fatehi.utility.property.PropertyName;
import us.fatehi.utility.string.ObjectToStringFormat;
import us.fatehi.utility.string.StringFormat;

public class ChainedCatalogLoader extends AbstractCatalogLoader<ChainedCatalogLoaderOptions> {

  static record ChainedCatalogLoaderOptions() implements CommandOptions {}

  private static final Logger LOGGER = Logger.getLogger(ChainedCatalogLoader.class.getName());

  private final List<CatalogLoader<?>> catalogLoaders;
  private final Config additionalConfig;

  public ChainedCatalogLoader(
      final List<CatalogLoader<?>> catalogLoaders, final Config additionalConfig) {
    super(
        new PropertyName(
            "chainloader", "Chain of all catalog loaders, called in turn by priority"));
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
    final SchemaRetrievalOptions schemaRetrievalOptions = getSchemaRetrievalOptions();
    for (final CatalogLoader<?> catalogLoader : catalogLoaders) {
      ExecutionStateUtility.transferState(this, catalogLoader);
      catalogLoader.setSchemaRetrievalOptions(schemaRetrievalOptions);

      // Execute
      LOGGER.log(Level.INFO, new StringFormat("Executing catalog loader <%s>", command));
      LOGGER.log(Level.CONFIG, new ObjectToStringFormat(catalogLoader.getCommandOptions()));
      catalogLoader.execute();

      ExecutionStateUtility.transferState(catalogLoader, this);
    }
    MetaDataUtility.logCatalogSummary(getCatalog(), Level.INFO);
  }

  @Override
  public void initialize() {
    super.initialize();
    for (final CatalogLoader<?> catalogLoader : catalogLoaders) {
      // Initialize, and check if the command is available
      catalogLoader.initialize();
    }
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
