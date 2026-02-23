/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.counts;

import schemacrawler.tools.executable.CommandOptions;

public record TableRowCountsCatalogLoaderOptions(boolean loadRowCounts, boolean noEmptyTables)
    implements CommandOptions {}
