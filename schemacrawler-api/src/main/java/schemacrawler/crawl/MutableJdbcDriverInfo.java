/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static java.util.Comparator.naturalOrder;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import schemacrawler.schema.JdbcDriverInfo;
import schemacrawler.schema.JdbcDriverProperty;
import schemacrawler.schemacrawler.ModelImplementation;
import us.fatehi.utility.database.JdbcDriverInformation;
import us.fatehi.utility.property.BaseProductVersion;
import us.fatehi.utility.property.VersionNumber;

/**
 * JDBC driver information. Created from metadata returned by a JDBC call, and other sources of
 * information.
 */
@ModelImplementation
final class MutableJdbcDriverInfo extends BaseProductVersion implements JdbcDriverInfo {

  @Serial private static final long serialVersionUID = 8030156654422512161L;

  private final JdbcDriverInformation jdbcDriverInformation;
  // Mutable properties collection
  private final Set<ImmutableJdbcDriverProperty> jdbcDriverProperties;

  public MutableJdbcDriverInfo(final JdbcDriverInformation jdbcDriverInformation) {
    super(jdbcDriverInformation);
    this.jdbcDriverInformation = jdbcDriverInformation;
    jdbcDriverProperties = new HashSet<>();
  }

  /** {@inheritDoc} */
  @Override
  public String getConnectionUrl() {
    return jdbcDriverInformation.getConnectionUrl();
  }

  /** {@inheritDoc} */
  @Override
  public String getDriverClassName() {
    return jdbcDriverInformation.getDriverClassName();
  }

  /**
   * @deprecated
   */
  @Deprecated
  @Override
  public int getDriverMajorVersion() {
    return jdbcDriverInformation.getDriverVersionNumber().major();
  }

  /**
   * @deprecated
   */
  @Deprecated
  @Override
  public int getDriverMinorVersion() {
    return jdbcDriverInformation.getDriverVersionNumber().minor();
  }

  /** {@inheritDoc} */
  @Override
  public Collection<JdbcDriverProperty> getDriverProperties() {
    final List<JdbcDriverProperty> properties = new ArrayList<>(jdbcDriverProperties);
    properties.sort(naturalOrder());
    return properties;
  }

  @Override
  public VersionNumber getDriverVersionNumber() {
    return jdbcDriverInformation.getDriverVersionNumber();
  }

  /**
   * @deprecated
   */
  @Deprecated
  @Override
  public int getJdbcMajorVersion() {
    return jdbcDriverInformation.getJdbcVersionNumber().major();
  }

  /**
   * @deprecated
   */
  @Deprecated
  @Override
  public int getJdbcMinorVersion() {
    return jdbcDriverInformation.getJdbcVersionNumber().minor();
  }

  @Override
  public VersionNumber getJdbcVersionNumber() {
    return jdbcDriverInformation.getJdbcVersionNumber();
  }

  @Override
  public boolean hasDriverClassName() {
    return jdbcDriverInformation.hasDriverClassName();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isJdbcCompliant() {
    return jdbcDriverInformation.isJdbcCompliant();
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return jdbcDriverInformation.toString();
  }

  /**
   * Adds a JDBC driver property.
   *
   * @param jdbcDriverProperty JDBC driver property
   */
  void addJdbcDriverProperty(final ImmutableJdbcDriverProperty jdbcDriverProperty) {
    jdbcDriverProperties.add(jdbcDriverProperty);
  }
}
