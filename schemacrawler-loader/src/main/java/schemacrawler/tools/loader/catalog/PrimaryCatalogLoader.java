/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.catalog;

import schemacrawler.crawl.SchemaCrawler;
import schemacrawler.schema.Catalog;
import schemacrawler.tools.command.CommandOptions;
import schemacrawler.tools.loader.catalog.PrimaryCatalogLoader.PrimaryCatalogLoaderOptions;
import us.fatehi.utility.property.PropertyName;

public class PrimaryCatalogLoader extends AbstractCatalogLoader<PrimaryCatalogLoaderOptions> {

  static record PrimaryCatalogLoaderOptions() implements CommandOptions {}

  PrimaryCatalogLoader(final PropertyName catalogLoaderName) {
    super(catalogLoaderName);
  }

  @Override
  public void execute() {
    if (hasCatalog()) {
      return;
    }

    final SchemaCrawler schemaCrawler =
        new SchemaCrawler(
            getConnectionSource(), getSchemaRetrievalOptions(), getSchemaCrawlerOptions());
    final Catalog catalog = schemaCrawler.crawl();
    setCatalog(catalog);
    // NOTE: Catalog loaders do not build ER models, so the ER model is not set here
  }

  @Override
  public boolean usesConnection() {
    return true;
  }
}
