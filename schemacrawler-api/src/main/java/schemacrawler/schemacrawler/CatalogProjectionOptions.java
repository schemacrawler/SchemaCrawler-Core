/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schemacrawler;

import static java.util.Objects.requireNonNull;

import us.fatehi.utility.Options;

/** Options for selecting a catalog projection after metadata has been loaded. */
public record CatalogProjectionOptions(FilterOptions filterOptions, GrepOptions grepOptions)
    implements Options {

  public CatalogProjectionOptions {
    requireNonNull(filterOptions, "No filter options provided");
    requireNonNull(grepOptions, "No grep options provided");
  }
}
