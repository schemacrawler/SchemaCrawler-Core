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
import schemacrawler.schemacrawler.HostLocationExtractor;
import us.fatehi.utility.HostClassifier;
import us.fatehi.utility.HostLocation;
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
      HostClassifier hostClassifier = jdbcUrl.hostClassifier();

      final String instanceName = obtainInstanceName(connection, jdbcUrl);
      if (!isBlank(instanceName)) {
        hostClassifier = new HostClassifier(instanceName);
      }

      return hostClassifier.getHostLocation();
    } catch (final Exception e) {
      LOGGER.log(Level.FINE, "Could not extract host location", e);
      return HostLocation.unknown();
    }
  }

  protected String obtainInstanceName(final Connection connection, final JdbcUrl jdbcUrl) {
    return null;
  }
}
