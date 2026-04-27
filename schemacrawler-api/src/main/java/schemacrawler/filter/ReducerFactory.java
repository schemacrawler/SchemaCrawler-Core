/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import schemacrawler.schema.CatalogReducer;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public final class ReducerFactory {

  public static CatalogReducer getCatalogReducer(final SchemaCrawlerOptions options) {
    return new StandardCatalogReducer(options);
  }

  private ReducerFactory() {
    // Prevent instantiation
  }
}
