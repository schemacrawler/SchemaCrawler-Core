/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */
package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static schemacrawler.test.utility.DatabaseTestUtility.getCatalog;
import static schemacrawler.test.utility.DatabaseTestUtility.schemaCrawlerOptionsWithMaximumSchemaInfoLevel;

import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import schemacrawler.loader.catalog.summary.CatalogStats;
import schemacrawler.loader.catalog.summary.CatalogStatsUtility;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Table;
import schemacrawler.schema.View;
import schemacrawler.test.utility.WithTestDatabase;
import us.fatehi.utility.datasource.DatabaseConnectionSource;

@WithTestDatabase
public class CatalogStatsUtilityTest {

  @Test
  public void extendedCatalogCounts(final DatabaseConnectionSource connectionSource) {
    final Catalog catalog =
        getCatalog(connectionSource.get(), schemaCrawlerOptionsWithMaximumSchemaInfoLevel);
    final CatalogStats stats = CatalogStatsUtility.from(catalog);

    int expectedTableCount = 0;
    int expectedViewCount = 0;
    final Set<ForeignKey> expectedForeignKeys = new LinkedHashSet<>();
    for (final Table table : catalog.getTables()) {
      if (table instanceof View || table.getTableType().isView()) {
        expectedViewCount++;
      } else {
        expectedTableCount++;
      }
      expectedForeignKeys.addAll(table.getImportedForeignKeys());
    }

    assertThat(stats.counts().tableCount(), is(expectedTableCount));
    assertThat(stats.counts().viewCount(), is(expectedViewCount));
    assertThat(stats.counts().foreignKeyCount(), is(expectedForeignKeys.size()));
    assertThat(
        stats.counts().tableCount() + stats.counts().viewCount(), is(stats.counts().tables()));
  }
}
