/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.databaseconnector;

import us.fatehi.utility.datasource.JdbcUrlParser;

public record DatabaseUrlConnectionOptions(String connectionUrl)
    implements DatabaseConnectionOptions {

  @Override
  public String databaseSystemIdentifier() {
    return JdbcUrlParser.parse(connectionUrl).databaseSystemIdentifier();
  }
}
