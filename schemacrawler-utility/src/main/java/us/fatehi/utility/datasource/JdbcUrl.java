/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.datasource;

import static us.fatehi.utility.Utility.isBlank;
import static us.fatehi.utility.Utility.trimToEmpty;

public record JdbcUrl(
    String databaseServerType,
    String hostHash,
    Integer port,
    String databaseName,
    HostClassification hostClassification) {

  public JdbcUrl {
    databaseServerType = normalizeToken(databaseServerType);
    hostHash = normalizeToken(hostHash);
    databaseName = normalizeToken(databaseName);
    if (hostClassification == null) {
      hostClassification = HostClassification.UNKNOWN;
    }
  }

  public JdbcUrl() {
    this(null, null, null, null, null);
  }

  public boolean hasDatabaseName() {
    return !isBlank(databaseName);
  }

  public boolean hasDatabaseServerType() {
    return !isBlank(databaseServerType);
  }

  public boolean hasHost() {
    return !isBlank(hostHash);
  }

  public boolean hasPort() {
    return port != null;
  }

  public boolean hasPublicHost() {
    return hasHost() && hostClassification == HostClassification.PUBLIC;
  }

  private static String normalizeToken(final String value) {
    return trimToEmpty(value).toLowerCase();
  }
}
