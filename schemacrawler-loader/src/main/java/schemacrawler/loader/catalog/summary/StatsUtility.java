/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.catalog.summary;

import java.util.function.Function;
import us.fatehi.utility.UtilityMarker;

/** Utility methods for building counts and statistics. */
@UtilityMarker
public class StatsUtility {

  public static final Function<Integer, Integer> removeNegativeInteger =
      x -> x == null || x < 0 ? null : x;
  public static final Function<Long, Long> makeValidRowCount = x -> x == null || x <= 0 ? null : x;

  private StatsUtility() {
    // Prevent instantiation
  }
}
