/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.catalogloader;

import schemacrawler.crawl.SchemaCrawler;
import schemacrawler.schema.Catalog;
import schemacrawler.tools.catalogloader.SchemaCrawlerCatalogLoader.SchemaCrawlerCatalogLoaderOptions;
import schemacrawler.tools.executable.CommandOptions;
import us.fatehi.utility.property.PropertyName;

public class SchemaCrawlerCatalogLoader
    extends BaseCatalogLoader<SchemaCrawlerCatalogLoaderOptions> {

  static record SchemaCrawlerCatalogLoaderOptions() implements CommandOptions {}

  SchemaCrawlerCatalogLoader(final PropertyName name) {
    super(name, 0);
  }

  @Override
  public void execute() {
    if (isLoaded()) {
      return;
    }

    final SchemaCrawler schemaCrawler =
        new SchemaCrawler(getDataSource(), getSchemaRetrievalOptions(), getSchemaCrawlerOptions());
    final Catalog catalog = schemaCrawler.crawl();
    setCatalog(catalog);
  }
}
