/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.ermodel;

import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.ermodel.implementation.ERModelBuilder;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.schema.Catalog;
import schemacrawler.tools.command.CommandOptions;
import schemacrawler.tools.loader.ermodel.PrimaryERModelLoader.PrimaryERModelLoaderLoaderOptions;
import us.fatehi.utility.property.PropertyName;

/**
 * ERModel loader that builds the full ERModel from a catalog. This is the primary loader, analogous
 * to {@code SchemaCrawlerCatalogLoader}.
 */
class PrimaryERModelLoader extends AbstractERModelLoader<PrimaryERModelLoaderLoaderOptions> {

  private static final Logger LOGGER = Logger.getLogger(PrimaryERModelLoader.class.getName());

  static record PrimaryERModelLoaderLoaderOptions() implements CommandOptions {}

  PrimaryERModelLoader(final PropertyName loaderName) {
    super(loaderName);
  }

  @Override
  public void execute() {
    if (hasERModel()) {
      LOGGER.log(Level.INFO, "ER model has already been built; skipping build");
      return;
    }

    LOGGER.log(Level.INFO, "Building ER model");

    final Catalog catalog = getCatalog();
    final ERModel eRModel = ERModelBuilder.builder(catalog).build();
    setERModel(eRModel);
  }
}
