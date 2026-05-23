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

/**
 * A lazy {@link java.util.function.Supplier Supplier&lt;String&gt;} that serializes an object to
 * JSON (with a plain-text fallback) on demand. Intended for use with {@link
 * java.util.logging.Logger} so that serialization only occurs when the log level is active.
 *
 * <p><b>Caveats:</b>
 *
 * <ol>
 *   <li><b>Pass the target object, not {@code this}</b> — construct with the object you want to
 *       serialize. Passing {@code this} from inside that object's own {@code toString()} will
 *       recurse infinitely and cause a {@link StackOverflowError}.
 *   <li><b>Output format varies</b> — delegates to {@link ObjectToStringFunction}, so output is
 *       JSON when Jackson is on the classpath and plain-text otherwise.
 *   <li><b>{@code toString()} delegates to {@code get()}</b> — this makes instances directly usable
 *       wherever a {@link String} representation is needed, such as log message arguments.
 * </ol>
 */
public final class ObjectToStringFormat implements Supplier<String> {

  private final Function<Object, String> serializer;
  private final Object args;

  public ObjectToStringFormat(final Object args) {
    serializer = new ObjectToStringFunction();
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
