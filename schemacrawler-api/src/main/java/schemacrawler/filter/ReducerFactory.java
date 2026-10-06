/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static java.util.Objects.requireNonNull;

import schemacrawler.schema.CatalogReducer;
import schemacrawler.schemacrawler.CrawlOptions;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public final class ReducerFactory {

  public static CatalogReducer getCatalogReducer(final CrawlOptions options) {
    requireNonNull(options, "No options provided");
    return new StandardCatalogReducer(options);
  }

  private ReducerFactory() {
    // Prevent instantiation
  }
}
