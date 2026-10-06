/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static schemacrawler.test.utility.DatabaseTestUtility.getCatalog;
import static us.fatehi.test.utility.extensions.FileHasContent.classpathResource;
import static us.fatehi.test.utility.extensions.FileHasContent.hasSameContentAs;
import static us.fatehi.test.utility.extensions.FileHasContent.outputOf;

import java.sql.Connection;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import schemacrawler.filter.CatalogProjectionBuilder;
import schemacrawler.filter.NamedObjectFilters;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.Routine;
import schemacrawler.schema.RoutineParameter;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.FilterOptionsBuilder;
import schemacrawler.schemacrawler.GrepOptionsBuilder;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.ProjectionOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.test.utility.WithTestDatabase;
import us.fatehi.test.utility.TestWriter;
import us.fatehi.test.utility.extensions.ResolveTestContext;
import us.fatehi.test.utility.extensions.TestContext;

@WithTestDatabase
@ResolveTestContext
public class SchemaCrawlerGrepTest {

  @Test
  public void grepColumns(final TestContext testContext, final Connection connection)
      throws Exception {
    final TestWriter testout = new TestWriter();
    try (final TestWriter out = testout) {
      final GrepOptionsBuilder grepOptionsBuilder =
          GrepOptionsBuilder.builder()
              .includeGreppedColumns(new RegularExpressionInclusionRule(".*\\..*\\.BOOKID"));
      final SchemaCrawlerOptions schemaCrawlerOptions =
          SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
              .withGrepOptions(grepOptionsBuilder.toOptions());

      final Catalog baseline = getCatalog(connection, schemaCrawlerOptions);
      final Schema booksSchema = baseline.lookupSchema("PUBLIC.BOOKS").orElseThrow();
      final Table bookAuthors = baseline.lookupTable(booksSchema, "BOOKAUTHORS").orElseThrow();
      assertThat(
          NamedObjectFilters.tableGrep(grepOptionsBuilder.toOptions()).test(bookAuthors), is(true));
      assertThat(
          CatalogProjectionBuilder.builder(baseline)
              .build()
              .lookupTable(booksSchema, "BOOKAUTHORS")
              .isPresent(),
          is(true));

      final Catalog catalog =
          CatalogProjectionBuilder.builder(baseline)
              .withOptions(
                  ProjectionOptionsBuilder.builder()
                      .withGrepOptions(grepOptionsBuilder.toOptions())
                      .toOptions())
              .build();
      final Schema[] schemas = catalog.getSchemas().toArray(new Schema[0]);
      assertThat("Schema count does not match", schemas, arrayWithSize(6));
      for (final Schema schema : schemas) {
        out.println("schema: " + schema.getFullName());
        final Table[] tables = catalog.getTables(schema).toArray(new Table[0]);
        for (final Table table : tables) {
          out.println("  table: " + table.getFullName());
          final Column[] columns = table.getColumns().toArray(new Column[0]);
          Arrays.sort(columns);
          for (final Column column : columns) {
            out.println("    column: " + column.getFullName());
          }
        }
      }
    }
    assertThat(
        outputOf(testout), hasSameContentAs(classpathResource(testContext.testMethodFullName())));
  }

  @Test
  public void grepColumnsAndIncludeParentTables(final Connection connection) throws Exception {
    final GrepOptionsBuilder grepOptionsBuilder =
        GrepOptionsBuilder.builder()
            .includeGreppedColumns(new RegularExpressionInclusionRule(".*\\.BOOKAUTHORS\\..*"));
    final FilterOptionsBuilder filterOptionsBuilder =
        FilterOptionsBuilder.builder().parentTableFilterDepth(1);
    SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
            .withGrepOptions(grepOptionsBuilder.toOptions());

    Catalog catalog;
    Schema schema;
    Table table;

    catalog = projectCatalog(connection, schemaCrawlerOptions);
    schema = catalog.lookupSchema("PUBLIC.BOOKS").get();
    assertThat("Schema PUBLIC.BOOKS not found", schema, notNullValue());
    assertThat(catalog.getTables(schema), hasSize(1));
    table = catalog.lookupTable(schema, "BOOKAUTHORS").get();
    assertThat("Table BOOKAUTHORS not found", table, notNullValue());

    schemaCrawlerOptions = schemaCrawlerOptions.withFilterOptions(filterOptionsBuilder.toOptions());
    catalog = projectCatalog(connection, schemaCrawlerOptions);
    schema = catalog.lookupSchema("PUBLIC.BOOKS").get();
    assertThat("Schema PUBLIC.BOOKS not found", schema, notNullValue());
    assertThat(catalog.getTables(schema).size(), is(3));
    table = catalog.lookupTable(schema, "BOOKAUTHORS").get();
    assertThat("Table BOOKAUTHORS not found", table, notNullValue());
    table = catalog.lookupTable(schema, "BOOKS").get();
    assertThat("Table BOOKS not found", table, notNullValue());
    table = catalog.lookupTable(schema, "AUTHORS").get();
    assertThat("Table AUTHORS not found", table, notNullValue());
  }

  @Test
  public void grepCombined(final TestContext testContext, final Connection connection)
      throws Exception {
    final TestWriter testout = new TestWriter();
    try (final TestWriter out = testout) {
      final GrepOptionsBuilder grepOptionsBuilder =
          GrepOptionsBuilder.builder()
              .includeGreppedColumns(new RegularExpressionInclusionRule(".*\\..*\\.BOOKID"))
              .includeGreppedDefinitions(new RegularExpressionInclusionRule(".*book author.*"));
      final SchemaCrawlerOptions schemaCrawlerOptions =
          SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
              .withGrepOptions(grepOptionsBuilder.toOptions());

      final Catalog catalog = projectCatalog(connection, schemaCrawlerOptions);
      final Schema[] schemas = catalog.getSchemas().toArray(new Schema[0]);
      assertThat("Schema count does not match", schemas, arrayWithSize(6));
      for (final Schema schema : schemas) {
        out.println("schema: " + schema.getFullName());
        final Table[] tables = catalog.getTables(schema).toArray(new Table[0]);
        for (final Table table : tables) {
          out.println("  table: " + table.getFullName());
          final Column[] columns = table.getColumns().toArray(new Column[0]);
          Arrays.sort(columns);
          for (final Column column : columns) {
            out.println("    column: " + column.getFullName());
          }
        }
      }
    }
    assertThat(
        outputOf(testout), hasSameContentAs(classpathResource(testContext.testMethodFullName())));
  }

  @Test
  public void grepDefinitions(final TestContext testContext, final Connection connection)
      throws Exception {
    final TestWriter testout = new TestWriter();
    try (final TestWriter out = testout) {
      final GrepOptionsBuilder grepOptionsBuilder =
          GrepOptionsBuilder.builder()
              .includeGreppedDefinitions(new RegularExpressionInclusionRule(".*book author.*"));
      final SchemaCrawlerOptions schemaCrawlerOptions =
          SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
              .withGrepOptions(grepOptionsBuilder.toOptions());

      final Catalog catalog = projectCatalog(connection, schemaCrawlerOptions);
      final Schema[] schemas = catalog.getSchemas().toArray(new Schema[0]);
      assertThat("Schema count does not match", schemas, arrayWithSize(6));
      for (final Schema schema : schemas) {
        out.println("schema: " + schema.getFullName());
        final Table[] tables = catalog.getTables(schema).toArray(new Table[0]);
        for (final Table table : tables) {
          out.println("  table: " + table.getFullName());
          final Column[] columns = table.getColumns().toArray(new Column[0]);
          Arrays.sort(columns);
          for (final Column column : columns) {
            out.println("    column: " + column.getFullName());
          }
        }
      }
    }
    assertThat(
        outputOf(testout), hasSameContentAs(classpathResource(testContext.testMethodFullName())));
  }

  @Test
  public void grepProcedures(final TestContext testContext, final Connection connection)
      throws Exception {
    final TestWriter testout = new TestWriter();
    try (final TestWriter out = testout) {
      final LimitOptionsBuilder limitOptionsBuilder =
          LimitOptionsBuilder.builder().includeAllRoutines();
      final GrepOptionsBuilder grepOptionsBuilder =
          GrepOptionsBuilder.builder()
              .includeGreppedRoutineParameters(new RegularExpressionInclusionRule(".*\\.B_COUNT"));
      final SchemaCrawlerOptions schemaCrawlerOptions =
          SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
              .withLimitOptions(limitOptionsBuilder.toOptions())
              .withGrepOptions(grepOptionsBuilder.toOptions());

      final Catalog catalog = projectCatalog(connection, schemaCrawlerOptions);
      final Schema[] schemas = catalog.getSchemas().toArray(new Schema[0]);
      assertThat("Schema count does not match", schemas, arrayWithSize(6));
      for (final Schema schema : schemas) {
        out.println("schema: " + schema.getFullName());
        final Routine[] routines = catalog.getRoutines(schema).toArray(new Routine[0]);
        for (final Routine routine : routines) {
          out.println("  routine: " + routine.getFullName());
          final RoutineParameter[] parameters =
              routine.getParameters().toArray(new RoutineParameter[0]);
          for (final RoutineParameter column : parameters) {
            out.println("    parameter: " + column.getFullName());
          }
        }
      }
    }
    assertThat(
        outputOf(testout), hasSameContentAs(classpathResource(testContext.testMethodFullName())));
  }

  private Catalog projectCatalog(final Connection connection, final SchemaCrawlerOptions options) {
    final Catalog baseline = getCatalog(connection, options);
    return CatalogProjectionBuilder.builder(baseline)
        .withOptions(
            ProjectionOptionsBuilder.builder()
                .withFilterOptions(options.filterOptions())
                .withGrepOptions(options.grepOptions())
                .toOptions())
        .build();
  }
}
