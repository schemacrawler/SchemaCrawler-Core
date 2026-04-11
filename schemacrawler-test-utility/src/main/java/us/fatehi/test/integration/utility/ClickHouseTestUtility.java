/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.test.integration.utility;

import org.testcontainers.clickhouse.ClickHouseContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.utility.DockerImageName;

public final class ClickHouseTestUtility {

  public static JdbcDatabaseContainer<?> newClickhouseContainer() {
    return newClickhouseContainer("26.2");
  }

  public static JdbcDatabaseContainer<?> newClickhouseContainer(final String version) {
    final DockerImageName imageName = DockerImageName.parse("clickhouse/clickhouse-server");
    return new ClickHouseContainer(imageName.withTag(version));
  }

  private ClickHouseTestUtility() {
    // Prevent instantiation
  }
}
