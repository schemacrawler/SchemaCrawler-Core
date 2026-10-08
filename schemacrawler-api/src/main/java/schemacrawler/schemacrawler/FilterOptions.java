/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schemacrawler;

import us.fatehi.utility.Options;

/** Options controlling post-load catalog filters and related-table expansion. */
public record FilterOptions(
    int childTableFilterDepth, int parentTableFilterDepth, boolean omitEmptyTables)
    implements Options {

  /**
   * Canonical constructor with validation.
   *
   * @param childTableFilterDepth depth for child tables; must be >= 0
   * @param parentTableFilterDepth depth for parent tables; must be >= 0
   * @param omitEmptyTables Whether to exclude tables with a known row count of zero
   */
  public FilterOptions {
    if (childTableFilterDepth < 0) {
      throw new IllegalArgumentException(
          "Invalid child table filter depth, " + childTableFilterDepth);
    }
    if (parentTableFilterDepth < 0) {
      throw new IllegalArgumentException(
          "Invalid parent table filter depth, " + parentTableFilterDepth);
    }
  }
}
