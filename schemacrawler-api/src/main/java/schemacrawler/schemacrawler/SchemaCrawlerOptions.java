/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schemacrawler;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.trimToEmpty;

import org.jspecify.annotations.NonNull;
import us.fatehi.utility.Options;

/** SchemaCrawler options. */
public record SchemaCrawlerOptions(
    @NonNull String title,
    @NonNull LimitOptions limitOptions,
    @NonNull FilterOptions filterOptions,
    @NonNull GrepOptions grepOptions,
    @NonNull LoadOptions loadOptions)
    implements Options {

  public SchemaCrawlerOptions {
    title = trimToEmpty(title);
    requireNonNull(limitOptions, "No limit options provided");
    requireNonNull(filterOptions, "No filter options provided");
    requireNonNull(grepOptions, "No grep options provided");
    requireNonNull(loadOptions, "No load options provided");
  }

  public SchemaCrawlerOptions(
      @NonNull LimitOptions limitOptions,
      @NonNull FilterOptions filterOptions,
      @NonNull GrepOptions grepOptions,
      @NonNull LoadOptions loadOptions) {
    this("", limitOptions, filterOptions, grepOptions, loadOptions);
  }

  public SchemaCrawlerOptions withFilterOptions(final FilterOptions filterOptions) {
    if (filterOptions == null) {
      return this;
    }
    return new SchemaCrawlerOptions(title, limitOptions, filterOptions, grepOptions, loadOptions);
  }

  public SchemaCrawlerOptions withGrepOptions(final GrepOptions grepOptions) {
    if (grepOptions == null) {
      return this;
    }
    return new SchemaCrawlerOptions(title, limitOptions, filterOptions, grepOptions, loadOptions);
  }

  public SchemaCrawlerOptions withLimitOptions(final LimitOptions limitOptions) {
    if (limitOptions == null) {
      return this;
    }
    return new SchemaCrawlerOptions(title, limitOptions, filterOptions, grepOptions, loadOptions);
  }

  public SchemaCrawlerOptions withLoadOptions(final LoadOptions loadOptions) {
    if (loadOptions == null) {
      return this;
    }
    return new SchemaCrawlerOptions(title, limitOptions, filterOptions, grepOptions, loadOptions);
  }

  public SchemaCrawlerOptions withTitle(final String title) {
    return new SchemaCrawlerOptions(title, limitOptions, filterOptions, grepOptions, loadOptions);
  }
}
