/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.database;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.isBlank;
import static us.fatehi.utility.Utility.requireNotBlank;

import java.io.Serial;
import us.fatehi.utility.property.BaseProductVersion;
import us.fatehi.utility.property.VersionNumber;

/**
 * JDBC driver information. Created from metadata returned by a JDBC call, and other sources of
 * information.
 */
public final class JdbcDriverInformation extends BaseProductVersion {

  @Serial private static final long serialVersionUID = 7192167974028174124L;

  private final String connectionUrl;
  private final VersionNumber driverVersion;
  private final VersionNumber jdbcVersion;
  private final String driverClassName;
  private final boolean jdbcCompliant;

  public JdbcDriverInformation(
      final String driverName,
      final String driverClassName,
      final String driverVersion,
      final VersionNumber driverVersionNumber,
      final VersionNumber jdbcVersion,
      final boolean jdbcCompliant,
      final String connectionUrl) {
    super(driverName, driverVersion);
    this.driverClassName =
        requireNonNull(driverClassName, "No database driver Java class name provided");
    this.driverVersion = requireNonNull(driverVersionNumber, "No driver version provided");
    this.jdbcVersion = requireNonNull(jdbcVersion, "No JDBC version provided");
    this.jdbcCompliant = jdbcCompliant;
    this.connectionUrl = requireNotBlank(connectionUrl, "No database connection URL provided");
  }

  public String getConnectionUrl() {
    return connectionUrl;
  }

  public String getDriverClassName() {
    return driverClassName;
  }

  /**
   * @deprecated
   */
  @Deprecated
  public int getDriverMajorVersion() {
    return driverVersion.major();
  }

  /**
   * @deprecated
   */
  @Deprecated
  public int getDriverMinorVersion() {
    return driverVersion.minor();
  }

  /**
   * Gets the name of the JDBC driver.
   *
   * @return Name of the JDBC driver
   */
  public String getDriverName() {
    return getProductName();
  }

  /**
   * Gets the version of the JDBC driver.
   *
   * @return Version of the JDBC driver
   */
  public String getDriverVersion() {
    return getProductVersion();
  }

  /**
   * Gets the version of the JDBC driver.
   *
   * @return Version of the JDBC driver
   */
  public VersionNumber getDriverVersionNumber() {
    return driverVersion;
  }

  /**
   * @deprecated
   */
  @Deprecated
  public int getJdbcMajorVersion() {
    return jdbcVersion.major();
  }

  /**
   * @deprecated
   */
  @Deprecated
  public int getJdbcMinorVersion() {
    return jdbcVersion.minor();
  }

  public VersionNumber getJdbcVersionNumber() {
    return jdbcVersion;
  }

  public boolean hasDriverClassName() {
    return !isBlank(driverClassName);
  }

  public boolean isJdbcCompliant() {
    return jdbcCompliant;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    final StringBuilder info = new StringBuilder(1024);
    info.append("-- driver: ")
        .append(getProductName())
        .append(' ')
        .append(getProductVersion())
        .append(System.lineSeparator());
    info.append("-- driver class: ").append(getDriverClassName()).append(System.lineSeparator());
    info.append("-- url: ").append(getConnectionUrl()).append(System.lineSeparator());
    return info.toString();
  }
}
