/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.utility;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import org.junit.jupiter.api.Test;
import schemacrawler.schemacrawler.exceptions.ExecutionRuntimeException;

public class SerializedObjectInputStreamTest {

  private static final Predicate<String> ALLOW_ALL = className -> true;
  private static final Predicate<String> DENY_CLASSES = new DeniedClassesFilter();

  @Test
  public void allowedConcurrentClassesAreRead() {
    final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
    map.put("a", "b");
    final ConcurrentHashMap<String, String> result =
        SerializedObjectInputStream.read(serialize(map), ALLOW_ALL);
    assertThat(result.get("a"), is("b"));
  }

  @Test
  public void deniedArrayComponents() {
    assertThat(DENY_CLASSES.test("[Ljava.util.logging.Level;"), is(true));
    assertThat(DENY_CLASSES.test("[[Ljava.lang.Thread;"), is(true));
  }

  @Test
  public void deniedPrefixes() {
    assertThat(DENY_CLASSES.test("java.lang.reflect.Method"), is(true));
    assertThat(DENY_CLASSES.test("java.lang.invoke.MethodHandle"), is(true));
    assertThat(DENY_CLASSES.test("java.util.logging.Level"), is(true));
    assertThat(DENY_CLASSES.test("java.util.prefs.Preferences"), is(true));
  }

  @Test
  public void deniedTypes() {
    for (final String className :
        new String[] {
          "java.lang.Class",
          "java.lang.System",
          "java.lang.ProcessBuilder",
          "java.lang.Process",
          "java.lang.Runtime",
          "java.lang.Thread",
          "java.lang.ThreadGroup",
          "java.lang.ClassLoader",
          "java.util.ServiceLoader"
        }) {
      assertThat(className, DENY_CLASSES.test(className), is(true));
    }
  }

  @Test
  public void denialWinsOverAllowPredicate() {
    assertThrows(
        ExecutionRuntimeException.class,
        () -> SerializedObjectInputStream.read(serialize(Level.INFO), ALLOW_ALL));
    assertThrows(
        ExecutionRuntimeException.class,
        () -> SerializedObjectInputStream.read(serialize(new Level[] {Level.INFO}), ALLOW_ALL));
  }

  @Test
  public void notAllowedIsRejected() {
    assertThrows(
        ExecutionRuntimeException.class,
        () -> SerializedObjectInputStream.read(serialize(new ArrayList<>()), className -> false));
  }

  @Test
  public void notDenied() {
    assertThat(DENY_CLASSES.test("java.lang.Threading"), is(false));
    assertThat(DENY_CLASSES.test("java.lang.Thread$State"), is(false));
    assertThat(DENY_CLASSES.test("java.lang.reflection.Foo"), is(false));
    assertThat(DENY_CLASSES.test("java.util.concurrent.ConcurrentHashMap"), is(false));
    assertThat(DENY_CLASSES.test("[Ljava.lang.String;"), is(false));
    assertThat(DENY_CLASSES.test("[B"), is(false));
  }

  @Test
  public void nullPredicateIsRejected() {
    assertThrows(
        NullPointerException.class, () -> SerializedObjectInputStream.read(serialize("x"), null));
  }

  private InputStream serialize(final Serializable object) {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    SerializedObjectInputStream.save(object, out);
    return new ByteArrayInputStream(out.toByteArray());
  }
}
