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
import java.util.Locale;
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

  public ServerIdentity extract(final Connection connection) {
    if (connection == null) {
      return ServerIdentity.unknown();
    }
    try {
      final DatabaseMetaData metaData = connection.getMetaData();
      final String url = metaData == null ? null : metaData.getURL();
      final JdbcUrl jdbcUrl = JdbcUrlParser.parse(url);
      final HostClassifier hostClassifier = jdbcUrl.hostClassifier();
      final String host = maskHost(hostClassifier);

      String instanceName = sanitizeInstance(tryExtractInstance(connection, jdbcUrl));
      if (isBlank(instanceName)) {
        instanceName = sanitizeInstance(jdbcUrl.databaseName());
      }
      if (isBlank(instanceName)) {
        instanceName = sanitizeInstance(firstHostSegment(host));
      }
      if (isBlank(instanceName)) {
        instanceName = "unknown-instance";
      }

      CloudProvider cloudProvider = hostClassifier.getCloudProvider();
      if (cloudProvider == null) {
        cloudProvider = CloudProvider.UNKNOWN;
      }

      String region = sanitizeRegion(hostClassifier.getCloudRegion());
      if (isBlank(region)) {
        region = "unknown";
      }

      return new ServerIdentity(instanceName, cloudProvider, region);
    } catch (final Exception e) {
      LOGGER.log(Level.FINE, "Could not extract server identity", e);
      return ServerIdentity.unknown();
    }
  }

  protected String tryExtractInstance(final Connection connection, final JdbcUrl jdbcUrl) {
    return null;
  }

  protected String sanitizeInstance(final String rawValue) {
    return new HostClassifier(rawValue).getSanitizedHostName();
  }

  private String firstHostSegment(final String host) {
    if (isBlank(host)) {
      return null;
    }
    final String value = host.trim();
    final int dot = value.indexOf('.');
    if (dot > 0) {
      return value.substring(0, dot);
    }
    return value;
  }

  private String maskHost(final HostClassifier hostClassifier) {
    if (hostClassifier == null) {
      return null;
    }
    if (hostClassifier.isLocalhost()) {
      return "localhost";
    }
    if (hostClassifier.isNotHostName()) {
      return null;
    }
    return hostClassifier.asHostName();
  }

  private String sanitizeRegion(final String rawValue) {
    if (isBlank(rawValue)) {
      return null;
    }
    return rawValue.trim().toLowerCase(Locale.ROOT);
  }
}
