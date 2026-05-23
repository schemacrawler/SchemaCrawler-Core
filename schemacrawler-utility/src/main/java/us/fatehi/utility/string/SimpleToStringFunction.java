/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.string;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Converts an object to a human-readable string, handling primitives, arrays, collections, and maps
 * with structured formatting.
 *
 * <p><b>Caveats:</b>
 *
 * <ol>
 *   <li><b>Do not call from {@code toString()}</b> — passing {@code this} inside a class's own
 *       {@code toString()} will recurse infinitely and cause a {@link StackOverflowError}. This
 *       function is intended to be called <em>on</em> an object from outside that object.
 *   <li><b>Limited type support</b> — only primitives, primitive wrappers, {@link String}, {@link
 *       BigDecimal}, arrays, {@link Collection}, and {@link Map} receive structured formatting. All
 *       other types fall through to {@link String#valueOf(Object)}, which calls the object's own
 *       {@code toString()}.
 *   <li><b>Fallback serializer</b> — intended as a backup when Jackson is not available on the
 *       classpath. For richer, schema-aware serialization prefer {@link ObjectToJsonString}.
 * </ol>
 */
public final class SimpleToStringFunction implements Function<Object, String> {

  private final boolean isData;

  public SimpleToStringFunction() {
    this(false);
  }

  public SimpleToStringFunction(boolean isData) {
    this.isData = isData;
  }

  @Override
  public String apply(final Object value) {
    if (value == null) {
      return isData ? "NULL" : "";
    }
    if (value instanceof final Collection<?> collection) {
      return formatCollection(collection);
    }
    if (value instanceof final Map<?, ?> map) {
      return formatMap(map);
    }
    if (value != null && value.getClass().isArray()) {
      return formatArray(value);
    }
    if (value instanceof final Number number) {
      return formatNumber(number);
    }
    return String.valueOf(value);
  }

  private String formatArray(final Object array) {
    final int len = Array.getLength(array);
    if (len == 0) {
      return "";
    }
    return IntStream.range(0, len)
        .mapToObj(i -> String.valueOf(Array.get(array, i)))
        .collect(Collectors.joining(", "));
  }

  private String formatCollection(final Collection<?> collection) {
    if (collection.isEmpty()) {
      return "";
    }
    return collection.stream().map(String::valueOf).collect(Collectors.joining(", "));
  }

  private String formatMap(final Map<?, ?> map) {
    requireNonNull(map, "No map provided");
    if (map.isEmpty()) {
      return "{ }";
    }
    return map.entrySet().stream()
        .map("  %s"::formatted)
        .collect(
            Collectors.joining(
                "," + System.lineSeparator(),
                "{" + System.lineSeparator(),
                System.lineSeparator() + "}"));
  }

  private String formatNumber(Number number) {
    final double d = number.doubleValue();
    if (Double.isFinite(d) && d == Math.rint(d)) {
      return number.toString();
    }
    // Avoid floating-point imprecision across operating systems
    final int scale = 2;
    final BigDecimal roundedNumber =
        new BigDecimal(number.toString()).setScale(scale, RoundingMode.HALF_UP);
    return roundedNumber.toString();
  }
}
