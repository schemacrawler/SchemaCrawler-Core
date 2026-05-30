/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DriverManagerLogWriterTest {

  /** Captures log records emitted to the DriverManager logger at CONFIG level. */
  private static class CapturingHandler extends Handler {

    private final List<String> messages = new ArrayList<>();

    @Override
    public void close() {}

    @Override
    public void flush() {}

    @Override
    public void publish(final LogRecord record) {
      if (record.getLevel().equals(Level.CONFIG)) {
        messages.add(record.getMessage());
      }
    }

    List<String> messages() {
      return List.copyOf(messages);
    }
  }

  private Logger driverManagerLogger;
  private CapturingHandler capturingHandler;
  private Level originalLevel;

  @Test
  public void carriageReturnOnlyFlushesLine() throws IOException {
    final DriverManagerLogWriter writer = new DriverManagerLogWriter();
    writer.write("hello\r".toCharArray(), 0, 6);
    assertThat(capturingHandler.messages(), contains("hello"));
  }

  @Test
  public void closeFlushesRemainingContent() throws IOException {
    final DriverManagerLogWriter writer = new DriverManagerLogWriter();
    writer.write("trailing".toCharArray(), 0, 8);
    assertThat(capturingHandler.messages(), is(empty()));
    writer.close();
    assertThat(capturingHandler.messages(), contains("trailing"));
  }

  // --- Unix-style line endings (\n) ---

  @Test
  public void contentAfterLastNewlineBufferedUntilFlush() throws IOException {
    final DriverManagerLogWriter writer = new DriverManagerLogWriter();
    writer.write("first\nsecond".toCharArray(), 0, 12);
    assertThat(capturingHandler.messages(), contains("first"));
    writer.flush();
    assertThat(capturingHandler.messages(), contains("first", "second"));
  }

  @Test
  public void emptyLinesCarriageReturnNotLogged() throws IOException {
    final DriverManagerLogWriter writer = new DriverManagerLogWriter();
    writer.write("\r\r\r".toCharArray(), 0, 3);
    assertThat(capturingHandler.messages(), is(empty()));
  }

  // --- Windows-style line endings (\r\n) ---

  @Test
  public void emptyLinesNotLogged() throws IOException {
    final DriverManagerLogWriter writer = new DriverManagerLogWriter();
    writer.write("\n\n\n".toCharArray(), 0, 3);
    assertThat(capturingHandler.messages(), is(empty()));
  }

  @Test
  public void emptyLinesWindowsNotLogged() throws IOException {
    final DriverManagerLogWriter writer = new DriverManagerLogWriter();
    writer.write("\r\n\r\n".toCharArray(), 0, 4);
    assertThat(capturingHandler.messages(), is(empty()));
  }

  // --- Old Mac-style line endings (\r only) ---

  @Test
  public void flushOnEmptyBufferLogsNothing() throws IOException {
    final DriverManagerLogWriter writer = new DriverManagerLogWriter();
    writer.flush();
    assertThat(capturingHandler.messages(), is(empty()));
  }

  @Test
  public void mixedLineEndingsInOneWrite() throws IOException {
    final DriverManagerLogWriter writer = new DriverManagerLogWriter();
    // Unix, Windows, old Mac, then trailing content
    writer.write("a\nb\r\nc\r".toCharArray(), 0, 7);
    assertThat(capturingHandler.messages(), contains("a", "b", "c"));
  }

  // --- Split \r\n across two write() calls ---

  @Test
  public void multipleCarriageReturns() throws IOException {
    final DriverManagerLogWriter writer = new DriverManagerLogWriter();
    writer.write("line1\rline2\r".toCharArray(), 0, 12);
    assertThat(capturingHandler.messages(), contains("line1", "line2"));
  }

  // --- Empty lines must not be logged ---

  @Test
  public void multipleUnixNewlines() throws IOException {
    final DriverManagerLogWriter writer = new DriverManagerLogWriter();
    writer.write("line1\nline2\n".toCharArray(), 0, 12);
    assertThat(capturingHandler.messages(), contains("line1", "line2"));
  }

  @Test
  public void multipleWindowsNewlines() throws IOException {
    final DriverManagerLogWriter writer = new DriverManagerLogWriter();
    writer.write("line1\r\nline2\r\n".toCharArray(), 0, 14);
    assertThat(capturingHandler.messages(), contains("line1", "line2"));
  }

  @Test
  public void noNewlineNotLoggedUntilFlush() throws IOException {
    final DriverManagerLogWriter writer = new DriverManagerLogWriter();
    writer.write("pending".toCharArray(), 0, 7);
    assertThat(capturingHandler.messages(), is(empty()));
    writer.flush();
    assertThat(capturingHandler.messages(), contains("pending"));
  }

  // --- No newline: content only logged on explicit flush / close ---

  @Test
  public void notLoggableWhenLevelOff() throws IOException {
    // Reset to OFF so shouldLog is false at construction time
    driverManagerLogger.setLevel(Level.OFF);
    Logger.getLogger("").setLevel(Level.OFF);

    final DriverManagerLogWriter writer = new DriverManagerLogWriter();
    writer.write("hello\n".toCharArray(), 0, 6);
    writer.write("hello\r\n".toCharArray(), 0, 7);
    writer.write("hello\r".toCharArray(), 0, 6);
    writer.flush();
    writer.close();

    assertThat(capturingHandler.messages(), is(empty()));
  }

  @BeforeEach
  public void setUp() {
    driverManagerLogger = Logger.getLogger(DriverManager.class.getName());
    originalLevel = driverManagerLogger.getLevel();

    // Enable CONFIG level so DriverManagerLogWriter.shouldLog is true
    driverManagerLogger.setLevel(Level.CONFIG);
    Logger.getLogger("").setLevel(Level.CONFIG);

    capturingHandler = new CapturingHandler();
    capturingHandler.setLevel(Level.ALL);
    driverManagerLogger.addHandler(capturingHandler);
  }

  @AfterEach
  public void tearDown() {
    driverManagerLogger.removeHandler(capturingHandler);
    driverManagerLogger.setLevel(originalLevel);
    Logger.getLogger("").setLevel(Level.OFF);
  }

  // --- offset and length in write(char[], off, len) ---

  @Test
  public void unixNewlineFlushesLine() throws IOException {
    final DriverManagerLogWriter writer = new DriverManagerLogWriter();
    writer.write(new char[] {'h', 'e', 'l', 'l', 'o', '\n'}, 0, 6);
    assertThat(capturingHandler.messages(), contains("hello"));
  }

  // --- When logging is disabled, write() is a no-op ---

  @Test
  public void windowsNewlineFlushesOnce() throws IOException {
    final DriverManagerLogWriter writer = new DriverManagerLogWriter();
    writer.write("hello\r\n".toCharArray(), 0, 7);
    // \r flushes "hello"; \n sees empty buffer → no second log entry
    assertThat(capturingHandler.messages(), contains("hello"));
  }

  // --- Mixed content: text before and after various newlines ---

  @Test
  public void windowsNewlineSplitAcrossTwoCalls() throws IOException {
    final DriverManagerLogWriter writer = new DriverManagerLogWriter();
    // First call ends with \r (flushes "hello")
    writer.write("hello\r".toCharArray(), 0, 6);
    assertThat(capturingHandler.messages(), contains("hello"));
    // Second call starts with \n (flush on empty buffer → no-op), then buffers
    // "world"
    writer.write("\nworld".toCharArray(), 0, 6);
    assertThat(capturingHandler.messages(), contains("hello")); // still just "hello"
    writer.flush();
    assertThat(capturingHandler.messages(), contains("hello", "world"));
  }

  @Test
  public void writeUsesOffsetAndLength() throws IOException {
    final DriverManagerLogWriter writer = new DriverManagerLogWriter();
    // Only write the middle portion "ell\n" from "hello\nworld"
    final char[] buf = "hello\nworld".toCharArray();
    writer.write(buf, 1, 5); // "ello\n"
    assertThat(capturingHandler.messages(), contains("ello"));
  }
}
