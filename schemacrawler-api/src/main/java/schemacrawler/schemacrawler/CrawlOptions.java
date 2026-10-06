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

/** Options for the crawl phase, combining metadata loading and crawl limits. */
public record CrawlOptions(LoadOptions loadOptions, LimitOptions limitOptions) implements Options {

  public CrawlOptions {
    requireNonNull(loadOptions, "No load options provided");
    requireNonNull(limitOptions, "No limit options provided");
  }
}
