/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.catalog;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import schemacrawler.loader.catalog.PrimaryCatalogLoader.PrimaryCatalogLoaderOptions;
import schemacrawler.tools.options.Config;
import us.fatehi.utility.property.PropertyName;

public class PrimaryCatalogLoaderProvider extends AbstractCatalogLoaderProvider {

  private static final PropertyName NAME =
      new PropertyName("primarycatalogloader", "Loader for SchemaCrawler metadata catalog");

  @Override
  public Collection<PropertyName> getSupportedCommands() {
    return List.of(NAME);
  }

  @Override
  public PrimaryCatalogLoader newCommand(final Config config) {
    requireNonNull(config, "No config provided");
    final PrimaryCatalogLoader loader = new PrimaryCatalogLoader(NAME);
    loader.configure(new PrimaryCatalogLoaderOptions());
    return loader;
  }
}
