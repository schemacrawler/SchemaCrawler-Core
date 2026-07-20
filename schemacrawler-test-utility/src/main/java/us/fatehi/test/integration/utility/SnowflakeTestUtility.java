/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.test.integration.utility;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.utility.DockerImageName;

public final class SnowflakeTestUtility {

  public static JdbcDatabaseContainer<?> newSnowflakeContainer() {
    return newSnowflakeContainer(null);
  }

  public static JdbcDatabaseContainer<?> newSnowflakeContainer(final String authToken) {
    return new SnowflakeContainer(DockerImageName.parse("localstack/snowflake").withTag("2026.06"))
        .withAuthToken(authToken)
        .withDatabaseName("books");
  }

  private SnowflakeTestUtility() {
    // Prevent instantiation
  }
}
