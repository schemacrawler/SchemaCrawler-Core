/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.counts;

import static java.util.Objects.requireNonNull;

import schemacrawler.tools.executable.CommandOptions;
import schemacrawler.tools.options.Config;

public record TableRowCountsCatalogLoaderOptions(boolean loadRowCounts, boolean noEmptyTables)
    implements CommandOptions {

  static TableRowCountsCatalogLoaderOptions fromConfig(final Config config) {
    requireNonNull(config, "No config provided");
    final boolean loadRowCounts =
        config.getBooleanValue(TableRowCountsCatalogLoader.OPTION_LOAD_ROW_COUNTS, false);
    final boolean noEmptyTables =
        config.getBooleanValue(TableRowCountsCatalogLoader.OPTION_NO_EMPTY_TABLES, false);
    return new TableRowCountsCatalogLoaderOptions(loadRowCounts, noEmptyTables);
  }
}
