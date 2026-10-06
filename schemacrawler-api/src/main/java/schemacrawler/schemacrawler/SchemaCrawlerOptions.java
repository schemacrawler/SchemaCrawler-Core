/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schemacrawler;

import static java.util.Objects.requireNonNull;

import org.jspecify.annotations.NonNull;
import us.fatehi.utility.Options;

/** SchemaCrawler options. */
public record SchemaCrawlerOptions(
    @NonNull CrawlOptions crawlOptions, @NonNull ProjectionOptions projectionOptions)
    implements Options {

  public SchemaCrawlerOptions {
    requireNonNull(crawlOptions, "No crawl options provided");
    requireNonNull(projectionOptions, "No projection options provided");
  }

  public String title() {
    return crawlOptions.title();
  }

  public LoadOptions loadOptions() {
    return crawlOptions.loadOptions();
  }

  public LimitOptions limitOptions() {
    return crawlOptions.limitOptions();
  }

  public FilterOptions filterOptions() {
    return projectionOptions.filterOptions();
  }

  public GrepOptions grepOptions() {
    return projectionOptions.grepOptions();
  }

  public SchemaCrawlerOptions withFilterOptions(final FilterOptions filterOptions) {
    if (filterOptions == null) {
      return this;
    }
    return new SchemaCrawlerOptions(
        crawlOptions, new ProjectionOptions(filterOptions, projectionOptions.grepOptions()));
  }

  public SchemaCrawlerOptions withGrepOptions(final GrepOptions grepOptions) {
    if (grepOptions == null) {
      return this;
    }
    return new SchemaCrawlerOptions(
        crawlOptions, new ProjectionOptions(projectionOptions.filterOptions(), grepOptions));
  }

  public SchemaCrawlerOptions withLimitOptions(final LimitOptions limitOptions) {
    if (limitOptions == null) {
      return this;
    }
    return new SchemaCrawlerOptions(
        new CrawlOptions(crawlOptions.title(), crawlOptions.loadOptions(), limitOptions),
        projectionOptions);
  }

  public SchemaCrawlerOptions withLoadOptions(final LoadOptions loadOptions) {
    if (loadOptions == null) {
      return this;
    }
    return new SchemaCrawlerOptions(
        new CrawlOptions(crawlOptions.title(), loadOptions, crawlOptions.limitOptions()),
        projectionOptions);
  }

  public SchemaCrawlerOptions withTitle(final String title) {
    return new SchemaCrawlerOptions(
        new CrawlOptions(title, crawlOptions.loadOptions(), crawlOptions.limitOptions()),
        projectionOptions);
  }
}
