/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.catalog.summary;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import schemacrawler.loader.catalog.summary.CatalogSummaryHandler.CatalogCounts;
import schemacrawler.loader.catalog.summary.CatalogSummaryHandler.SchemaCounts;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Schema;

/**
 * Drives a {@link CatalogSummaryHandler} over every schema in a catalog. Pre-computes {@link
 * SchemaCounts} for each schema in a single pass, derives {@link CatalogCounts} from those totals,
 * then invokes the handler methods in order.
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

    final Collection<Schema> schemas = catalog.getSchemas();
    final List<Schema> schemaList = new ArrayList<>(schemas);
    final List<SchemaCounts> allCounts = new ArrayList<>(schemaList.size());
    for (final Schema schema : schemaList) {
      allCounts.add(SchemaCounts.from(catalog, schema));
    }
    final CatalogCounts catalogCounts = CatalogCounts.from(allCounts);

    handler.begin();
    handler.handleHeader(catalog.getName(), catalog.getCrawlInfo(), catalogCounts);
    for (int i = 0; i < schemaList.size(); i++) {
      handler.handleSchema(schemaList.get(i), allCounts.get(i));
    }
    handler.end();
  }

  private CatalogSummaryTraverser() {
    // Prevent instantiation
  }
}
