/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.weakassociations;

import static java.util.Objects.requireNonNull;

import schemacrawler.tools.executable.CommandOptions;
import schemacrawler.tools.options.Config;

public record WeakAssociationsCatalogLoaderOptions(
    boolean findWeakAssociations, boolean inferExtensionTables) implements CommandOptions {

  static WeakAssociationsCatalogLoaderOptions fromConfig(final Config config) {
    requireNonNull(config, "No config provided");
    final boolean findWeakAssociations =
        config.getBooleanValue(WeakAssociationsCatalogLoader.OPTION_WEAK_ASSOCIATIONS, false);
    final boolean inferExtensionTables =
        config.getBooleanValue(WeakAssociationsCatalogLoader.OPTION_INFER_EXTENSION_TABLES, false);
    return new WeakAssociationsCatalogLoaderOptions(findWeakAssociations, inferExtensionTables);
  }
}
