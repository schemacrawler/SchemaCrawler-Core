/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.ermodel;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import schemacrawler.tools.loader.ermodel.SchemaCrawlerERModelLoader.SchemaCrawlerERModelLoaderOptions;
import schemacrawler.tools.options.Config;
import us.fatehi.utility.property.PropertyName;

/** Provider for {@link SchemaCrawlerERModelLoader}. */
public class SchemaCrawlerERModelLoaderProvider extends BaseERModelLoaderProvider {

  private static final PropertyName NAME =
      new PropertyName("schemacrawlerermodelloader", "Loader for SchemaCrawler ERModel");

  @Override
  public Collection<PropertyName> getSupportedCommands() {
    return List.of(NAME);
  }

  @Override
  public SchemaCrawlerERModelLoader newCommand(final Config config) {
    requireNonNull(config, "No config provided");
    final SchemaCrawlerERModelLoader loader = new SchemaCrawlerERModelLoader(NAME);
    loader.configure(new SchemaCrawlerERModelLoaderOptions());
    return loader;
  }
}
