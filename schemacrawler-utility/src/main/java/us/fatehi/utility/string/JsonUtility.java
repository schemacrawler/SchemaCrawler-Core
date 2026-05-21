/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.string;

import static java.util.Objects.requireNonNull;
import static tools.jackson.core.StreamReadFeature.IGNORE_UNDEFINED;
import static tools.jackson.core.StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION;
import static tools.jackson.core.StreamWriteFeature.IGNORE_UNKNOWN;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES;
import static tools.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS;
import static tools.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static tools.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;
import static tools.jackson.databind.SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID;

import java.util.function.Function;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.json.JsonMapper;
import us.fatehi.utility.UtilityMarker;

/** Jackson-backed serializer. Isolated so that Jackson classes are only loaded on demand. */
@UtilityMarker
public final class JsonUtility {

  public static final ObjectMapper mapper = newConfiguredObjectMapper(JsonMapper.builder());

  static Function<Object, String> jsonSerializer() {
    return args -> {
      try {
        return mapper.writeValueAsString(args);
      } catch (final JacksonException e) {
        return String.valueOf(args);
      }
    };
  }

  private static ObjectMapper newConfiguredObjectMapper(
      final MapperBuilder<? extends ObjectMapper, ?> mapperBuilder) {

    requireNonNull(mapperBuilder, "No mapper builder provided");
    mapperBuilder.enable(INCLUDE_SOURCE_IN_LOCATION, IGNORE_UNDEFINED);
    mapperBuilder.disable(FAIL_ON_NULL_FOR_PRIMITIVES);
    //
    mapperBuilder.enable(IGNORE_UNKNOWN);
    mapperBuilder.enable(ORDER_MAP_ENTRIES_BY_KEYS, INDENT_OUTPUT, USE_EQUALITY_FOR_OBJECT_ID);
    mapperBuilder.enable(ACCEPT_CASE_INSENSITIVE_ENUMS);

    final ObjectMapper objectMapper = mapperBuilder.build();
    return objectMapper;
  }
}
