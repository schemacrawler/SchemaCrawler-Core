/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.catalog.summary;

import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schema.Catalog;
import us.fatehi.utility.UtilityMarker;

/** Utility methods for producing catalog summaries. */
@UtilityMarker
public final class CatalogSummaryUtility {

  private static final Logger LOGGER = Logger.getLogger(CatalogSummaryUtility.class.getName());

  /**
   * Logs a YAML summary of the catalog at the given level.
   *
   * @param catalog Catalog to summarize; may be {@code null} (no-op)
   * @param logLevel Log level; may be {@code null} (no-op)
   */
  public static void logSummary(final Catalog catalog, final Level logLevel) {
    if (catalog == null || logLevel == null) {
      return;
    }
    LOGGER.log(logLevel, () -> summarize(catalog));
  }

  /**
   * Produces a YAML summary of the catalog.
   *
   * @param catalog Catalog to summarize; must not be {@code null}
   * @return Valid YAML summary
   */
  public static String summarize(final Catalog catalog) {
    if (catalog == null) {
      return "";
    }
    return CatalogSummaryTraverser.toYaml(catalog);
  }

  private CatalogSummaryUtility() {
    // Prevent instantiation
  }
}
