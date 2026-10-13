/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schemacrawler;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import us.fatehi.utility.string.StringFormat;

public enum InfoLevel {
  unknown(SchemaInfoLevelBuilder::standard),
  minimum(SchemaInfoLevelBuilder::minimum),
  standard(SchemaInfoLevelBuilder::standard),
  detailed(SchemaInfoLevelBuilder::detailed),
  maximum(SchemaInfoLevelBuilder::maximum);

  private static final Logger LOGGER = Logger.getLogger(InfoLevel.class.getName());

  public static InfoLevel valueOfFromString(final String infoLevelValue) {
    try {
      return InfoLevel.valueOf(infoLevelValue);
    } catch (final IllegalArgumentException | NullPointerException e) {
      LOGGER.log(Level.INFO, new StringFormat("Unknown infolevel <%s>", infoLevelValue));
      return unknown;
    }
  }

  private final Supplier<SchemaInfoLevel> toSchemaInfoLevelFunction;

  InfoLevel(final Supplier<SchemaInfoLevel> toSchemaInfoLevelFunction) {
    this.toSchemaInfoLevelFunction = toSchemaInfoLevelFunction;
  }

  public final SchemaInfoLevel toSchemaInfoLevel() {
    return toSchemaInfoLevelFunction.get();
  }
}
