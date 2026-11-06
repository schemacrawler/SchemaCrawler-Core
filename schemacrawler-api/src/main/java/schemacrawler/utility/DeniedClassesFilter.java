/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.utility;

import static us.fatehi.utility.Utility.isBlank;

import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Predicate that matches class names which must never be deserialized. It returns {@code true} when
 * a class name is <em>denied</em>, so it must be negated with {@link Predicate#not(Predicate)} to
 * be used as an allow check, for example {@code Predicate.not(new
 * DeniedClassesFilter()).and(allowed)}.
 *
 * <p>Denied classes are identified by package prefix or by exact class name. Array descriptors are
 * unwrapped, so an array of a denied class is also denied. A blank name is not denied by this
 * filter. Other classes, such as the {@code java.util.concurrent} collections used by catalogs, are
 * not denied here, and remain subject to the allow policy of the serialization format.
 */
final class DeniedClassesFilter implements Predicate<String> {

  private static final Logger LOGGER = Logger.getLogger(DeniedClassesFilter.class.getName());

  private static final Predicate<String> DENIED_PACKAGE =
      className ->
          Stream.of(
                  "java.lang.reflect.",
                  "java.lang.invoke.",
                  "java.lang.ref.",
                  "java.lang.module.",
                  "java.lang.foreign.",
                  "java.lang.management.",
                  "java.util.logging.",
                  "java.util.prefs.")
              .anyMatch(className::startsWith);

  private static final Predicate<String> DENIED_TYPE =
      Set.of(
              "java.lang.Class",
              "java.lang.System",
              "java.lang.ProcessBuilder",
              "java.lang.Process",
              "java.lang.Runtime",
              "java.lang.Thread",
              "java.lang.ThreadGroup",
              "java.lang.ClassLoader",
              "java.lang.ThreadLocal",
              "java.lang.InheritableThreadLocal",
              "java.util.ServiceLoader",
              "java.util.Timer",
              "java.util.TimerTask",
              "java.net.Socket",
              "java.net.ServerSocket",
              "java.net.DatagramSocket",
              "java.util.concurrent.ThreadPoolExecutor",
              "java.util.concurrent.ScheduledThreadPoolExecutor",
              "java.util.concurrent.ForkJoinPool",
              "java.util.concurrent.FutureTask",
              "java.util.concurrent.CompletableFuture")
          ::contains;

  private static final Predicate<String> DENIED_CLASS = DENIED_PACKAGE.or(DENIED_TYPE);

  // Array descriptors are unwrapped for denial only; primitive arrays are left
  // unchanged
  private static String componentTypeName(final String className) {
    int dimensions = 0;
    while (dimensions < className.length() && className.charAt(dimensions) == '[') {
      dimensions++;
    }
    final boolean isReferenceArray =
        dimensions > 0
            && className.length() > dimensions + 1
            && className.charAt(dimensions) == 'L'
            && className.endsWith(";");
    return isReferenceArray
        ? className.substring(dimensions + 1, className.length() - 1)
        : className;
  }

  /**
   * Tests whether a class name is denied.
   *
   * @param text Serialized class name, which may be an array descriptor
   * @return {@code true} if the class must not be deserialized; {@code false} if it is not denied
   *     by this filter or the name is blank
   */
  @Override
  public boolean test(final String className) {
    if (isBlank(className)) {
      return true;
    }
    final String componentTypeName = componentTypeName(className);
    final boolean denied = DENIED_CLASS.test(componentTypeName);
    if (denied) {
      LOGGER.log(Level.FINER, "Denied deserializing <%s>".formatted(className));
    }
    return denied;
  }
}
