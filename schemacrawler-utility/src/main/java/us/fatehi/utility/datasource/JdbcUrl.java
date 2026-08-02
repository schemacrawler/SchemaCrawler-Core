/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.datasource;

import static us.fatehi.utility.Utility.isBlank;

import us.fatehi.utility.HostClassifier;

public record JdbcUrl(
    String databaseServerType, HostClassifier hostClassifier, Integer port, String databaseName) {

  public JdbcUrl {
    databaseServerType = isBlank(databaseServerType) ? "" : databaseServerType.strip();
    hostClassifier = hostClassifier == null ? new HostClassifier(null) : hostClassifier;
    databaseName = isBlank(databaseName) ? null : databaseName.strip();
  }

  public static JdbcUrl empty() {
    return new JdbcUrl("", new HostClassifier(null), null, null);
  }
}
