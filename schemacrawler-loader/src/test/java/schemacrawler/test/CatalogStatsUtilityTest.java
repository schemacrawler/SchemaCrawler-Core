/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */
package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static schemacrawler.test.utility.DatabaseTestUtility.getCatalog;
import static schemacrawler.test.utility.DatabaseTestUtility.schemaCrawlerOptionsWithMaximumSchemaInfoLevel;

import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import schemacrawler.loader.utility.CatalogStats;
import schemacrawler.loader.utility.CatalogStats.CatalogCounts;
import schemacrawler.loader.utility.CatalogStats.SchemaStats;
import schemacrawler.loader.utility.CatalogStatsUtility;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Schema;
import schemacrawler.test.utility.WithTestDatabase;
import us.fatehi.utility.datasource.DatabaseConnectionSource;

@WithTestDatabase
public class CatalogStatsUtilityTest {

  @Test
  public void extendedCatalogCounts(final DatabaseConnectionSource connectionSource) {
    final Catalog catalog =
        getCatalog(connectionSource.get(), schemaCrawlerOptionsWithMaximumSchemaInfoLevel);
    final CatalogStats catalogStats = CatalogStatsUtility.from(catalog);
    final CatalogCounts catalogCounts = catalogStats.counts();

    assertThat(catalogCounts.tables(), is(20));
    assertThat(catalogStats.counts().columns(), is(90));
    assertThat(catalogStats.counts().foreignKeys(), is(16));

    assertThat(catalogStats.counts().routines(), is(15));
  }

  @Test
  public void fromThrowsOnNullCatalog() {
    assertThrows(NullPointerException.class, () -> CatalogStatsUtility.from(null));
  }

  @Test
  public void schemaStatsFrom(final DatabaseConnectionSource connectionSource) {
    final Catalog catalog =
        getCatalog(connectionSource.get(), schemaCrawlerOptionsWithMaximumSchemaInfoLevel);

    final Map<String, SchemaStats> schemaStatsBySchemaName =
        CatalogStatsUtility.schemaStatsFrom(catalog).stream()
            .collect(Collectors.toMap(stat -> stat.schema().getFullName(), stat -> stat));

    assertThat(schemaStatsBySchemaName.size(), is(catalog.getSchemas().size()));

    for (final Schema schema : catalog.getSchemas()) {
      final SchemaStats schemaStats = schemaStatsBySchemaName.get(schema.getFullName());
      assertThat(schemaStats, is(notNullValue()));
      assertThat(
          schemaStats.counts().dataTypes().count(), is(catalog.getColumnDataTypes(schema).size()));
      assertThat(schemaStats.counts().tables().count(), is(catalog.getTables(schema).size()));
      assertThat(schemaStats.counts().routines().count(), is(catalog.getRoutines(schema).size()));
      assertThat(schemaStats.counts().synonyms().count(), is(catalog.getSynonyms(schema).size()));
      assertThat(schemaStats.counts().sequences().count(), is(catalog.getSequences(schema).size()));
      assertThat(schemaStats.counts().tables().columns(), is((Integer) null));
      assertThat(schemaStats.counts().tables().foreignKeys(), is((Integer) null));
      assertThat(schemaStats.counts().tables().views(), is((Integer) null));
      assertThat(schemaStats.counts().routines().procedures(), is((Integer) null));
      assertThat(schemaStats.counts().routines().functions(), is((Integer) null));
      assertThat(schemaStats.counts().routines().parameters(), is((Integer) null));
    }
  }

  @Test
  public void schemaStatsFromThrowsOnNullCatalog() {
    assertThrows(NullPointerException.class, () -> CatalogStatsUtility.schemaStatsFrom(null));
  }

  @Test
  public void schemaTableSumsMatchCatalogCounts(final DatabaseConnectionSource connectionSource) {
    final Catalog catalog =
        getCatalog(connectionSource.get(), schemaCrawlerOptionsWithMaximumSchemaInfoLevel);
    final CatalogStats catalogStats = CatalogStatsUtility.from(catalog);

    final int sumOfColumns =
        catalogStats.schemas().stream()
            .map(SchemaStats::counts)
            .map(stats -> stats.tables().columns())
            .mapToInt(Integer::intValue)
            .sum();
    final int sumOfForeignKeys =
        catalogStats.schemas().stream()
            .map(SchemaStats::counts)
            .map(stats -> stats.tables().foreignKeys())
            .mapToInt(Integer::intValue)
            .sum();
    final int sumOfViews =
        catalogStats.schemas().stream()
            .map(SchemaStats::counts)
            .map(stats -> stats.tables().views())
            .mapToInt(Integer::intValue)
            .sum();

    assertThat(catalogStats.counts().columns(), is(sumOfColumns));
    assertThat(catalogStats.counts().foreignKeys(), is(sumOfForeignKeys));
    assertThat(catalogStats.counts().views(), is(sumOfViews));
  }

  @Test
  public void schemaStatsFromMatchesCatalogSchemas(
      final DatabaseConnectionSource connectionSource) {
    final Catalog catalog =
        getCatalog(connectionSource.get(), schemaCrawlerOptionsWithMaximumSchemaInfoLevel);
    final CatalogStats catalogStats = CatalogStatsUtility.from(catalog);
    final Map<String, SchemaStats> statsFromCatalog =
        catalogStats.schemas().stream()
            .collect(Collectors.toMap(stat -> stat.schema().getFullName(), stat -> stat));
    final Map<String, SchemaStats> statsFromSchemaStatsFrom =
        CatalogStatsUtility.schemaStatsFrom(catalog).stream()
            .collect(Collectors.toMap(stat -> stat.schema().getFullName(), stat -> stat));

    assertThat(statsFromSchemaStatsFrom.size(), is(statsFromCatalog.size()));

    for (final Map.Entry<String, SchemaStats> entry : statsFromSchemaStatsFrom.entrySet()) {
      final SchemaStats schemaStatsFrom = entry.getValue();
      final SchemaStats schemaStatsFromCatalog = statsFromCatalog.get(entry.getKey());
      assertThat(schemaStatsFromCatalog, is(notNullValue()));
      assertThat(
          schemaStatsFrom.counts().dataTypes().count(),
          is(schemaStatsFromCatalog.counts().dataTypes().count()));
      assertThat(
          schemaStatsFrom.counts().tables().count(),
          is(schemaStatsFromCatalog.counts().tables().count()));
      assertThat(
          schemaStatsFrom.counts().routines().count(),
          is(schemaStatsFromCatalog.counts().routines().count()));
      assertThat(
          schemaStatsFrom.counts().synonyms().count(),
          is(schemaStatsFromCatalog.counts().synonyms().count()));
      assertThat(
          schemaStatsFrom.counts().sequences().count(),
          is(schemaStatsFromCatalog.counts().sequences().count()));
    }

    assertThat(catalogStats.counts().views(), is(greaterThan(0)));
    assertThat(catalogStats.counts().foreignKeys(), is(greaterThan(0)));
    assertThat(catalogStats.counts().columns(), is(greaterThan(0)));
  }
}
