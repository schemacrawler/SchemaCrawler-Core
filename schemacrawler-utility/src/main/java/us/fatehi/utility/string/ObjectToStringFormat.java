/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.string;

import static us.fatehi.utility.Utility.isBlank;

import java.util.function.Supplier;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

public final class ObjectToStringFormat implements Supplier<String> {

  private static final JsonMapper MAPPER =
      JsonMapper.builder()
          .enable(SerializationFeature.INDENT_OUTPUT)
          .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
          .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
          .build();

  private final String context;
  private final Object args;

  public ObjectToStringFormat(final Object args) {
    this(null, args);
  }

  public ObjectToStringFormat(final String context, final Object args) {
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
      try {
        buffer.append(MAPPER.writeValueAsString(args));
      } catch (final JacksonException e) {
        buffer.append(String.valueOf(args));
      }
    }
    return buffer.toString();
  }

  @Override
  public String toString() {
    return get();
  }
}
