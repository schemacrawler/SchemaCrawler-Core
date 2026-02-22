/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.attributes;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.isBlank;

import schemacrawler.tools.executable.CommandOptions;
import schemacrawler.tools.options.Config;

public record AttributesCatalogLoaderOptions(String catalogAttributesFile)
    implements CommandOptions {

  public boolean hasCatalogAttributesFile() {
    return !isBlank(catalogAttributesFile);
  }

  static AttributesCatalogLoaderOptions fromConfig(final Config config) {
    requireNonNull(config, "No config provided");
    final String catalogAttributesFile =
        config.getStringValue(AttributesCatalogLoader.OPTION_ATTRIBUTES_FILE);
    return new AttributesCatalogLoaderOptions(catalogAttributesFile);
  }
}
