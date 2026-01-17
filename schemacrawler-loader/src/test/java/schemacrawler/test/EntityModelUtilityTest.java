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
import static schemacrawler.test.utility.DatabaseTestUtility.getCatalog;
import static schemacrawler.test.utility.DatabaseTestUtility.schemaCrawlerOptionsWithMaximumSchemaInfoLevel;

import java.sql.Connection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.ermodel.model.ForeignKeyCardinality;
import schemacrawler.ermodel.utility.EntityModelUtility;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.test.utility.WithTestDatabase;
import us.fatehi.test.utility.extensions.ResolveTestContext;
import us.fatehi.utility.OptionalBoolean;

@WithTestDatabase(script = "/cardinality.sql")
@ResolveTestContext
@TestInstance(Lifecycle.PER_CLASS)
public class EntityModelUtilityTest {

  private Catalog catalog;

  @Test
  public void findForeignKeyCardinality() throws Exception {
    final Schema schema = catalog.lookupSchema("PUBLIC.PUBLIC").get();

    // zero_one: Unique, Nullable
    final Table zeroOneTable = catalog.lookupTable(schema, "ZEROONECHILD").get();
    final ForeignKey zeroOneFk = zeroOneTable.getForeignKeys().iterator().next();
    assertThat(EntityModelUtility.inferCardinality(zeroOneFk), is(ForeignKeyCardinality.zero_one));

    // one_one: Unique, Not Null
    final Table oneOneTable = catalog.lookupTable(schema, "ONEONECHILD").get();
    final ForeignKey oneOneFk = oneOneTable.getForeignKeys().iterator().next();
    assertThat(EntityModelUtility.inferCardinality(oneOneFk), is(ForeignKeyCardinality.one_one));

    // zero_many: Not Unique, Nullable
    final Table zeroManyTable = catalog.lookupTable(schema, "ZEROMANYCHILD").get();
    final ForeignKey zeroManyFk = zeroManyTable.getForeignKeys().iterator().next();
    assertThat(
        EntityModelUtility.inferCardinality(zeroManyFk), is(ForeignKeyCardinality.zero_many));

    // one_many: Not Unique, Not Null
    final Table oneManyTable = catalog.lookupTable(schema, "ONEMANYCHILD").get();
    final ForeignKey oneManyFk = oneManyTable.getForeignKeys().iterator().next();
    assertThat(EntityModelUtility.inferCardinality(oneManyFk), is(ForeignKeyCardinality.one_many));

    // unknown
    assertThat(EntityModelUtility.inferCardinality(null), is(ForeignKeyCardinality.unknown));
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
  public void testCoveredByIndex() {
    final Schema schema = catalog.lookupSchema("PUBLIC.PUBLIC").get();

    final Table zeroOneTable = catalog.lookupTable(schema, "ZEROONECHILD").get();
    final ForeignKey zeroOneFk = zeroOneTable.getForeignKeys().iterator().next();
    assertThat(EntityModelUtility.coveredByIndex(zeroOneFk), is(OptionalBoolean.true_value));

    final Table zeroManyTable = catalog.lookupTable(schema, "ZEROMANYCHILD").get();
    final ForeignKey zeroManyFk = zeroManyTable.getForeignKeys().iterator().next();

    assertThat(EntityModelUtility.coveredByIndex(zeroManyFk), is(OptionalBoolean.true_value));
    assertThat(EntityModelUtility.coveredByIndex(null), is(OptionalBoolean.unknown));
  }

  @Test
  public void testCoveredByUniqueIndex() {
    final Schema schema = catalog.lookupSchema("PUBLIC.PUBLIC").get();

    final Table zeroOneTable = catalog.lookupTable(schema, "ZEROONECHILD").get();
    final ForeignKey zeroOneFk = zeroOneTable.getForeignKeys().iterator().next();
    assertThat(EntityModelUtility.coveredByUniqueIndex(zeroOneFk), is(OptionalBoolean.true_value));

    final Table zeroManyTable = catalog.lookupTable(schema, "ZEROMANYCHILD").get();
    final ForeignKey zeroManyFk = zeroManyTable.getForeignKeys().iterator().next();
    assertThat(
        EntityModelUtility.coveredByUniqueIndex(zeroManyFk), is(OptionalBoolean.false_value));

    assertThat(EntityModelUtility.coveredByUniqueIndex(null), is(OptionalBoolean.unknown));
  }

  @Test
  public void testInferEntityType() {
    final Schema schema = catalog.lookupSchema("PUBLIC.PUBLIC").get();

    final Table parentTable = catalog.lookupTable(schema, "PARENT").get();
    assertThat(EntityModelUtility.inferEntityType(parentTable), is(EntityType.strong_entity));

    assertThat(EntityModelUtility.inferEntityType(null), is(EntityType.unknown));
  }
}
