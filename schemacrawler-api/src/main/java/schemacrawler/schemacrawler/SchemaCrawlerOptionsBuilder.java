/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schemacrawler;

/** SchemaCrawler options builder, to build the immutable options to crawl a schema. */
public final class SchemaCrawlerOptionsBuilder {

  public static SchemaCrawlerOptions newSchemaCrawlerOptions() {
    final CrawlOptions crawlOptions =
        new CrawlOptions(
            "", LoadOptionsBuilder.newLoadOptions(), LimitOptionsBuilder.newLimitOptions());
    final ProjectionOptions projectionOptions =
        new ProjectionOptions(
            FilterOptionsBuilder.newFilterOptions(), GrepOptionsBuilder.newGrepOptions());
    return new SchemaCrawlerOptions(crawlOptions, projectionOptions);
  }

  private SchemaCrawlerOptionsBuilder() {
    throw new UnsupportedOperationException();
  }
}
