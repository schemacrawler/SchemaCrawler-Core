/*
 * SchemaCrawler Scribe
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */
package schemacrawler.tools.utility;

import java.util.function.Function;

public record TableTraits(
    Boolean noPrimaryKey,
    Boolean noForeignKeys,
    Boolean noIndexes,
    Boolean selfReferencing,
    Boolean hasTriggers,
    Boolean emptyTable,
    EntityModelType entityModelType) {

  private static final Function<Boolean, Boolean> makeTrueOrNull =
      booleanValue -> booleanValue == null || !booleanValue ? null : Boolean.TRUE;

  public TableTraits() {
    this(null, null, null, null, null, null, null);
  }

  /**
   * NOTE: A null value means either false or unknown. A null value is not serialized into JSON -
   * only true and known values are.
   */
  public TableTraits {
    noPrimaryKey = makeTrueOrNull.apply(noPrimaryKey);
    noForeignKeys = makeTrueOrNull.apply(noForeignKeys);
    noIndexes = makeTrueOrNull.apply(noIndexes);
    selfReferencing = makeTrueOrNull.apply(selfReferencing);
    hasTriggers = makeTrueOrNull.apply(hasTriggers);
    emptyTable = makeTrueOrNull.apply(emptyTable);
    if (entityModelType == EntityModelType.unknown) {
      entityModelType = null;
    }
  }
}
