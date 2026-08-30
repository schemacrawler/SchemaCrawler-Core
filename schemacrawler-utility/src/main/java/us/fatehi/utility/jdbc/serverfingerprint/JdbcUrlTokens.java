/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.jdbc.serverfingerprint;

import static us.fatehi.utility.Utility.isBlank;
import static us.fatehi.utility.Utility.trimToEmpty;

record JdbcUrlTokens(
    String databaseSystemIdentifier,
    String host,
    String databaseName,
    HostClassification hostClassification) {

  JdbcUrlTokens {
    databaseSystemIdentifier = normalizeToken(databaseSystemIdentifier);
    host = normalizeToken(host);
    databaseName = normalizeToken(databaseName);
    if (hostClassification == null) {
      hostClassification = HostClassification.UNKNOWN;
    }
  }

  JdbcUrlTokens() {
    this(null, null, null, null);
  }

  boolean hasDatabaseName() {
    return !isBlank(databaseName);
  }

  boolean hasDatabaseSystemIdentifier() {
    return !isBlank(databaseSystemIdentifier);
  }

  boolean hasHost() {
    return !isBlank(host);
  }

  boolean hasPublicHost() {
    return hasHost() && hostClassification == HostClassification.PUBLIC;
  }

  private static String normalizeToken(final String value) {
    return trimToEmpty(value).toLowerCase();
  }
}
