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
import static schemacrawler.test.utility.crawl.LightColumnDataTypeUtility.enumColumnDataType;

import java.sql.Connection;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import schemacrawler.ermodel.model.EntityAttributeType;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.ermodel.model.RelationshipCardinality;
import schemacrawler.ermodel.utility.ERModelUtility;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.test.utility.WithTestDatabase;
import schemacrawler.test.utility.crawl.LightColumnDataTypeUtility;
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
    assertThat(ERModelUtility.inferCardinality(zeroOneFk), is(RelationshipCardinality.zero_one));

    // one_one: Unique, Not Null
    final Table oneOneTable = catalog.lookupTable(schema, "ONEONECHILD").get();
    final ForeignKey oneOneFk = oneOneTable.getForeignKeys().iterator().next();
    assertThat(ERModelUtility.inferCardinality(oneOneFk), is(RelationshipCardinality.one_one));

    // zero_many: Not Unique, Nullable
    final Table zeroManyTable = catalog.lookupTable(schema, "ZEROMANYCHILD").get();
    final ForeignKey zeroManyFk = zeroManyTable.getForeignKeys().iterator().next();
    assertThat(ERModelUtility.inferCardinality(zeroManyFk), is(RelationshipCardinality.zero_many));

    // one_many: Not Unique, Not Null
    final Table oneManyTable = catalog.lookupTable(schema, "ONEMANYCHILD").get();
    final ForeignKey oneManyFk = oneManyTable.getForeignKeys().iterator().next();
    assertThat(ERModelUtility.inferCardinality(oneManyFk), is(RelationshipCardinality.one_many));

    // unknown
    assertThat(ERModelUtility.inferCardinality(null), is(RelationshipCardinality.unknown));
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
    assertThat(ERModelUtility.coveredByIndex(zeroOneFk), is(OptionalBoolean.true_value));

    final Table zeroManyTable = catalog.lookupTable(schema, "ZEROMANYCHILD").get();
    final ForeignKey zeroManyFk = zeroManyTable.getForeignKeys().iterator().next();

    assertThat(ERModelUtility.coveredByIndex(zeroManyFk), is(OptionalBoolean.true_value));
    assertThat(ERModelUtility.coveredByIndex(null), is(OptionalBoolean.unknown));
  }

  @Test
  public void testCoveredByUniqueIndex() {
    final Schema schema = catalog.lookupSchema("PUBLIC.PUBLIC").get();

    final Table zeroOneTable = catalog.lookupTable(schema, "ZEROONECHILD").get();
    final ForeignKey zeroOneFk = zeroOneTable.getForeignKeys().iterator().next();
    assertThat(ERModelUtility.coveredByUniqueIndex(zeroOneFk), is(OptionalBoolean.true_value));

    final Table zeroManyTable = catalog.lookupTable(schema, "ZEROMANYCHILD").get();
    final ForeignKey zeroManyFk = zeroManyTable.getForeignKeys().iterator().next();
    assertThat(ERModelUtility.coveredByUniqueIndex(zeroManyFk), is(OptionalBoolean.false_value));

    assertThat(ERModelUtility.coveredByUniqueIndex(null), is(OptionalBoolean.unknown));
  }

  @Test
  public void testInferBridgeTable() {
    final Schema schema = catalog.lookupSchema("PUBLIC.PUBLIC").get();

    final Table bridgeTable = catalog.lookupTable(schema, "BRIDGE").get();
    assertThat(ERModelUtility.inferBridgeTable(bridgeTable), is(OptionalBoolean.true_value));

    final Table parentTable = catalog.lookupTable(schema, "PARENT").get();
    assertThat(ERModelUtility.inferBridgeTable(parentTable), is(OptionalBoolean.false_value));

    assertThat(ERModelUtility.inferBridgeTable(null), is(OptionalBoolean.unknown));
  }

  @Test
  public void testInferEntityType() {
    final Schema schema = catalog.lookupSchema("PUBLIC.PUBLIC").get();

    final Table parentTable = catalog.lookupTable(schema, "PARENT").get();
    assertThat(ERModelUtility.inferEntityType(parentTable), is(EntityType.strong_entity));

    assertThat(ERModelUtility.inferEntityType(null), is(EntityType.unknown));
  }

  @Test
  public void inferEntityAttributeType() {
    Map<String, EntityAttributeType> attributeTypes =
        Map.of(
            "INTEGER",
            EntityAttributeType.integer,
            "decimal",
            EntityAttributeType.decimal,
            "OTHER",
            EntityAttributeType.other,
            "LONGVARBINARY",
            EntityAttributeType.binary,
            "BOOLEAN",
            EntityAttributeType.bool,
            "LONGNVARCHAR",
            EntityAttributeType.string,
            "DATE",
            EntityAttributeType.date,
            "TIME_WITH_TIMEZONE",
            EntityAttributeType.time,
            "TIMESTAMP",
            EntityAttributeType.timestamp,
            "NO_DATA_TYPE",
            EntityAttributeType.unknown);
    for (Entry<String, EntityAttributeType> attributeTypeCase : attributeTypes.entrySet()) {
      assertThat(
          "Wrong attribute type for <%s>".formatted(attributeTypeCase.getKey()),
          ERModelUtility.inferEntityAttributeType(
              LightColumnDataTypeUtility.columnDataType(attributeTypeCase.getKey())),
          is(attributeTypeCase.getValue()));
    }

    assertThat(ERModelUtility.inferEntityAttributeType(null), is(EntityAttributeType.unknown));

    final ColumnDataType enumeratedDataType = enumColumnDataType();
    assertThat(
        ERModelUtility.inferEntityAttributeType(enumeratedDataType),
        is(EntityAttributeType.enumerated));
  }
}
