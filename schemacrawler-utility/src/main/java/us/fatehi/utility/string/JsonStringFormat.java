/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.string;

import java.util.function.Function;
import java.util.function.Supplier;

public final class JsonStringFormat implements Supplier<String> {

  private static final Function<Object, String> SERIALIZER;

  static {
    // NOTE: This block handles the situation if Jackson is not available on the classpath
    Function<Object, String> serializer;
    try {
      serializer = new JsonFormatFunction();
    } catch (final NoClassDefFoundError | ExceptionInInitializerError e) {
      serializer = String::valueOf;
    }
    SERIALIZER = serializer;
  }

  private final Object args;

  public JsonStringFormat(final Object args) {
    this.args = args;
  }

  @Override
  public String get() {
    return SERIALIZER.apply(args);
  }

  @Override
  public String toString() {
    return get();
  }
}
