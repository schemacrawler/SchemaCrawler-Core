/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import schemacrawler.tools.offline.jdbc.OfflineConnection;
import schemacrawler.tools.offline.jdbc.OfflineDriver;

public class OfflineDriverConnectTest {

  @TempDir private Path tempDir;

  private OfflineDriver offlineDriver;
  private Path offlineDatabasePath;

  @BeforeEach
  public void _createOfflineDatabaseFile() throws Exception {
    offlineDriver = new OfflineDriver();
    offlineDatabasePath = tempDir.resolve("offline-catalog-snapshot.ser");
    // An empty file is not considered readable, so write some content
    Files.write(offlineDatabasePath, "offline catalog snapshot".getBytes(UTF_8));
  }

  @Test
  public void connectsThroughDriverManager() throws SQLException {
    try (final Connection connection =
        DriverManager.getConnection("jdbc:offline:" + offlineDatabasePath)) {
      assertThat(connection, is(notNullValue()));
      assertThat(
          ((OfflineConnection) connection).getOfflineDatabasePath(),
          is(offlineDatabasePath.toAbsolutePath()));
    }
  }

  @Test
  public void connectsWithDoubleSlashPrefix() throws SQLException {
    final OfflineConnection connection =
        (OfflineConnection)
            offlineDriver.connect("jdbc:offline://" + offlineDatabasePath, new Properties());

    assertThat(connection, is(notNullValue()));
    assertThat(connection.getOfflineDatabasePath(), is(offlineDatabasePath.toAbsolutePath()));
  }

  @Test
  public void connectsWithSimplePrefix() throws SQLException {
    final OfflineConnection connection =
        (OfflineConnection)
            offlineDriver.connect("jdbc:offline:" + offlineDatabasePath, new Properties());

    assertThat(connection, is(notNullValue()));
    assertThat(connection.getOfflineDatabasePath(), is(offlineDatabasePath.toAbsolutePath()));
  }

  @Test
  public void returnsNullForUnsupportedUrl() throws SQLException {
    assertThat(offlineDriver.connect(null, new Properties()), is(nullValue()));
    assertThat(offlineDriver.connect("", new Properties()), is(nullValue()));
    assertThat(offlineDriver.connect("jdbc:test-db:something", new Properties()), is(nullValue()));
  }

  @Test
  public void throwsForBlankPath() {
    // Windows rejects a whitespace-only path with an InvalidPathException, while POSIX
    // file systems accept it as a file name and fail the readability check instead
    assertThrows(
        RuntimeException.class, () -> offlineDriver.connect("jdbc:offline:   ", new Properties()));
  }

  @Test
  public void throwsForDirectoryPath() {
    assertThrows(
        UncheckedIOException.class,
        () -> offlineDriver.connect("jdbc:offline:" + tempDir, new Properties()));
  }

  @Test
  public void throwsForEmptyFile() throws Exception {
    final Path emptyFilePath = tempDir.resolve("empty-catalog-snapshot.ser");
    Files.createFile(emptyFilePath);

    assertThrows(
        UncheckedIOException.class,
        () -> offlineDriver.connect("jdbc:offline:" + emptyFilePath, new Properties()));
  }

  @Test
  public void throwsForEmptyPath() {
    assertThrows(
        UncheckedIOException.class, () -> offlineDriver.connect("jdbc:offline:", new Properties()));
    assertThrows(
        UncheckedIOException.class,
        () -> offlineDriver.connect("jdbc:offline://", new Properties()));
  }

  @Test
  public void throwsForNonExistentPath() {
    final Path missingPath = tempDir.resolve("does-not-exist.ser");

    assertThrows(
        UncheckedIOException.class,
        () -> offlineDriver.connect("jdbc:offline:" + missingPath, new Properties()));
    assertThrows(
        UncheckedIOException.class,
        () -> offlineDriver.connect("jdbc:offline://" + missingPath, new Properties()));
  }
}
