/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import us.fatehi.utility.ObjectToString;

public class ObjectToStringTest {

  static class SomeClass {
    @Override
    public String toString() {
      return "some-object";
    }
  }

  private static String normalize(final String s) {
    return s.strip().replaceAll("\\R", "\n");
  }

  @Test
  public void nullValue() {
    assertThat(ObjectToString.toString(null), is("null"));
  }

  @Test
  public void stringValue() {
    // Top-level toString() uses String.valueOf() directly — no quoting at root level
    assertThat(ObjectToString.toString("hello"), is("hello"));
  }

  @Test
  public void primitiveWrappers() {
    assertThat(ObjectToString.toString(42), is("42"));
    assertThat(ObjectToString.toString(42L), is("42"));
    assertThat(ObjectToString.toString(3.14), is("3.14"));
    assertThat(ObjectToString.toString(true), is("true"));
    assertThat(ObjectToString.toString((byte) 1), is("1"));
    assertThat(ObjectToString.toString((short) 7), is("7"));
  }

  @Test
  public void characterValue() {
    assertThat(ObjectToString.toString('a'), is("a"));
  }

  @Test
  public void arbitraryObject() {
    // Top-level toString() calls String.valueOf() — no quoting at root level
    assertThat(ObjectToString.toString(new SomeClass()), is("some-object"));
  }

  @Test
  public void collectionElementQuoting() {
    // inside a collection: strings and Character are quoted, Integer is not
    assertThat(
        normalize(ObjectToString.toString(List.of("text", 42, 'c'))),
        is(
            """
            [
              "text",
              42,
              "c"
            ]\
            """));
  }

  @Test
  public void emptyCollection() {
    assertThat(ObjectToString.toString(List.of()), is("[ ]"));
  }

  @Test
  public void collectionOfStrings() {
    assertThat(
        normalize(ObjectToString.toString(List.of("one", "two", "three"))),
        is(
            """
            [
              "one",
              "two",
              "three"
            ]\
            """));
  }

  @Test
  public void collectionOfIntegers() {
    assertThat(
        normalize(ObjectToString.toString(List.of(1, 2, 3))),
        is(
            """
            [
              1,
              2,
              3
            ]\
            """));
  }

  @Test
  public void collectionWithNullElement() {
    assertThat(
        normalize(ObjectToString.toString(Arrays.asList(null, "value"))),
        is(
            """
            [
              null,
              "value"
            ]\
            """));
  }

  @Test
  public void emptyMap() {
    assertThat(ObjectToString.toString(new TreeMap<>()), is("{ }"));
  }

  @Test
  public void mapWithStringEntries() {
    final TreeMap<String, String> map = new TreeMap<>();
    map.put("alpha", "first");
    map.put("beta", "second");
    assertThat(
        normalize(ObjectToString.toString(map)),
        is(
            """
            {
              "alpha" : "first",
              "beta" : "second"
            }\
            """));
  }

  @Test
  public void mapWithIntegerValues() {
    final TreeMap<String, Integer> map = new TreeMap<>();
    map.put("one", 1);
    map.put("two", 2);
    assertThat(
        normalize(ObjectToString.toString(map)),
        is(
            """
            {
              "one" : 1,
              "two" : 2
            }\
            """));
  }

  @Test
  public void emptyStringArray() {
    assertThat(ObjectToString.toString(new String[] {}), is("[ ]"));
  }

  @Test
  public void stringArray() {
    assertThat(
        normalize(ObjectToString.toString(new String[] {"a", "b"})),
        is(
            """
            [
              "a",
              "b"
            ]\
            """));
  }

  @Test
  public void intArray() {
    // Array.get() boxes int to Integer which is in the primitives list — unquoted
    assertThat(
        normalize(ObjectToString.toString(new int[] {1, 2, 3})),
        is(
            """
            [
              1,
              2,
              3
            ]\
            """));
  }

  @Test
  public void arrayWithNullElement() {
    // null is unquoted, string is quoted
    assertThat(
        normalize(ObjectToString.toString(new String[] {null, "x"})),
        is(
            """
            [
              null,
              "x"
            ]\
            """));
  }
}
