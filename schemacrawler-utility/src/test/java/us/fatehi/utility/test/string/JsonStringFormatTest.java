/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.test.string;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import us.fatehi.utility.string.JsonStringFormat;

public class JsonStringFormatTest {

  static class SomeClass {
    private String string;
    private int integer;

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

  @Test
  public void nullArgs() {
    assertThat(new JsonStringFormat(null).get(), is("null"));
  }

  @Test
  public void stringArg() {
    // Strings serialize as JSON strings (quoted)
    assertThat(new JsonStringFormat("hello, world").get(), is("\"hello, world\""));
    // toString() delegates to get()
    assertThat(
        new JsonStringFormat("hello, world").get(),
        is(new JsonStringFormat("hello, world").toString()));
  }

  @Test
  public void listArg() {
    final List<String> list = List.of("one", "two", "three");
    assertThat(new JsonStringFormat(list).get(), is("[ \"one\", \"two\", \"three\" ]"));
  }

  @Test
  public void mapArg() {
    final TreeMap<String, Integer> map = new TreeMap<>();
    map.put("one", 1);
    map.put("three", 3);
    map.put("two", 2);
    final String result = new JsonStringFormat(map).get();
    assertThat(result, containsString("\"one\" : 1"));
    assertThat(result, containsString("\"three\" : 3"));
    assertThat(result, containsString("\"two\" : 2"));
  }

  @Test
  public void objectArg() {
    final String result = new JsonStringFormat(new SomeClass("hello, world", 42)).get();
    assertThat(result, containsString("\"string\" : \"hello, world\""));
    assertThat(result, containsString("\"integer\" : 42"));
  }
}
