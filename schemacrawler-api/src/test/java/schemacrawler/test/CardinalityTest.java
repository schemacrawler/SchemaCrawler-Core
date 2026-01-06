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
import static org.junit.jupiter.api.Assertions.fail;
import static schemacrawler.schema.IdentifierQuotingStrategy.quote_all;
import static schemacrawler.test.utility.DatabaseTestUtility.getCatalog;
import static schemacrawler.test.utility.DatabaseTestUtility.schemaCrawlerOptionsWithMaximumSchemaInfoLevel;

import java.sql.Connection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Identifiers;
import schemacrawler.schema.IdentifiersBuilder;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.test.utility.WithTestDatabase;
import schemacrawler.utility.MetaDataUtility;
import schemacrawler.utility.MetaDataUtility.ForeignKeyCardinality;
import us.fatehi.test.utility.extensions.ResolveTestContext;

@WithTestDatabase(script = "/cardinality.sql")
@ResolveTestContext
@TestInstance(Lifecycle.PER_CLASS)
public class CardinalityTest {

  private static final Identifiers identifiers =
      IdentifiersBuilder.builder()
          .withIdentifierQuotingStrategy(quote_all)
          .withIdentifierQuoteString("'")
          .toOptions();
  private Catalog catalog;

  @Test
  public void findForeignKeyCardinality() throws Exception {
    final Schema schema = catalog.lookupSchema("PUBLIC.PUBLIC").get();

    // zero_one: Unique, Nullable
    final Table zeroOneTable = catalog.lookupTable(schema, "ZEROONECHILD").get();
    final ForeignKey zeroOneFk = zeroOneTable.getForeignKeys().iterator().next();
    assertThat(
        MetaDataUtility.findForeignKeyCardinality(zeroOneFk), is(ForeignKeyCardinality.zero_one));

    // one_one: Unique, Not Null
    final Table oneOneTable = catalog.lookupTable(schema, "ONEONECHILD").get();
    final ForeignKey oneOneFk = oneOneTable.getForeignKeys().iterator().next();
    assertThat(
        MetaDataUtility.findForeignKeyCardinality(oneOneFk), is(ForeignKeyCardinality.one_one));

    // zero_many: Not Unique, Nullable
    final Table zeroManyTable = catalog.lookupTable(schema, "ZEROMANYCHILD").get();
    final ForeignKey zeroManyFk = zeroManyTable.getForeignKeys().iterator().next();
    assertThat(
        MetaDataUtility.findForeignKeyCardinality(zeroManyFk), is(ForeignKeyCardinality.zero_many));

    // one_many: Not Unique, Not Null
    final Table oneManyTable = catalog.lookupTable(schema, "ONEMANYCHILD").get();
    final ForeignKey oneManyFk = oneManyTable.getForeignKeys().iterator().next();
    assertThat(
        MetaDataUtility.findForeignKeyCardinality(oneManyFk), is(ForeignKeyCardinality.one_many));

    // unknown
    assertThat(MetaDataUtility.findForeignKeyCardinality(null), is(ForeignKeyCardinality.unknown));
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
}
