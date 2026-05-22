/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.string;

import static us.fatehi.utility.Utility.isBlank;

import java.util.function.Function;
import java.util.function.Supplier;

public final class JsonStringFormat implements Supplier<String> {

  private static final Function<Object, String> SERIALIZER;

  static {
    Function<Object, String> serializer;
    try {
      serializer = JsonUtility.jsonSerializer();
    } catch (final NoClassDefFoundError | ExceptionInInitializerError e) {
      serializer = String::valueOf;
    }
    SERIALIZER = serializer;
  }

  private final String context;
  private final Object args;

  public JsonStringFormat(final Object args) {
    this(null, args);
  }

  public JsonStringFormat(final String context, final Object args) {
    this.context = context;
    this.args = args;
  }

  @Override
  public String get() {
    final StringBuilder buffer = new StringBuilder();
    if (!isBlank(context)) {
      buffer.append(context).append(System.lineSeparator());
    }
    if (args != null) {
      buffer.append(SERIALIZER.apply(args));
    }
    return buffer.toString();
  }

  @Override
  public String toString() {
    return get();
  }
}
