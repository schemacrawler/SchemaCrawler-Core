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
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.ForeignKeyCardinality;
import schemacrawler.schema.Schema;
import schemacrawler.schemacrawler.SchemaReference;

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
}
