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
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import us.fatehi.utility.string.ObjectToStringFunction;

public class ObjectToStringFunctionTest {

  static class SomeClass {
    @Override
    public String toString() {
      return "some-object";
    }
  }

  private final ObjectToStringFunction function = new ObjectToStringFunction();

  @Test
  public void nullArg() {
    assertThat(function.apply(null), is("null"));
  }

  @Test
  public void stringArg() {
    // top-level apply() uses String.valueOf() directly — no quoting at root level
    assertThat(function.apply("hello"), is("hello"));
  }

  @Test
  public void arbitraryObject() {
    assertThat(function.apply(new SomeClass()), is("some-object"));
  }

  @Test
  public void emptyCollection() {
    assertThat(function.apply(List.of()), is("[ ]"));
  }

  @Test
  public void collectionArg() {
    assertThat(
        function.apply(List.of("one", "two", "three")),
        equalToCompressingWhiteSpace(
            """
            [
              "one",
              "two",
              "three"
            ]\
            """));
  }

  @Test
  public void collectionWithMixedTypes() {
    // strings and Character are quoted inside a collection; Integer is not
    assertThat(
        function.apply(Arrays.asList(null, "text", 42)),
        equalToCompressingWhiteSpace(
            """
            [
              null,
              "text",
              42
            ]\
            """));
  }

  @Test
  public void emptyMap() {
    assertThat(function.apply(new TreeMap<>()), is("{ }"));
  }

  @Test
  public void mapArg() {
    final TreeMap<String, Integer> map = new TreeMap<>();
    map.put("alpha", 1);
    map.put("beta", 2);
    assertThat(
        function.apply(map),
        equalToCompressingWhiteSpace(
            """
            {
              "alpha" : 1,
              "beta" : 2
            }\
            """));
  }

  @Test
  public void emptyArray() {
    assertThat(function.apply(new String[] {}), is("[ ]"));
  }

  @Test
  public void arrayArg() {
    assertThat(
        function.apply(new String[] {"a", "b", "c"}),
        equalToCompressingWhiteSpace(
            """
            [
              "a",
              "b",
              "c"
            ]\
            """));
  }

  @Test
  public void intArray() {
    // Array.get() boxes int to Integer — unquoted
    assertThat(
        function.apply(new int[] {1, 2, 3}),
        equalToCompressingWhiteSpace(
            """
            [
              1,
              2,
              3
            ]\
            """));
  }
}
