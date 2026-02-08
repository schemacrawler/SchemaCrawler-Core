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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static us.fatehi.test.utility.TestObjectUtility.mockConnection;

import java.sql.Connection;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.Test;
import schemacrawler.schemacrawler.InformationSchemaKey;
import schemacrawler.schemacrawler.InformationSchemaViews;
import schemacrawler.schemacrawler.InformationSchemaViewsBuilder;

public class InformationSchemaViewsBuilderTest {

  @Test
  public void builderFromOptions() {

    final InformationSchemaViews informationSchemaViews =
        InformationSchemaViewsBuilder.builder()
            .withSql(InformationSchemaKey.ADDITIONAL_COLUMN_ATTRIBUTES, "SOME SQL SELECT")
            .toOptions();

    final InformationSchemaViews informationSchemaViews2 =
        InformationSchemaViewsBuilder.builder(informationSchemaViews).toOptions();

    assertThat(
        informationSchemaViews2.getQuery(InformationSchemaKey.ADDITIONAL_COLUMN_ATTRIBUTES).query(),
        is("SOME SQL SELECT"));
  }

  @Test
  public void fromResourceFolder() {

    InformationSchemaViews informationSchemaViews;

    informationSchemaViews =
        InformationSchemaViewsBuilder.builder().fromResourceFolder(null).toOptions();

    assertThat(informationSchemaViews.size(), is(0));

    informationSchemaViews =
        InformationSchemaViewsBuilder.builder().fromResourceFolder("/").toOptions();

    assertThat(informationSchemaViews.size(), is(0));
  }

  @Test
  public void newOptions() {

    InformationSchemaViews informationSchemaViews;

    informationSchemaViews = InformationSchemaViewsBuilder.newInformationSchemaViews();

    assertThat(informationSchemaViews.size(), is(0));

    informationSchemaViews = InformationSchemaViewsBuilder.builder().fromOptions(null).toOptions();

    assertThat(informationSchemaViews.size(), is(0));

    informationSchemaViews =
        InformationSchemaViewsBuilder.builder().fromResourceFolder(null).toOptions();

    assertThat(informationSchemaViews.size(), is(0));
  }

  @Test
  public void substituteAll() {
    final String sql = "SOME SQL SELECT";
    final InformationSchemaViewsBuilder builder =
        InformationSchemaViewsBuilder.builder()
            .withSql(InformationSchemaKey.ADDITIONAL_COLUMN_ATTRIBUTES, sql)
            .withSql(
                InformationSchemaKey.ADDITIONAL_TABLE_ATTRIBUTES,
                "SOME ${key} SUBSTITUTE SQL SELECT");

    builder.substituteAll("key", "value");

    assertThat(
        builder.toOptions().getQuery(InformationSchemaKey.ADDITIONAL_COLUMN_ATTRIBUTES).query(),
        is(sql));

    assertThat(
        builder.toOptions().getQuery(InformationSchemaKey.ADDITIONAL_TABLE_ATTRIBUTES).query(),
        is("SOME value SUBSTITUTE SQL SELECT"));
  }

  @Test
  public void withFunction() {

    final Connection connection = mockConnection();
    final InformationSchemaViewsBuilder builder = InformationSchemaViewsBuilder.builder();
    final BiConsumer<InformationSchemaViewsBuilder, Connection> function =
        (bldr, conn) -> {
          throw new RuntimeException("Forced exception");
        };

    final RuntimeException exception =
        assertThrows(RuntimeException.class, () -> builder.withFunction(function, connection));
    assertThat(exception.getMessage(), is("Forced exception"));

    assertDoesNotThrow(() -> builder.withFunction(null, connection));
  }

  @Test
  public void withSql() {
    final InformationSchemaViewsBuilder builder = InformationSchemaViewsBuilder.builder();

    final String sql = "SOME SQL SELECT";

    builder.withSql(InformationSchemaKey.ADDITIONAL_COLUMN_ATTRIBUTES, sql);

    assertThat(
        builder.toOptions().getQuery(InformationSchemaKey.ADDITIONAL_COLUMN_ATTRIBUTES).query(),
        is(sql));

    builder.withSql(InformationSchemaKey.ADDITIONAL_COLUMN_ATTRIBUTES, null);

    assertThat(
        builder.toOptions().hasQuery(InformationSchemaKey.ADDITIONAL_COLUMN_ATTRIBUTES), is(false));
  }
}
