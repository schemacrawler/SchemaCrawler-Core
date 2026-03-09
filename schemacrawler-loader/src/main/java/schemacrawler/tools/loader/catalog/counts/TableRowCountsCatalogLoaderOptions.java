/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.catalog.counts;

import schemacrawler.tools.command.CommandOptions;

public record TableRowCountsCatalogLoaderOptions(boolean loadRowCounts, boolean noEmptyTables)
    implements CommandOptions {}
