/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.string;

import java.util.function.Function;

/**
 * Converts an object to a JSON string using Jackson, with a transparent fallback to {@link
 * SimpleToStringFunction} if Jackson is not available on the classpath.
 *
 * <p><b>Caveats:</b>
 *
 * <ol>
 *   <li><b>Do not call from {@code toString()}</b> — passing {@code this} inside a class's own
 *       {@code toString()} will recurse infinitely and cause a {@link StackOverflowError}. Use
 *       {@link ObjectToStringFormat} instead, which wraps the target object lazily from the
 *       outside.
 *   <li><b>Jackson optional</b> — if Jackson is absent from the classpath, silently delegates to
 *       {@link SimpleToStringFunction} so callers always receive a non-null string.
 *   <li><b>Output format varies</b> — when Jackson is present the output is valid JSON; when the
 *       fallback is active the output is plain-Java formatted text.
 * </ol>
 */
public final class ObjectToStringFunction implements Function<Object, String> {

  private static final Function<Object, String> SERIALIZER;

  static {
    // NOTE: This block handles the situation if Jackson is not available on the
    // classpath
    Function<Object, String> serializer;
    try {
      serializer = new JsonFormatFunction();
    } catch (final NoClassDefFoundError | ExceptionInInitializerError e) {
      serializer = new SimpleToStringFunction();
    }
    SERIALIZER = serializer;
  }

  @Override
  public String apply(final Object object) {
    return SERIALIZER.apply(object);
  }
}
