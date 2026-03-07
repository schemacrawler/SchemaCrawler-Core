/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.loader;

import schemacrawler.ermodel.implementation.ERModelBuilder;
import schemacrawler.ermodel.model.ERModel;
import us.fatehi.utility.property.PropertyName;

/**
 * ERModel loader that builds the full ERModel from a catalog. This is the primary loader,
 * analogous to {@code SchemaCrawlerCatalogLoader}.
 */
public class SchemaCrawlerERModelLoader extends AbstractERModelLoader {

  SchemaCrawlerERModelLoader(final PropertyName loaderName) {
    super(loaderName, 0);
  }

  @Override
  public void execute() {
    if (isLoaded()) {
      return;
    }

    final ERModel builtERModel = new ERModelBuilder(catalog).build();
    setERModel(builtERModel);
  }
}
