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

public record JdbcUrl(
    String databaseSystemIdentifier,
    String hostHash,
    String databaseName,
    HostClassification hostClassification) {

  public JdbcUrl {
    databaseSystemIdentifier = normalizeToken(databaseSystemIdentifier);
    hostHash = normalizeToken(hostHash);
    databaseName = normalizeToken(databaseName);
    if (hostClassification == null) {
      hostClassification = HostClassification.UNKNOWN;
    }
  }

  public JdbcUrl() {
    this(null, null, null, null);
  }

  public boolean hasDatabaseName() {
    return !isBlank(databaseName);
  }

  public boolean hasDatabaseSystemIdentifier() {
    return !isBlank(databaseSystemIdentifier);
  }

  public boolean hasHost() {
    return !isBlank(hostHash);
  }

  public boolean hasPublicHost() {
    return hasHost() && hostClassification == HostClassification.PUBLIC;
  }

  private static String normalizeToken(final String value) {
    return trimToEmpty(value).toLowerCase();
  }
}
