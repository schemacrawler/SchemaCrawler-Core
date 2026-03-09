/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.ermodel;

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
public class PrimaryERModelLoader extends AbstractERModelLoader<PrimaryERModelLoaderLoaderOptions> {

  static record PrimaryERModelLoaderLoaderOptions() implements CommandOptions {}

  PrimaryERModelLoader(final PropertyName loaderName) {
    super(loaderName, 0);
  }

  @Override
  public void execute() {
    if (hasERModel()) {
      return;
    }

    final Catalog catalog = getCatalog();
    final ERModel eRModel = new ERModelBuilder(catalog).build();
    setERModel(eRModel);
  }
}
