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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ObjectToStringFunction implements Function<Object, String> {

  @Override
  public String apply(final Object object) {
    if (object instanceof final Collection<?> collection) {
      return formatCollection(collection);
    }
    if (object instanceof final Map<?, ?> map) {
      return formatMap(map);
    }
    if (object != null && object.getClass().isArray()) {
      return formatArray(object);
    }
    return String.valueOf(object);
  }

  private static String formatArray(final Object array) {
    requireNonNull(array, "No array provided");

    final int len = Array.getLength(array);
    if (len == 0) {
      return "[ ]";
    }
    return IntStream.range(0, len)
        .mapToObj(i -> "  " + quoted(Array.get(array, i)))
        .collect(
            Collectors.joining(
                "," + System.lineSeparator(),
                "[" + System.lineSeparator(),
                System.lineSeparator() + "]"));
  }

  private static String formatCollection(final Collection<?> collection) {
    requireNonNull(collection, "No collection provided");
    if (collection.isEmpty()) {
      return "[ ]";
    }
    return collection.stream()
        .map(item -> "  " + quoted(item))
        .collect(
            Collectors.joining(
                "," + System.lineSeparator(),
                "[" + System.lineSeparator(),
                System.lineSeparator() + "]"));
  }

  private static String formatMap(final Map<?, ?> map) {
    requireNonNull(map, "No map provided");
    if (map.isEmpty()) {
      return "{ }";
    }
    return map.entrySet().stream()
        .map(e -> "  " + quoted(e.getKey()) + " : " + quoted(e.getValue()))
        .collect(
            Collectors.joining(
                "," + System.lineSeparator(),
                "{" + System.lineSeparator(),
                System.lineSeparator() + "}"));
  }

  private static boolean isPrimitive(final Object object) {
    final Class<?> objectClass = object.getClass();
    return objectClass.isPrimitive()
        || List.of(
                Integer.class,
                Long.class,
                Double.class,
                Float.class,
                Boolean.class,
                Byte.class,
                Void.class,
                Short.class)
            .contains(objectClass);
  }

  private static String quoted(final Object object) {
    final String stringValue = String.valueOf(object);
    if (object == null || isPrimitive(object)) {
      return stringValue;
    }
    return "\"" + stringValue + "\"";
  }
}
