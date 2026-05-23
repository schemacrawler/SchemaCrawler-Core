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
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import us.fatehi.utility.string.SimpleToStringFunction;

public class SimpleToStringFunctionTest {

  /** Object that directly recurses back into SimpleToStringFunction from toString(). */
  static class SelfReferencing {
    private final SimpleToStringFunction fn = new SimpleToStringFunction();

    @Override
    public String toString() {
      return fn.apply(this);
    }
  }

  /**
   * Pair of objects that form an indirect cycle: A.toString() → apply(B) → B.toString() → apply(A).
   */
  static class CycleA {
    CycleB partner;
    private final SimpleToStringFunction fn = new SimpleToStringFunction();

    @Override
    public String toString() {
      return fn.apply(partner);
    }
  }

  static class CycleB {
    CycleA partner;
    private final SimpleToStringFunction fn = new SimpleToStringFunction();

    @Override
    public String toString() {
      return fn.apply(partner);
    }
  }

  static class SomeClass {
    @Override
    public String toString() {
      return "some-object";
    }
  }

  private final SimpleToStringFunction function = new SimpleToStringFunction();

  @Test
  public void arbitraryObject() {
    assertThat(function.apply(new SomeClass()), is("some-object"));
  }

  @Test
  public void arrayArg() {
    assertThat(function.apply(new String[] {"a", "b", "c"}), is("a, b, c"));
  }

  @Test
  public void collectionArg() {
    assertThat(function.apply(List.of("one", "two", "three")), is("one, two, three"));
  }

  @Test
  public void collectionWithMixedTypes() {
    assertThat(function.apply(Arrays.asList(null, "text", 42)), is("null, text, 42"));
  }

  @Test
  public void decimalArray() {
    assertThat(function.apply(new double[] {1, 2.99, Math.PI}), is("1.0, 2.99, " + Math.PI));
  }

  @Test
  public void decimals() {
    assertThat(function.apply(1.0), is("1.0"));
    assertThat(function.apply(2.99), is("2.99"));
    assertThat(function.apply(Math.PI), is("3.14"));
  }

  @Test
  public void emptyArray() {
    assertThat(function.apply(new String[] {}), is(emptyString()));
  }

  @Test
  public void emptyCollection() {
    assertThat(function.apply(List.of()), is(emptyString()));
  }

  @Test
  public void emptyMap() {
    assertThat(function.apply(new TreeMap<>()), is("{ }"));
  }

  @Test
  public void intArray() {
    assertThat(function.apply(new int[] {1, 2, 3}), is("1, 2, 3"));
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
              alpha=1,
              beta=2
            }
            """));
  }

  @Test
  public void nullArg() {
    assertThat(function.apply(null), is(""));
    assertThat(new SimpleToStringFunction(true).apply(null), is("NULL"));
  }

  @Test
  public void stringArg() {
    assertThat(function.apply("hello"), is("hello"));
  }

  @Test
  public void directCycle() {
    // SelfReferencing.toString() calls apply(this) — should cause StackOverflow
    final SelfReferencing obj = new SelfReferencing();
    assertThrows(StackOverflowError.class, () -> obj.toString());
  }

  @Test
  public void indirectCycle() {
    // A.toString() → apply(B), B.toString() → apply(A) — second hop causes StackOverflow
    final CycleA a = new CycleA();
    final CycleB b = new CycleB();
    a.partner = b;
    b.partner = a;
    // a.toString() calls fn.apply(b) which calls b.toString() which calls fn.apply(a)
    assertThrows(StackOverflowError.class, () -> a.toString());
  }
}
