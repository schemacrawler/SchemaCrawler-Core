/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.string;

import java.util.function.Function;

public final class ObjectToJsonFunction implements Function<Object, String> {

  private static final Function<Object, String> SERIALIZER;

  static {
    // NOTE: This block handles the situation if Jackson is not available on the
    // classpath
    Function<Object, String> serializer;
    try {
      serializer = new JsonFormatFunction();
    } catch (final NoClassDefFoundError | ExceptionInInitializerError e) {
      serializer = new ObjectToStringFunction();
    }
    SERIALIZER = serializer;
  }

  @Override
  public String apply(final Object object) {
    return SERIALIZER.apply(object);
  }
}
