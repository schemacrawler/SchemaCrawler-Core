/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.test.string;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import us.fatehi.utility.string.StringFormatFunction;

public class StringFormatFunctionTest {

  @Test
  public void happyPath() {
    assertThat(new StringFormatFunction("%03d").apply(1), is("001"));
    assertThat(new StringFormatFunction("hello").apply(null), is("hello"));
    assertThat(new StringFormatFunction("Value: %s").apply(42), is("Value: 42"));
  }

  @Test
  public void nullArg() {
    // null arg with a substitution format returns the format string unchanged (isBlank check
    // passes)
    assertThat(new StringFormatFunction("%s").apply(null), is("%s"));
  }

  @Test
  public void nullFormat() {
    // null format returns null regardless of arg
    assertThat(new StringFormatFunction(null).apply("x"), is(nullValue()));
  }

  @Test
  public void badFormat() {
    // format type mismatch logs quietly and returns empty string
    assertThat(new StringFormatFunction("%d").apply("hello"), is(""));
  }

  @Test
  public void blankFormat() {
    assertThat(new StringFormatFunction("").apply(42), is(""));
    assertThat(new StringFormatFunction("   ").apply(42), is("   "));
  }

  @Test
  public void multipleArgs() {
    assertThat(
        new StringFormatFunction("%s and %s").apply(List.of("hello", "world")),
        is("hello and world"));
    assertThat(new StringFormatFunction("%03d + %03d").apply(List.of(1, 2)), is("001 + 002"));
  }

  @Test
  public void nullElementInList() {
    // null element inside a list is passed to the formatter — rendered as "null"
    assertThat(new StringFormatFunction("%s").apply(Arrays.asList((Object) null)), is("null"));
  }

  @Test
  public void emptyList() {
    // empty list returns format unchanged, just like no-arg StringFormat
    assertThat(new StringFormatFunction("%s").apply(List.of()), is("%s"));
  }
}
