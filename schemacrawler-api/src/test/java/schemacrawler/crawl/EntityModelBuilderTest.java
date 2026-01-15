/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import schemacrawler.schema.EntityType;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.ForeignKeyCardinality;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaReference;
import us.fatehi.utility.OptionalBoolean;

public class EntityModelBuilderTest {

  @Test
  public void testUpdateForeignKeyCardinality() {
    final Schema schema = new SchemaReference("catalog", "schema");
    final MutableTable table = new MutableTable(schema, "table");
    final MutableColumn pkColumn = new MutableColumn(table, "pk");
    final MutableColumn fkColumn = new MutableColumn(table, "fk");
    final ImmutableColumnReference columnReference =
        new ImmutableColumnReference(1, fkColumn, pkColumn);
    final MutableForeignKey fk = new MutableForeignKey("fk_name", columnReference);

    final EntityModelBuilder builder = EntityModelBuilder.builder();
    builder.updateForeignKeyCardinality(fk, ForeignKeyCardinality.one_one);

    assertThat(fk.getForeignKeyCardinality(), is(ForeignKeyCardinality.one_one));
  }

  @Test
  public void testUpdateForeignKeyCardinalityNonMutable() {
    final ForeignKey fk = mock(ForeignKey.class);
    final EntityModelBuilder builder = EntityModelBuilder.builder();
    assertDoesNotThrow(
        () -> builder.updateForeignKeyCardinality(fk, ForeignKeyCardinality.one_one));
  }

  @Test
  public void testUpdateForeignKeyIndexCoverage() {
    final Schema schema = new SchemaReference("catalog", "schema");
    final MutableTable table = new MutableTable(schema, "table");
    final MutableColumn pkColumn = new MutableColumn(table, "pk");
    final MutableColumn fkColumn = new MutableColumn(table, "fk");
    final ImmutableColumnReference columnReference =
        new ImmutableColumnReference(1, fkColumn, pkColumn);
    final MutableForeignKey fk = new MutableForeignKey("fk_name", columnReference);

    final EntityModelBuilder builder = EntityModelBuilder.builder();
    builder.updateForeignKeyIndexCoverage(fk, OptionalBoolean.true_value);

    assertThat(fk.hasIndex(), is(OptionalBoolean.true_value));
  }

  @Test
  public void testUpdateForeignKeyIndexCoverageNonMutable() {
    final ForeignKey fk = mock(ForeignKey.class);
    final EntityModelBuilder builder = EntityModelBuilder.builder();
    assertDoesNotThrow(() -> builder.updateForeignKeyIndexCoverage(fk, OptionalBoolean.true_value));
  }

  @Test
  public void testUpdateForeignKeyUniqueIndexCoverage() {
    final Schema schema = new SchemaReference("catalog", "schema");
    final MutableTable table = new MutableTable(schema, "table");
    final MutableColumn pkColumn = new MutableColumn(table, "pk");
    final MutableColumn fkColumn = new MutableColumn(table, "fk");
    final ImmutableColumnReference columnReference =
        new ImmutableColumnReference(1, fkColumn, pkColumn);
    final MutableForeignKey fk = new MutableForeignKey("fk_name", columnReference);

    final EntityModelBuilder builder = EntityModelBuilder.builder();
    builder.updateForeignKeyUniqueIndexCoverage(fk, OptionalBoolean.true_value);

    assertThat(fk.hasUniqueIndex(), is(OptionalBoolean.true_value));
  }

  @Test
  public void testUpdateForeignKeyUniqueIndexCoverageNonMutable() {
    final ForeignKey fk = mock(ForeignKey.class);
    final EntityModelBuilder builder = EntityModelBuilder.builder();
    assertDoesNotThrow(
        () -> builder.updateForeignKeyUniqueIndexCoverage(fk, OptionalBoolean.true_value));
  }

  @Test
  public void testUpdateTableEntity() {
    final Schema schema = new SchemaReference("catalog", "schema");
    final MutableTable table = new MutableTable(schema, "table");

    final EntityModelBuilder builder = EntityModelBuilder.builder();
    builder.updateTableEntity(table, EntityType.strong_entity);

    assertThat(table.getEntityType(), is(EntityType.strong_entity));
  }

  @Test
  public void testUpdateTableEntityNonMutable() {
    final Table table = mock(Table.class);
    final EntityModelBuilder builder = EntityModelBuilder.builder();
    assertDoesNotThrow(() -> builder.updateTableEntity(table, EntityType.strong_entity));
  }
}
