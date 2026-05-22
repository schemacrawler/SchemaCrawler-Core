/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.string;

import java.util.Formatter;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import us.fatehi.utility.Utility;

public final class StringFormatFunction implements Function<Object, String> {

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
