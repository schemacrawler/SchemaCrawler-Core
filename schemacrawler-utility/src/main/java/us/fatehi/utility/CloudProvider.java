/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility;

import static us.fatehi.utility.Utility.isBlank;

import java.util.Locale;

public enum CloudProvider {
  AWS,
  AZURE,
  GCP,
  ORACLE,
  UNKNOWN;

  public static CloudProvider fromHost(final String host) {
    if (isBlank(host)) {
      return UNKNOWN;
    }
    final String h = host.trim().toLowerCase(Locale.ROOT);
    if (h.contains("rds.amazonaws.com") || h.contains("aurora")) {
      return AWS;
    }
    if (h.contains("database.windows.net")) {
      return AZURE;
    }
    if (h.contains("cloudsql")) {
      return GCP;
    }
    if (h.contains("oraclecloud.com")) {
      return ORACLE;
    }
    return UNKNOWN;
  }
}
