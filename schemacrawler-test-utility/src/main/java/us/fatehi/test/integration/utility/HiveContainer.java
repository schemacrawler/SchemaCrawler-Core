/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.test.integration.utility;

import static java.time.temporal.ChronoUnit.MINUTES;

import java.time.Duration;
import java.util.Set;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

public final class HiveContainer extends JdbcDatabaseContainer<HiveContainer> {

  private static final int HIVE_PORT = 10000;

  private String databaseName = "default";

  HiveContainer(final DockerImageName dockerImageName) {
    super(dockerImageName);
  }

  @Override
  public String getDatabaseName() {
    return databaseName;
  }

  @Override
  public String getDriverClassName() {
    return "org.apache.hive.jdbc.HiveDriver";
  }

  @Override
  public String getJdbcUrl() {
    return "jdbc:hive2://%s:%d/%s".formatted(getHost(), getMappedPort(HIVE_PORT), databaseName);
  }

  @Override
  public Set<Integer> getLivenessCheckPortNumbers() {
    return Set.of(getMappedPort(HIVE_PORT));
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return "hive";
  }

  @Override
  public HiveContainer withDatabaseName(final String databaseName) {
    this.databaseName = databaseName;
    return self();
  }

  @Override
  public HiveContainer withPassword(final String password) {
    return self();
  }

  @Override
  public HiveContainer withUrlParam(final String paramName, final String paramValue) {
    return self();
  }

  @Override
  public HiveContainer withUsername(final String username) {
    return self();
  }

  @Override
  protected void configure() {
    super.configure();
    addExposedPort(HIVE_PORT);
    withEnv("SERVICE_NAME", "hiveserver2");
    waitingFor(
        new LogMessageWaitStrategy()
            .withRegEx(".*Started HiveServer2.*|.*HiveServer2.*started.*")
            .withStartupTimeout(Duration.of(5, MINUTES)));
  }

  @Override
  protected String getTestQueryString() {
    return "select 1";
  }

  @Override
  protected void waitUntilContainerStarted() {
    getWaitStrategy().waitUntilReady(this);
  }
}
