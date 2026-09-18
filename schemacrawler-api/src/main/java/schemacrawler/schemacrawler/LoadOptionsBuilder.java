/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schemacrawler;

import us.fatehi.utility.OptionsBuilder;

public final class LoadOptionsBuilder implements OptionsBuilder<LoadOptionsBuilder, LoadOptions> {

  public static LoadOptionsBuilder builder() {
    return new LoadOptionsBuilder();
  }

  public static LoadOptions newLoadOptions() {
    return builder().toOptions();
  }

  private SchemaInfoLevel schemaInfoLevel;

  /** Default options. */
  private LoadOptionsBuilder() {
    schemaInfoLevel = SchemaInfoLevelBuilder.standard();
  }

  @Override
  public LoadOptionsBuilder fromOptions(final LoadOptions options) {
    if (options == null) {
      return this;
    }

    schemaInfoLevel = options.schemaInfoLevel();

    return this;
  }

  @Override
  public LoadOptions toOptions() {
    return new LoadOptions(schemaInfoLevel);
  }

  public LoadOptionsBuilder withInfoLevel(final InfoLevel infoLevel) {
    if (infoLevel != null) {
      schemaInfoLevel = infoLevel.toSchemaInfoLevel();
    }
    return this;
  }

  public LoadOptionsBuilder withSchemaInfoLevel(final SchemaInfoLevel schemaInfoLevel) {
    if (schemaInfoLevel != null) {
      this.schemaInfoLevel = schemaInfoLevel;
    }
    return this;
  }

  public LoadOptionsBuilder withSchemaInfoLevelBuilder(
      final SchemaInfoLevelBuilder schemaInfoLevelBuilder) {
    if (schemaInfoLevelBuilder != null) {
      schemaInfoLevel = schemaInfoLevelBuilder.toOptions();
    }
    return this;
  }
}
