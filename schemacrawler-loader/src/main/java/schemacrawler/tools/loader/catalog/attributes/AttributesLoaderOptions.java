/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.catalog.attributes;

import static us.fatehi.utility.Utility.isBlank;

import schemacrawler.tools.command.CommandOptions;

public record AttributesLoaderOptions(String catalogAttributesFile) implements CommandOptions {

  public boolean hasCatalogAttributesFile() {
    return !isBlank(catalogAttributesFile);
  }
}
