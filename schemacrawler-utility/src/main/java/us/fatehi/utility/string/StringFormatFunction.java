/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.string;

import java.lang.reflect.Array;
import java.util.Formatter;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import us.fatehi.utility.Utility;

/**
 * Formats a single object, a {@link List}, or an array using a {@link java.util.Formatter
 * printf-style} format string. Package-private; use {@link StringFormat} for public access.
 *
 * <p><b>Caveats:</b>
 *
 * <ol>
 *   <li><b>Null or blank format returns the format string unchanged</b> — no exception is thrown;
 *       the raw format string is returned as-is.
 *   <li><b>Empty {@link List} or empty array returns the format string unchanged</b> — consistent
 *       with the null-arg behavior above.
 *   <li><b>Formatting errors are swallowed</b> — if {@link java.util.Formatter} throws (e.g. a type
 *       mismatch), the error is logged at {@code FINEST} and an empty string is returned.
 *   <li><b>Primitive arrays are auto-boxed</b> — elements of {@code int[]}, {@code double[]}, etc.
 *       are boxed via {@link java.lang.reflect.Array#get Array.get()} before formatting.
 * </ol>
 */
final class StringFormatFunction implements Function<Object, String> {

  private static final Logger LOGGER = Logger.getLogger(StringFormatFunction.class.getName());

  private final String format;

  public StringFormatFunction(final String format) {
    // Be tolerant - allow null or blank format strings
    this.format = format;
  }

  @Override
  public String apply(final Object arg) {
    if (Utility.isBlank(format)) {
      return format;
    }

    final Object[] args;
    if (arg instanceof final List<?> list) {
      if (list.isEmpty()) {
        return format;
      }
      args = list.toArray();
    } else if (arg != null && arg.getClass().isArray()) {
      final int len = Array.getLength(arg);
      if (len == 0) {
        return format;
      }
      args = IntStream.range(0, len).mapToObj(i -> Array.get(arg, i)).toArray();
    } else {
      if (arg == null) {
        return format;
      }
      args = new Object[] {arg};
    }

    try (final Formatter formatter = new Formatter()) {
      return formatter.format(format, args).toString();
    } catch (final Exception e) {
      // NOTE: Do not output arguments, since the toString on argument may throw an exception
      // obscuring this one
      LOGGER.log(Level.FINEST, "Error logging message <%s>".formatted(format));
      return "";
    }
  }
}
