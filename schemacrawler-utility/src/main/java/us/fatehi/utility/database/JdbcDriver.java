/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.database;

import static java.util.Objects.requireNonNull;

import us.fatehi.utility.property.PropertyName;
import us.fatehi.utility.property.VersionNumber;

/**
 * Immutable JDBC driver information.
 *
 * <p>Does not expose the internal {@link java.sql.Driver} instance.
 */
public record JdbcDriver(
    String driverClassName,
    VersionNumber driverVersionNumber,
    VersionNumber jdbcVersionNumber,
    boolean jdbcCompliant) {

  public JdbcDriver {
    driverClassName = requireNonNull(driverClassName, "Driver class name required");
    driverVersionNumber = requireNonNull(driverVersionNumber, "Driver version number required");
    jdbcVersionNumber = requireNonNull(jdbcVersionNumber, "JDBC version number required");
  }

  public PropertyName toPropertyName() {
    return new PropertyName(driverClassName, driverVersionNumber.toString());
  }

  @Override
  public String toString() {
    return toPropertyName().toString();
  }
}
