/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schemacrawler;

import static java.util.Objects.requireNonNull;

import us.fatehi.utility.OptionsBuilder;

/** Builds immutable options for selecting a catalog projection. */
public final class CatalogProjectionOptionsBuilder
    implements OptionsBuilder<CatalogProjectionOptionsBuilder, CatalogProjectionOptions> {

  public static CatalogProjectionOptionsBuilder builder() {
    return new CatalogProjectionOptionsBuilder();
  }

  public static CatalogProjectionOptions newCatalogProjectionOptions() {
    return builder().toOptions();
  }

  private FilterOptions filterOptions;
  private GrepOptions grepOptions;

  private CatalogProjectionOptionsBuilder() {
    filterOptions = FilterOptionsBuilder.newFilterOptions();
    grepOptions = GrepOptionsBuilder.newGrepOptions();
  }

  @Override
  public CatalogProjectionOptionsBuilder fromOptions(final CatalogProjectionOptions options) {
    if (options != null) {
      filterOptions = options.filterOptions();
      grepOptions = options.grepOptions();
    }
    return this;
  }

  public CatalogProjectionOptionsBuilder withFilterOptions(final FilterOptions filterOptions) {
    if (filterOptions != null) {
      this.filterOptions = filterOptions;
    }
    return this;
  }

  public CatalogProjectionOptionsBuilder withGrepOptions(final GrepOptions grepOptions) {
    if (grepOptions != null) {
      this.grepOptions = grepOptions;
    }
    return this;
  }

  @Override
  public CatalogProjectionOptions toOptions() {
    return new CatalogProjectionOptions(
        requireNonNull(filterOptions, "No filter options provided"),
        requireNonNull(grepOptions, "No grep options provided"));
  }
}
