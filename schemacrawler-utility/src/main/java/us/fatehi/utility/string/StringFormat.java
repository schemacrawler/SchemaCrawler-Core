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
 * A lazy {@link java.util.function.Supplier Supplier&lt;String&gt;} that formats a message using a
 * {@link java.util.Formatter printf-style} format string and zero or more arguments. Intended for
 * use with {@link java.util.logging.Logger} so that string formatting only occurs when the log
 * level is active.
 *
 * <p><b>Caveats:</b>
 *
 * <ol>
 *   <li><b>Arguments are captured at construction time</b> — the varargs array is stored as-is; if
 *       any argument is mutable, its state at the time {@link #get()} is called determines the
 *       output.
 *   <li><b>Formatting errors are swallowed</b> — delegates to {@link StringFormatFunction}, which
 *       logs at {@code FINEST} and returns an empty string on any formatting failure.
 *   <li><b>{@code toString()} delegates to {@code get()}</b> — this makes instances directly usable
 *       wherever a {@link String} representation is needed, such as log message arguments.
 * </ol>
 */
public final class StringFormat implements Supplier<String> {

  private final Function<Object, String> serializer;
  private final Object[] args;

  public StringFormat(final String format, final Object... args) {
    serializer = new StringFormatFunction(format);
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
