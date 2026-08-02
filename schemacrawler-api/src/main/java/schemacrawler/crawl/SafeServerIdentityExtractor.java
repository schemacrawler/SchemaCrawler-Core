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
import schemacrawler.schema.ServerIdentity;
import schemacrawler.schemacrawler.ServerIdentityExtractor;
import us.fatehi.utility.CloudProvider;
import us.fatehi.utility.HostClassifier;
import us.fatehi.utility.datasource.JdbcUrl;
import us.fatehi.utility.datasource.JdbcUrlParser;

public class SafeServerIdentityExtractor implements ServerIdentityExtractor {

  private static final Logger LOGGER =
      Logger.getLogger(SafeServerIdentityExtractor.class.getName());

  @Override
  public ServerIdentity extract(final Connection connection) {
    if (connection == null) {
      return ServerIdentity.unknown();
    }
    try {
      final DatabaseMetaData metaData = connection.getMetaData();
      final String url = metaData == null ? null : metaData.getURL();
      final JdbcUrl jdbcUrl = JdbcUrlParser.parse(url);
      final HostClassifier hostClassifier = jdbcUrl.hostClassifier();

      final String extractedInstance = tryExtractInstance(connection, jdbcUrl);
      String instanceName = new HostClassifier(extractedInstance).getSanitizedHostName();
      if (isBlank(instanceName)) {
        instanceName = hostClassifier.getSanitizedHostName();
      }

      final CloudProvider cloudProvider = hostClassifier.getCloudProvider();
      final String region = hostClassifier.getCloudRegion();

      return new ServerIdentity(instanceName, cloudProvider, region);
    } catch (final Exception e) {
      LOGGER.log(Level.FINE, "Could not extract server identity", e);
      return ServerIdentity.unknown();
    }
  }

  protected String tryExtractInstance(final Connection connection, final JdbcUrl jdbcUrl) {
    return null;
  }
}
