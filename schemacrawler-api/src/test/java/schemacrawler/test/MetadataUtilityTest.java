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
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.fail;
import static schemacrawler.schema.IdentifierQuotingStrategy.quote_all;
import static schemacrawler.test.utility.DatabaseTestUtility.getCatalog;
import static schemacrawler.test.utility.DatabaseTestUtility.schemaCrawlerOptionsWithMaximumSchemaInfoLevel;
import static us.fatehi.test.utility.extensions.FileHasContent.classpathResource;
import static us.fatehi.test.utility.extensions.FileHasContent.hasSameContentAs;
import static us.fatehi.test.utility.extensions.FileHasContent.outputOf;

import java.sql.Connection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Identifiers;
import schemacrawler.schema.IdentifiersBuilder;
import schemacrawler.schema.Index;
import schemacrawler.schema.PrimaryKey;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.test.utility.WithTestDatabase;
import schemacrawler.utility.MetaDataUtility;
import us.fatehi.test.utility.TestWriter;
import us.fatehi.test.utility.extensions.ResolveTestContext;
import us.fatehi.test.utility.extensions.TestContext;

@WithTestDatabase
@ResolveTestContext
@TestInstance(Lifecycle.PER_CLASS)
public class MetadataUtilityTest {

  private static final Identifiers identifiers =
      IdentifiersBuilder.builder()
          .withIdentifierQuotingStrategy(quote_all)
          .withIdentifierQuoteString("'")
          .toOptions();
  private Catalog catalog;

  @Test
  public void columnsListAsStringConstraint() throws Exception {

    final Schema schema = catalog.lookupSchema("PUBLIC.BOOKS").get();
    assertThat("BOOKS Schema not found", schema, notNullValue());

    final Table table = catalog.lookupTable(schema, "BOOKS").get();
    assertThat("BOOKS Table not found", table, notNullValue());

    final PrimaryKey pk = table.getPrimaryKey();
    assertThat("Index not found", pk, notNullValue());

    final String columnsListAsStringChild = MetaDataUtility.getColumnsListAsString(pk, identifiers);
    assertThat(columnsListAsStringChild, is("'ID'"));
  }

  @Test
  public void columnsListAsStringIndex() throws Exception {

    final Schema schema = catalog.lookupSchema("PUBLIC.BOOKS").get();
    assertThat("BOOKS Schema not found", schema, notNullValue());

    final Table table = catalog.lookupTable(schema, "BOOKS").get();
    assertThat("BOOKS Table not found", table, notNullValue());

    final Index index = table.getIndexes().toArray(new Index[0])[0];
    assertThat("Index not found", index, notNullValue());

    final String columnsListAsStringChild =
        MetaDataUtility.getColumnsListAsString(index, identifiers);
    assertThat(columnsListAsStringChild, is("'ID'"));
  }

  @BeforeAll
  public void loadCatalog(final Connection connection) {
    final SchemaCrawlerOptions schemaCrawlerOptions =
        schemaCrawlerOptionsWithMaximumSchemaInfoLevel;
    try {
      catalog = getCatalog(connection, schemaCrawlerOptions);
    } catch (final Exception e) {
      fail("Catalog not loaded", e);
    }
  }

  @Test
  public void reduceCatalog() throws Exception {

    final LimitOptionsBuilder limitOptionsBuilder = LimitOptionsBuilder.builder();
    limitOptionsBuilder.includeTables(tableName -> !tableName.matches(".*\\.BOOKS"));

    final SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
            .withLimitOptions(limitOptionsBuilder.toOptions());

    // Reduce catalog
    MetaDataUtility.reduceCatalog(catalog, schemaCrawlerOptions);

    final Schema schema = catalog.lookupSchema("PUBLIC.BOOKS").get();
    assertThat("BOOKS Schema not found", schema, notNullValue());

    assertThat("BOOKS Table not found", catalog.lookupTable(schema, "BOOKS").isEmpty());

    // Undo reduce catalog
    MetaDataUtility.reduceCatalog(catalog, SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions());

    final Table table = catalog.lookupTable(schema, "BOOKS").get();
    assertThat("BOOKS Table not found", table, notNullValue());
  }

  @Test
  public void summarizeCatalog(final TestContext testContext) throws Exception {
    final TestWriter testout = new TestWriter();
    try (final TestWriter out = testout) {
      out.write(MetaDataUtility.summarizeCatalog(catalog));
    }
    assertThat(
        outputOf(testout), hasSameContentAs(classpathResource(testContext.testMethodFullName())));
  }
}
