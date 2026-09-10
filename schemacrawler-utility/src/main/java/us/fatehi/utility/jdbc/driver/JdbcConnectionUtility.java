/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.jdbc.driver;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public final class JdbcConnectionUtility {

  static final Logger LOGGER = Logger.getLogger(JdbcConnectionUtility.class.getName());

  /**
   * Get database connection URL.
   *
   * <p>NOTE: Some connections such as an offline connection may throw an exception.
   *
   * @param connection Database connection.
   * @return Database connection URL
   */
  public static String getConnectionUrl(final Connection connection) {
    if (connection == null) {
      return "";
    }
    final DatabaseMetaData dbMetaData;
    try {
      // JDBC metadata URL is the canonical source for connector resolution.
      dbMetaData = connection.getMetaData();
    } catch (final SQLException e) {
      // Callers treat blank URL as "cannot infer connector from URL".
      logException(e);
      return "";
    }

    final String connectionUrl = getConnectionUrl(dbMetaData);
    return connectionUrl;
  }

  /**
   * Get database connection URL.
   *
   * <p>NOTE: Some databases such as Hive may throw an exception. See issue #910.
   *
   * @param dbMetaData Database metadata.
   * @return Database connection URL
   */
  public static String getConnectionUrl(final DatabaseMetaData dbMetaData) {
    if (dbMetaData == null) {
      return "";
    }
    try {
      return dbMetaData.getURL();
    } catch (final SQLException e) {
      logException(e);
      return "";
    }
  }

  private static void logException(final SQLException e) {
    LOGGER.log(Level.WARNING, "Could not obtain the database connection URL", e);
  }

  private JdbcConnectionUtility() {
    // Prevent instantiation
  }
}
