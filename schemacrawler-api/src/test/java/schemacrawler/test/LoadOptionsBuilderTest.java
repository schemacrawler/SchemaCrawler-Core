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
import static org.hamcrest.Matchers.sameInstance;

import org.junit.jupiter.api.Test;
import schemacrawler.schemacrawler.InfoLevel;
import schemacrawler.schemacrawler.LoadOptions;
import schemacrawler.schemacrawler.LoadOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevel;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;

public class LoadOptionsBuilderTest {

  @Test
  public void fromOptions() {
    final LoadOptions options =
        LoadOptionsBuilder.builder().withInfoLevel(InfoLevel.detailed).toOptions();

    final LoadOptionsBuilder builder = LoadOptionsBuilder.builder().fromOptions(options);
    assertThat(builder.toOptions().schemaInfoLevel(), is(options.schemaInfoLevel()));

    final LoadOptionsBuilder builderNull = LoadOptionsBuilder.builder().fromOptions(null);
    assertThat(builderNull.toOptions().schemaInfoLevel(), is(SchemaInfoLevelBuilder.standard()));
  }

  @Test
  public void newOptions() {
    assertThat(
        LoadOptionsBuilder.newLoadOptions().schemaInfoLevel(),
        is(SchemaInfoLevelBuilder.standard()));
  }

  @Test
  public void withInfoLevel() {
    final LoadOptionsBuilder builder = LoadOptionsBuilder.builder();

    assertThat(builder.withInfoLevel(InfoLevel.maximum), is(sameInstance(builder)));
    assertThat(builder.toOptions().schemaInfoLevel(), is(SchemaInfoLevelBuilder.maximum()));

    builder.withInfoLevel(null);
    assertThat(builder.toOptions().schemaInfoLevel(), is(SchemaInfoLevelBuilder.maximum()));
  }

  @Test
  public void withSchemaInfoLevel() {
    final LoadOptionsBuilder builder = LoadOptionsBuilder.builder();
    final SchemaInfoLevel schemaInfoLevel = SchemaInfoLevelBuilder.minimum();

    assertThat(builder.withSchemaInfoLevel(schemaInfoLevel), is(sameInstance(builder)));
    assertThat(builder.toOptions().schemaInfoLevel(), is(schemaInfoLevel));

    builder.withSchemaInfoLevel(null);
    assertThat(builder.toOptions().schemaInfoLevel(), is(schemaInfoLevel));
  }

  @Test
  public void withSchemaInfoLevelBuilder() {
    final LoadOptionsBuilder builder = LoadOptionsBuilder.builder();
    final SchemaInfoLevelBuilder schemaInfoLevelBuilder =
        SchemaInfoLevelBuilder.builder().withInfoLevel(InfoLevel.standard);

    assertThat(
        builder.withSchemaInfoLevelBuilder(schemaInfoLevelBuilder), is(sameInstance(builder)));
    assertThat(builder.toOptions().schemaInfoLevel(), is(schemaInfoLevelBuilder.toOptions()));

    builder.withSchemaInfoLevelBuilder(null);
    assertThat(builder.toOptions().schemaInfoLevel(), is(schemaInfoLevelBuilder.toOptions()));
  }
}
