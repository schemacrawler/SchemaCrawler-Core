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
    return new SchemaCrawlerOptions(
        "",
        LimitOptionsBuilder.newLimitOptions(),
        FilterOptionsBuilder.newFilterOptions(),
        GrepOptionsBuilder.newGrepOptions(),
        LoadOptionsBuilder.newLoadOptions());
  }

  private SchemaCrawlerOptionsBuilder() {
    throw new UnsupportedOperationException();
  }
}
