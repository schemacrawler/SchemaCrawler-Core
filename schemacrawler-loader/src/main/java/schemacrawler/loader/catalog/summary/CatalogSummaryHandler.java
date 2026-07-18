/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.catalog.summary;

/**
 * Handler interface for catalog summary traversal. Implementations receive structured count data
 * for each schema and catalog-level aggregates through {@link CatalogStats}.
 */
interface CatalogSummaryHandler {

  void begin();

  void end();

  void handleHeader(CatalogStats catalogStats);

  void handleSchema(CatalogStats.SchemaStats schemaStats);
}
