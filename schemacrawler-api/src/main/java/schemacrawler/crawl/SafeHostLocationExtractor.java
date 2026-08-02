/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static us.fatehi.utility.Utility.isBlank;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schema.HostLocation;
import schemacrawler.schemacrawler.HostLocationExtractor;
import us.fatehi.utility.CloudProvider;
import us.fatehi.utility.HostClassifier;
import us.fatehi.utility.HostType;
import us.fatehi.utility.datasource.JdbcUrl;
import us.fatehi.utility.datasource.JdbcUrlParser;

public class SafeHostLocationExtractor implements HostLocationExtractor {

  private static final Logger LOGGER = Logger.getLogger(SafeHostLocationExtractor.class.getName());

  @Override
  public HostLocation extract(final Connection connection) {
    if (connection == null) {
      return HostLocation.unknown();
    }
    try {
      final DatabaseMetaData metaData = connection.getMetaData();
      final String url = metaData == null ? null : metaData.getURL();
      final JdbcUrl jdbcUrl = JdbcUrlParser.parse(url);
      final HostClassifier hostClassifier = jdbcUrl.hostClassifier();

      final HostClassifier extractedHostClassifier =
          new HostClassifier(obtainInstanceName(connection, jdbcUrl));
      final HostClassifier effectiveHostClassifier =
          isBlank(extractedHostClassifier.getSanitizedHostName())
              ? hostClassifier
              : extractedHostClassifier;
      final HostType hostType = effectiveHostClassifier.getHostType();
      final CloudProvider cloudProvider = hostClassifier.getCloudProvider();
      final String region = hostClassifier.getCloudRegion();

      return new HostLocation(hostType, cloudProvider, region);
    } catch (final Exception e) {
      LOGGER.log(Level.FINE, "Could not extract host location", e);
      return HostLocation.unknown();
    }
  }

  protected String obtainInstanceName(final Connection connection, final JdbcUrl jdbcUrl) {
    return null;
  }
}
