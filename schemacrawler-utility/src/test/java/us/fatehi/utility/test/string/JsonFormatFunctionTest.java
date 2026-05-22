/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.test.string;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.util.List;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import us.fatehi.utility.string.JsonFormatFunction;

public class JsonFormatFunctionTest {

  static class SomeClass {
    private final String string;
    private final int integer;

    public SomeClass(final String string, final int integer) {
      this.string = string;
      this.integer = integer;
    }

    public int getInteger() {
      return integer;
    }

    public String getString() {
      return string;
    }
  }

  private static String normalize(final String s) {
    return s.strip().replaceAll("\\R", "\n");
  }

  private final JsonFormatFunction function = new JsonFormatFunction();

  @Test
  public void nullArg() {
    assertThat(function.apply(null), is("null"));
  }

  @Test
  public void stringArg() {
    // Strings serialize as JSON strings (quoted)
    assertThat(function.apply("hello, world"), is("\"hello, world\""));
  }

  @Test
  public void listArg() {
    assertThat(
        normalize(function.apply(List.of("one", "two", "three"))),
        is(
            """
            [ "one", "two", "three" ]\
            """));
  }

  @Test
  public void mapArg() {
    final TreeMap<String, Integer> map = new TreeMap<>();
    map.put("one", 1);
    map.put("three", 3);
    map.put("two", 2);
    assertThat(
        normalize(function.apply(map)),
        is(
            """
            {
              "one" : 1,
              "three" : 3,
              "two" : 2
            }\
            """));
  }

  @Test
  public void objectArg() {
    final String result = function.apply(new SomeClass("hello, world", 42));
    assertThat(result, containsString("\"string\" : \"hello, world\""));
    assertThat(result, containsString("\"integer\" : 42"));
  }
}
