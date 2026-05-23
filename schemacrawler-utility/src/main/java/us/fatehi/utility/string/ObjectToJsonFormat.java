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

public final class ObjectToJsonFormat implements Supplier<String> {

  private final Function<Object, String> serializer;
  private final Object args;

  public ObjectToJsonFormat(final Object args) {
    serializer = new ObjectToJsonFunction();
    this.args = args;
  }

  @Override
  public String get() {
    return serializer.apply(args);
  }

  @Override
  public String toString() {
    return get();
  }
}
