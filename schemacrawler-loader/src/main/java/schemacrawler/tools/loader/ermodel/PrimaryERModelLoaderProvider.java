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
import schemacrawler.tools.loader.ermodel.PrimaryERModelLoader.PrimaryERModelLoaderLoaderOptions;
import schemacrawler.tools.options.Config;
import us.fatehi.utility.property.PropertyName;

/** Provider for {@link PrimaryERModelLoader}. */
public class PrimaryERModelLoaderProvider extends AbstractERModelLoaderProvider {

  private static final PropertyName NAME =
      new PropertyName("primarymodelloader", "Loader for ER Model");

  @Override
  public Collection<PropertyName> getSupportedCommands() {
    return List.of(NAME);
  }

  @Override
  public PrimaryERModelLoader newCommand(final Config config) {
    requireNonNull(config, "No config provided");
    final PrimaryERModelLoader loader = new PrimaryERModelLoader(NAME);
    loader.configure(new PrimaryERModelLoaderLoaderOptions());
    return loader;
  }
}
