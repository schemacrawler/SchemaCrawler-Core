/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.catalog.summary;

import static java.util.Objects.requireNonNull;

import schemacrawler.schema.Catalog;

/**
 * Drives a {@link CatalogSummaryHandler} over every schema in a catalog using precomputed {@link
 * CatalogStats}.
 */
final class CatalogSummaryTraverser {

  /**
   * Convenience method that traverses {@code catalog} with a {@link YamlCatalogSummaryHandler} and
   * returns the resulting YAML string.
   *
   * @param catalog Catalog to summarize; must not be {@code null}
   * @return Valid YAML summary of the catalog
   */
  static String toYaml(final Catalog catalog) {
    requireNonNull(catalog, "No catalog provided");
    final YamlCatalogSummaryHandler handler = new YamlCatalogSummaryHandler();
    CatalogSummaryTraverser.traverse(catalog, handler);
    return handler.getYaml();
  }

  /**
   * Traverses {@code catalog} and calls {@code handler} methods in order: {@code begin}, {@code
   * handleHeader} (with catalog-level counts), one {@code handleSchema} per schema, {@code end}.
   *
   * @param catalog Catalog to traverse; must not be {@code null}
   * @param handler Handler to receive traversal events; must not be {@code null}
   */
  private static void traverse(final Catalog catalog, final CatalogSummaryHandler handler) {
    requireNonNull(catalog, "No catalog provided");
    requireNonNull(handler, "No handler provided");

    final CatalogStats catalogStats = CatalogStatsUtility.from(catalog);

    handler.begin();
    handler.handleHeader(catalogStats);
    for (final CatalogStats.SchemaStats schemaStats : catalogStats.schemas()) {
      handler.handleSchema(schemaStats);
    }
    handler.end();
  }

  private CatalogSummaryTraverser() {
    // Prevent instantiation
  }
}
