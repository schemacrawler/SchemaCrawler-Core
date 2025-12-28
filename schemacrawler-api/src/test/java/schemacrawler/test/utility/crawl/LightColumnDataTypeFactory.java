/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import static java.lang.reflect.Proxy.newProxyInstance;
import static us.fatehi.utility.Utility.requireNotBlank;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;
import java.util.List;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.utility.JavaSqlTypes;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public class LightColumnDataTypeFactory {

  private static final class ColumnDataTypeInvocationHandler implements InvocationHandler {

    private final String name;
    private final boolean isEnumerated;
    private final List<String> enumValues;

    ColumnDataTypeInvocationHandler(final String name) {
      this(name, null);
    }

    ColumnDataTypeInvocationHandler(final String name, final List<String> enumValues) {
      this.name = requireNotBlank(name, "No name provided");
      isEnumerated = enumValues != null;
      this.enumValues = isEnumerated ? enumValues : Collections.emptyList();
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
        throws Throwable {
      final String methodName = method.getName();
      return switch (methodName) {
        case "getName", "getDatabaseSpecificTypeName", "getStandardTypeName" -> name;
        case "getJavaSqlType" -> new JavaSqlTypes().getFromJavaSqlTypeName(name);
        case "isEnumerated" -> isEnumerated;
        case "getEnumValues" -> enumValues;
        case "toString" -> toString();
        default -> throw new SQLFeatureNotSupportedException(methodName);
      };
    }

    @Override
    public String toString() {
      if (isEnumerated) {
        return "%s %s".formatted(name, enumValues);
      }
      return name;
    }
  }

  public static ColumnDataType columnDataType(final String name) {
    return (ColumnDataType)
        newProxyInstance(
            ColumnDataType.class.getClassLoader(),
            new Class[] {ColumnDataType.class},
            new ColumnDataTypeInvocationHandler(name));
  }

  public static ColumnDataType enumColumnDataType() {
    return (ColumnDataType)
        newProxyInstance(
            ColumnDataType.class.getClassLoader(),
            new Class[] {ColumnDataType.class},
            new ColumnDataTypeInvocationHandler("VARCHAR", List.of("VALUE1", "VALUE2")));
  }

  private LightColumnDataTypeFactory() {
    // Prevent instantiation
  }
}
