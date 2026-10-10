/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static schemacrawler.utility.TableRowCountsUtility.getRowCount;
import static schemacrawler.utility.TableRowCountsUtility.hasRowCount;
import static us.fatehi.test.utility.extensions.FileHasContent.classpathResource;
import static us.fatehi.test.utility.extensions.FileHasContent.hasSameContentAs;
import static us.fatehi.test.utility.extensions.FileHasContent.outputOf;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import schemacrawler.inclusionrule.RegularExpressionExclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.LoadOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.test.utility.DatabaseTestUtility;
import schemacrawler.test.utility.WithTestDatabase;
import schemacrawler.test.utility.crawl.LightTable;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.options.ConfigUtility;
import schemacrawler.tools.utility.SchemaCrawlerUtility;
import schemacrawler.utility.NamedObjectSort;
import schemacrawler.utility.TableRowCountsUtility;
import us.fatehi.test.utility.TestWriter;
import us.fatehi.test.utility.extensions.ResolveTestContext;
import us.fatehi.test.utility.extensions.TestContext;
import us.fatehi.utility.datasource.DatabaseConnectionSource;

@WithTestDatabase
@ResolveTestContext
public class TableRowCountsTest {

  private static String getRowCountMessage(final long number) {
    if (number <= 0) {
      return "empty";
    }
    return "%,d rows".formatted(number);
  }

  @Test
  public void add() {
    final Table table = new LightTable("table1");

    addRowCountToTable(null, 0);
    assertThat(TableRowCountsUtility.hasRowCount(null), is(false));

    addRowCountToTable(table, 1);
    assertThat(TableRowCountsUtility.hasRowCount(table), is(true));
    assertThat(TableRowCountsUtility.getRowCount(table), is(1L));

    addRowCountToTable(table, 0);
    assertThat(TableRowCountsUtility.hasRowCount(table), is(true));
    assertThat(TableRowCountsUtility.getRowCount(table), is(0L));

    addRowCountToTable(table, -1);
    assertThat(TableRowCountsUtility.hasRowCount(table), is(false));
    assertThat(TableRowCountsUtility.getRowCount(table), is(-1L));
  }

  @Test
  public void rowCountLoadingLeavesEmptyTablesInBaseline(
      final DatabaseConnectionSource connectionSource) throws Exception {
    final SchemaRetrievalOptions schemaRetrievalOptions =
        DatabaseTestUtility.newSchemaRetrievalOptions();
    final SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions();
    final Config additionalConfig = ConfigUtility.newConfig();
    additionalConfig.put("load-row-counts", true);

    final Catalog catalog =
        SchemaCrawlerUtility.getCatalog(
            connectionSource, schemaRetrievalOptions, schemaCrawlerOptions, additionalConfig);

    assertThat(catalog.getTables().stream().allMatch(TableRowCountsUtility::hasRowCount), is(true));
    assertThat(catalog.getTables().stream().anyMatch(table -> getRowCount(table) == 0L), is(true));
  }

  @Test
  public void rowCounts(
      final TestContext testContext, final DatabaseConnectionSource connectionSource)
      throws Exception {
    final TestWriter testout = new TestWriter();
    try (final TestWriter out = testout) {

      final SchemaRetrievalOptions schemaRetrievalOptions =
          DatabaseTestUtility.newSchemaRetrievalOptions();

      final LimitOptionsBuilder limitOptionsBuilder =
          LimitOptionsBuilder.builder()
              .includeSchemas(new RegularExpressionExclusionRule(".*\\.FOR_LINT"));
      final LoadOptionsBuilder loadOptionsBuilder =
          LoadOptionsBuilder.builder().withSchemaInfoLevel(SchemaInfoLevelBuilder.standard());
      final SchemaCrawlerOptions schemaCrawlerOptions =
          SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
              .withLimitOptions(limitOptionsBuilder.toOptions())
              .withLoadOptions(loadOptionsBuilder.toOptions());

      final Config additionalConfig = ConfigUtility.newConfig();
      additionalConfig.put("load-row-counts", true);

      final Catalog catalog =
          SchemaCrawlerUtility.getCatalog(
              connectionSource, schemaRetrievalOptions, schemaCrawlerOptions, additionalConfig);

      final Schema[] schemas = catalog.getSchemas().toArray(new Schema[0]);
      assertThat("Schema count does not match", schemas, arrayWithSize(5));
      for (final Schema schema : schemas) {
        final Table[] tables = catalog.getTables(schema).toArray(new Table[0]);
        Arrays.sort(tables, NamedObjectSort.alphabetical);
        for (final Table table : tables) {
          assertThat(
              "Table <%s> should have row counts".formatted(table), hasRowCount(table), is(true));
          out.println(
              "%s [%s]"
                  .formatted(
                      table.getFullName(),
                      getRowCountMessage(TableRowCountsUtility.getRowCount(table))));
        }
      }
    }
    assertThat(
        outputOf(testout), hasSameContentAs(classpathResource(testContext.testMethodFullName())));
  }

  private void addRowCountToTable(final Table table, final long rowCount) {
    if (table != null) {
      if (rowCount >= 0) {
        table.setAttribute(TableRowCountsUtility.TABLE_ROW_COUNT_KEY, rowCount);
      } else {
        table.removeAttribute(TableRowCountsUtility.TABLE_ROW_COUNT_KEY);
      }
    }
  }
}
