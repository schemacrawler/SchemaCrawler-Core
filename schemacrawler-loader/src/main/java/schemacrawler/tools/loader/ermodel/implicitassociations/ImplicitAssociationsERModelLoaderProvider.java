/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.ermodel.implicitassociations;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import schemacrawler.tools.loader.ermodel.BaseERModelLoaderProvider;
import schemacrawler.tools.options.Config;
import us.fatehi.utility.property.PropertyName;

/**
 * Provider for {@link ImplicitAssociationsERModelLoader}.
 *
 * <p>This provider registers the implicit associations ER model loader, which enriches an existing
 * ER model with implicit relationships discovered from table and column naming patterns.
 */
public class ImplicitAssociationsERModelLoaderProvider extends BaseERModelLoaderProvider {

  private static final PropertyName NAME =
      new PropertyName(
          "implicitassociationsmodelloader", "Loader for implicit associations in ER Model");

  private static final String OPTION_IMPLICIT_ASSOCIATIONS = "implicit-associations";
  @Override
  public Collection<PropertyName> getSupportedCommands() {
    return List.of(NAME);
  }

  @Override
  public ImplicitAssociationsERModelLoader newCommand(final Config config) {
    requireNonNull(config, "No config provided");

    final ImplicitAssociationsERModelLoader loader = new ImplicitAssociationsERModelLoader(NAME);
    final ImplicitAssociationsERModelLoaderOptions options = createOptionsFromConfig(config);
    loader.configure(options);

    return loader;
  }

  private ImplicitAssociationsERModelLoaderOptions createOptionsFromConfig(final Config config) {
    final boolean loadImplicitAssociations =
        config.getBooleanValue(OPTION_IMPLICIT_ASSOCIATIONS, true);
    return new ImplicitAssociationsERModelLoaderOptions(loadImplicitAssociations);
  }
}
