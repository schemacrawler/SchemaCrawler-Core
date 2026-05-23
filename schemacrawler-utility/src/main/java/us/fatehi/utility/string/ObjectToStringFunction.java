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

public final class ObjectToStringFunction implements Function<Object, String> {

  private final boolean isData;

  public ObjectToStringFunction() {
    this(false);
  }

  public ObjectToStringFunction(boolean isData) {
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
      final double d = number.doubleValue();
      if (Double.isFinite(d) && d == Math.rint(d)) {
        return String.valueOf(number.longValue());
      }
      // Avoid floating-point imprecision across operating systems
      final int scale = 2;
      final BigDecimal roundedNumber =
          new BigDecimal(number.toString()).setScale(scale, RoundingMode.HALF_UP);
      return roundedNumber.toString();
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
}
