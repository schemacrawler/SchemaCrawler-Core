/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schemacrawler;

import us.fatehi.utility.Utility;

public enum SchemaInfoMetadataRetrievalStrategy {
  foreignKeysRetrievalStrategy,
  functionParametersRetrievalStrategy,
  functionsRetrievalStrategy,
  indexesRetrievalStrategy,
  primaryKeysRetrievalStrategy,
  routinesRetrievalStrategy,
  routineReferencesRetrievalStrategy,
  proceduresRetrievalStrategy,
  procedureParametersRetrievalStrategy,
  tableColumnPrivilegesRetrievalStrategy,
  tableColumnsRetrievalStrategy,
  tableAdditionalAttributesRetrievalStrategy,
  tableColumnAdditionalAttributesRetrievalStrategy,
  tablePrivilegesRetrievalStrategy,
  tablesRetrievalStrategy,
  triggersRetrievalStrategy,
  tableConstraintsRetrievalStrategy,
  tableConstraintColumnsRetrievalStrategy,
  tableCheckConstraintsRetrievalStrategy,
  typeInfoRetrievalStrategy,
  viewInformationRetrievalStrategy,
  viewTableUsageRetrievalStrategy,
  ;

  /**
   * Find the enumeration value corresponding to the string.
   *
   * @param key Retrieval strategy key.
   * @return Enumeration value
   */
  public static SchemaInfoMetadataRetrievalStrategy valueOfFromKey(final String key) {
    for (final SchemaInfoMetadataRetrievalStrategy retrievalStrategy :
        SchemaInfoMetadataRetrievalStrategy.values()) {
      if (retrievalStrategy.getKey().equalsIgnoreCase(key)) {
        return retrievalStrategy;
      }
    }
    return null;
  }

  public String getKey() {
    final String identifier = name().replace("RetrievalStrategy", "");
    return Utility.toKebabCase(identifier);
  }
}
