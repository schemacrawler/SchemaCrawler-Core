/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.sql.DriverManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class LoggingConfigTest {

  @AfterEach
  public void restoreLogging() {
    // Restore logging to a usable state after each test
    Logger.getLogger("").setLevel(Level.OFF);
  }

  @Test
  public void loggingConfigDefaultConstructorSetsOff() {
    new LoggingConfig();
    assertThat(Logger.getLogger("").getLevel(), is(Level.OFF));
  }

  @Test
  public void loggingConfigWithNullLevelSetsOff() {
    new LoggingConfig(null);
    assertThat(Logger.getLogger("").getLevel(), is(Level.OFF));
  }

  @Test
  public void loggingConfigWithLevelInfo() {
    new LoggingConfig(Level.INFO);
    assertThat(Logger.getLogger("").getLevel(), is(Level.INFO));
    assertThat(System.getProperty("org.slf4j.simpleLogger.defaultLogLevel"), is("info"));
  }

  @Test
  public void loggingConfigWithLevelWarning() {
    new LoggingConfig(Level.WARNING);
    assertThat(Logger.getLogger("").getLevel(), is(Level.WARNING));
    assertThat(System.getProperty("org.slf4j.simpleLogger.defaultLogLevel"), is("warn"));
  }

  @Test
  public void loggingConfigWithLevelConfig() {
    new LoggingConfig(Level.CONFIG);
    assertThat(Logger.getLogger("").getLevel(), is(Level.CONFIG));
    assertThat(System.getProperty("org.slf4j.simpleLogger.defaultLogLevel"), is("debug"));
  }

  @Test
  public void loggingConfigWithLevelSevere() {
    new LoggingConfig(Level.SEVERE);
    assertThat(Logger.getLogger("").getLevel(), is(Level.SEVERE));
    assertThat(System.getProperty("org.slf4j.simpleLogger.defaultLogLevel"), is("error"));
  }

  @Test
  public void loggingConfigWithLevelFine() {
    new LoggingConfig(Level.FINE);
    assertThat(Logger.getLogger("").getLevel(), is(Level.FINE));
    assertThat(System.getProperty("org.slf4j.simpleLogger.defaultLogLevel"), is("trace"));
  }

  @Test
  public void loggingConfigSetsSystemProperties() {
    new LoggingConfig(Level.INFO);
    assertThat(System.getProperty("polyglot.engine.WarnInterpreterOnly"), is("false"));
    assertThat(System.getProperty("polyglotimpl.AttachLibraryFailureAction"), is("ignore"));
    assertThat(System.getProperty("log4j2.formatMsgNoLookups"), is("true"));
  }

  @Test
  public void loggingConfigSetsDriverManagerWriter() {
    new LoggingConfig(Level.INFO);
    assertThat(DriverManager.getLogWriter(), is(not(nullValue())));
  }

  @Test
  public void loggingConfigPicocliTraceMappedFromSevere() {
    System.setProperty("picocli.trace", "WARN");
    try {
      new LoggingConfig(Level.SEVERE);
      assertThat(System.getProperty("picocli.trace"), is("WARN"));
    } finally {
      System.clearProperty("picocli.trace");
    }
  }

  @Test
  public void loggingConfigPicocliTraceMappedFromInfo() {
    System.setProperty("picocli.trace", "something");
    try {
      new LoggingConfig(Level.INFO);
      assertThat(System.getProperty("picocli.trace"), is("INFO"));
    } finally {
      System.clearProperty("picocli.trace");
    }
  }

  @Test
  public void loggingConfigPicocliTraceMappedFromFine() {
    System.setProperty("picocli.trace", "something");
    try {
      new LoggingConfig(Level.FINE);
      assertThat(System.getProperty("picocli.trace"), is("DEBUG"));
    } finally {
      System.clearProperty("picocli.trace");
    }
  }

  @Test
  public void driverManagerLogWriterNoExceptionWhenNotLoggable() throws IOException {
    // shouldLog will be false since logger is typically not at CONFIG level
    final DriverManagerLogWriter writer = new DriverManagerLogWriter();
    writer.write(new char[] {'a', 'b', 'c', '\n'}, 0, 4);
    writer.flush();
    writer.close();
  }

  @Test
  public void driverManagerLogWriterLogsWhenLoggable() throws IOException {
    final Logger driverManagerLogger = Logger.getLogger(DriverManager.class.getName());
    final Level originalLevel = driverManagerLogger.getLevel();
    try {
      driverManagerLogger.setLevel(Level.CONFIG);
      // Set root logger to CONFIG so isLoggable returns true
      Logger.getLogger("").setLevel(Level.CONFIG);

      final DriverManagerLogWriter writer = new DriverManagerLogWriter();
      writer.write(new char[] {'h', 'e', 'l', 'l', 'o', '\n'}, 0, 6);
      writer.write(new char[] {'w', 'o', 'r', 'l', 'd'}, 0, 5);
      writer.flush();
      writer.close();
    } finally {
      driverManagerLogger.setLevel(originalLevel);
    }
  }
}
