/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.catalogloader;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import schemacrawler.tools.catalogloader.SchemaCrawlerCatalogLoader.SchemaCrawlerCatalogLoaderOptions;
import schemacrawler.tools.options.Config;
import us.fatehi.utility.property.PropertyName;

public class SchemaCrawlerCatalogLoaderProvider extends BaseCatalogLoaderProvider {

  private static final PropertyName NAME =
      new PropertyName("schemacrawlerloader", "Loader for SchemaCrawler metadata catalog");

  @Override
  public Collection<PropertyName> getSupportedCommands() {
    return List.of(NAME);
  }

  @Override
  public SchemaCrawlerCatalogLoader newCommand(final Config config) {
    requireNonNull(config, "No config provided");
    final SchemaCrawlerCatalogLoader loader = new SchemaCrawlerCatalogLoader(NAME);
    loader.configure(new SchemaCrawlerCatalogLoaderOptions());
    return loader;
  }
}
