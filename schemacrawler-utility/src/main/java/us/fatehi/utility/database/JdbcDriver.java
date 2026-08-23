/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.database;

import static us.fatehi.utility.Utility.isBlank;

import us.fatehi.utility.property.VersionNumber;

/**
 * Immutable JDBC driver information.
 *
 * <p>Does not expose the internal {@link java.sql.Driver} instance.
 */
public record JdbcDriver(
    String driverClassName, VersionNumber driverVersionNumber, boolean jdbcCompliant) {

  public JdbcDriver {
    driverClassName = isBlank(driverClassName) ? "" : driverClassName;
    driverVersionNumber =
        driverVersionNumber == null ? new VersionNumber(0, 0) : driverVersionNumber;
  }

  public JdbcDriver() {
    this(null, null, false);
  }
}
