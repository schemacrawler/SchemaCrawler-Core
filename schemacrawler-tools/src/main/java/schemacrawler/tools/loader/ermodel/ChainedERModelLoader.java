/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.ermodel;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schema.Catalog;
import schemacrawler.tools.command.CommandOptions;
import schemacrawler.tools.loader.ermodel.ChainedERModelLoader.ChainedERModelLoaderOptions;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.options.ConfigUtility;
import us.fatehi.utility.property.PropertyName;
import us.fatehi.utility.string.StringFormat;

/**
 * A chained ERModel loader that executes a prioritized list of ERModel loaders in sequence.
 *
 * <p>Each loader in the chain receives the ERModel produced by the previous loader, allowing
 * successive loaders to enrich the model.
 */
public class ChainedERModelLoader extends AbstractERModelLoader<ChainedERModelLoaderOptions> {

  static record ChainedERModelLoaderOptions() implements CommandOptions {}

  private static final Logger LOGGER = Logger.getLogger(ChainedERModelLoader.class.getName());

  private final List<ERModelLoader<?>> erModelLoaders;
  private final Config additionalConfig;

  ChainedERModelLoader(final List<ERModelLoader<?>> erModelLoaders, final Config additionalConfig) {
    super(
        new PropertyName(
            "ermodelchainloader", "Chain of all ERModel loaders, called in turn by priority"),
        Integer.MIN_VALUE);
    requireNonNull(erModelLoaders, "No ERModel loaders provided");
    this.erModelLoaders = new ArrayList<>(erModelLoaders);

    if (additionalConfig == null) {
      this.additionalConfig = ConfigUtility.newConfig();
    } else {
      this.additionalConfig = additionalConfig;
    }

    configure(new ChainedERModelLoaderOptions());
  }

  @Override
  public void execute() {
    final Catalog catalog = getCatalog();
    for (final ERModelLoader<?> erModelLoader : erModelLoaders) {
      erModelLoader.setCatalog(catalog);
      if (hasERModel()) {
        // Pass enriched ERModel to the next loader
        erModelLoader.setERModel(getERModel());
      }

      LOGGER.log(Level.INFO, new StringFormat("Executing ERModel loader <%s>", erModelLoader));
      erModelLoader.execute();

      if (erModelLoader.hasERModel()) {
        setERModel(erModelLoader.getERModel());
      }
    }
  }

  @Override
  public void initialize() {
    super.initialize();
    for (final ERModelLoader<?> erModelLoader : erModelLoaders) {
      erModelLoader.initialize();
    }
  }

  /**
   * Returns the number of loaders in the chain.
   *
   * @return Number of loaders
   */
  public int size() {
    return erModelLoaders.size();
  }

  @Override
  public String toString() {
    return "ChainedERModelLoader [" + erModelLoaders + "]";
  }
}
