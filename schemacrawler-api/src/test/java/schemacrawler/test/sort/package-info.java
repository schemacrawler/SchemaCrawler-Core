/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

/**
 * Explicit schema sorting and comparison contracts, including index and projection ordering.
 *
 * <p>Use production model objects from {@link schemacrawler.crawl.SortTestUtility}, or crawled
 * catalogs, rather than Light* fixtures: their compareTo implementations do not model production
 * natural order. The test-only factory keeps package-private implementations in schemacrawler.crawl
 * and exposes only public schema interfaces. Mocks are reserved for otherwise unreachable null
 * fields and comparison failures.
 */
package schemacrawler.test.sort;
