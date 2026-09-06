/*
 * SchemaCrawler Scribe
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */
package schemacrawler.tools.utility;

import static schemacrawler.loader.catalog.summary.StatsUtility.makeValidRowCount;
import static schemacrawler.loader.catalog.summary.StatsUtility.removeNegativeInteger;

import java.io.Serializable;

public record TableCounts(
    Integer attributeColumnCount,
    Integer columnCount,
    Integer foreignKeyCount,
    Integer indexCount,
    Integer triggerCount,
    Long rowCount)
    implements Serializable {

  public TableCounts {
    attributeColumnCount = removeNegativeInteger.apply(attributeColumnCount);
    columnCount = removeNegativeInteger.apply(columnCount);
    foreignKeyCount = removeNegativeInteger.apply(foreignKeyCount);
    indexCount = removeNegativeInteger.apply(indexCount);
    triggerCount = removeNegativeInteger.apply(triggerCount);
    rowCount = makeValidRowCount.apply(rowCount);
  }

  public TableCounts() {
    this(null, null, null, null, null, null);
  }
}
