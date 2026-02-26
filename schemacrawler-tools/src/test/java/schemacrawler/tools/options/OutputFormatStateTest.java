/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class OutputFormatStateTest {

  @Test
  public void testOutputFormatState() {
    final OutputFormatState state =
        new OutputFormatState("html", "HTML output format", "htm", "HTML");

    assertAll(
        () -> assertThat(state.getFormat(), is("html")),
        () -> assertThat(state.getDescription(), is("HTML output format")),
        () -> assertThat(state.getFormats(), contains("html", "htm", "HTML")),
        () -> assertTrue(state.isSupportedFormat("html")),
        () -> assertTrue(state.isSupportedFormat("htm")),
        () -> assertTrue(state.isSupportedFormat("HTML")),
        () -> assertTrue(state.isSupportedFormat("Html")),
        () -> assertFalse(state.isSupportedFormat("text")),
        () -> assertThat(state.toString(), containsString("html")),
        () -> assertThat(state.toString(), containsString("HTML output format")));
  }

  @Test
  public void testOutputFormatStateWithNulls() {
    final OutputFormatState state =
        new OutputFormatState("json", "JSON output format", (String[]) null);

    assertAll(
        () -> assertThat(state.getFormat(), is("json")),
        () -> assertThat(state.getFormats(), contains("json")),
        () -> assertTrue(state.isSupportedFormat("json")));
  }

  @Test
  public void testOutputFormatStateInvalid() {
    assertAll(
        () ->
            assertThrows(IllegalArgumentException.class, () -> new OutputFormatState(null, "desc")),
        () -> assertThrows(IllegalArgumentException.class, () -> new OutputFormatState("", "desc")),
        () ->
            assertThrows(IllegalArgumentException.class, () -> new OutputFormatState("fmt", null)),
        () -> assertThrows(IllegalArgumentException.class, () -> new OutputFormatState("fmt", "")));
  }
}
