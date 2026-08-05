/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.datasource;

import static us.fatehi.utility.Utility.isBlank;

public record JdbcUrl(String databaseServerType, Integer port, String databaseName) {

  public JdbcUrl {
    databaseServerType = isBlank(databaseServerType) ? "" : databaseServerType.strip();
    databaseName = isBlank(databaseName) ? null : databaseName.strip();
  }

  public static JdbcUrl empty() {
    return new JdbcUrl("", null, null);
  }
}
