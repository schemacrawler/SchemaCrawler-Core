/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.databaseconnector;

import us.fatehi.utility.jdbc.serverfingerprint.DatabaseServerFingerprint;
import us.fatehi.utility.jdbc.serverfingerprint.DatabaseServerFingerprintBuilder;

public record DatabaseUrlConnectionOptions(String connectionUrl)
    implements DatabaseConnectionOptions {

  @Override
  public String databaseSystemIdentifier() {
    final DatabaseServerFingerprint serverFingerprint =
        DatabaseServerFingerprintBuilder.builder(connectionUrl).build();
    final String databaseSystemIdentifier = serverFingerprint.databaseSystemIdentifier();
    return databaseSystemIdentifier;
  }
}
