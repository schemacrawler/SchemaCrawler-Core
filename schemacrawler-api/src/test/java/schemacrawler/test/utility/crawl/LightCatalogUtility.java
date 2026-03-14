/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import static us.fatehi.test.utility.TestObjectUtility.returnEmpty;
import static us.fatehi.utility.Utility.requireNotBlank;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.CrawlInfo;
import schemacrawler.schema.DatabaseInfo;
import schemacrawler.schema.JdbcDriverInfo;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.Version;
import us.fatehi.test.utility.TestObjectUtility;
import us.fatehi.utility.UtilityMarker;
import us.fatehi.utility.property.BaseProductVersion;
import us.fatehi.utility.property.ProductVersion;

@UtilityMarker
public class LightCatalogUtility {

  private static final class NamedObjectInvocationHandler
      implements InvocationHandler, Serializable {
    @Serial private static final long serialVersionUID = 1L;

    private final String name;

    NamedObjectInvocationHandler(final String name) {
      this.name = requireNotBlank(name, "No name provided");
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
        throws Throwable {
      final String methodName = method.getName();
      return switch (methodName) {
        case "getName", "getFullName", "toString" -> name;
        case "key" -> new NamedObjectKey(name);
        case "equals" -> invokeEquals(args[0]);
        case "hashCode" -> System.identityHashCode(name);
        case "compareTo" -> invokeCompareTo(args[0]);
        default -> returnEmpty(method);
      };
    }

    private Object invokeCompareTo(final Object other) {
      if (other == null) {
        return 1;
      }
      final String otherName = ((ColumnDataType) other).getName();
      return name.compareTo(otherName);
    }

    private Object invokeEquals(final Object other) {
      if (other == null) {
        return false;
      }
      final String otherName = ((ColumnDataType) other).getName();
      return name.equals(otherName);
    }
  }

  public static Catalog lightCatalog() {
    return lightCatalog(new Table[0]);
  }

  public static Catalog lightCatalog(final Table... tables) {
    final List<Table> tablesList;
    if (tables == null) {
      tablesList = List.of();
    } else {
      tablesList = List.of(tables);
    }

    final InvocationHandler handler =
        (proxy, method, args) -> {
          final String methodName = method.getName();

          return switch (methodName) {
            case "key" -> new NamedObjectKey("light-catalog");
            case "getName", "getFullName", "toString" -> "light-catalog";
            case "equals" -> proxy == args[0];
            case "hashCode" -> System.identityHashCode(proxy);
            case "getCrawlInfo" -> lightCrawlInfo();
            case "getJdbcDriverInfo" -> TestObjectUtility.makeTestObject(JdbcDriverInfo.class);
            case "getDatabaseInfo" -> lightDatabaseInfo();
            case "getTables" -> tablesList;
            default -> returnEmpty(method);
          };
        };

    return (Catalog)
        Proxy.newProxyInstance(
            Catalog.class.getClassLoader(), new Class<?>[] {Catalog.class}, handler);
  }

  public static CrawlInfo lightCrawlInfo() {
    final Class<CrawlInfo> clazz = CrawlInfo.class;
    final InvocationHandler handler =
        (proxy, method, args) -> {
          final Class<?> returnType = method.getReturnType();
          if (ProductVersion.class.isAssignableFrom(returnType)) {
            return Version.version();
          }
          return returnEmpty(method);
        };

    return (CrawlInfo)
        Proxy.newProxyInstance(
            LightCatalogUtility.class.getClassLoader(), new Class<?>[] {clazz}, handler);
  }

  public static DatabaseInfo lightDatabaseInfo() {
    final Class<DatabaseInfo> clazz = DatabaseInfo.class;
    final InvocationHandler handler =
        (proxy, method, args) -> {
          final String methodName = method.getName();

          final String productVersion = Version.version().getProductVersion();
          final String productName = "Test Database";
          switch (methodName) {
            case "getDatabaseProductName":
              return productName;
            case "getDatabaseProductVersion":
              return productVersion;
            default:
              final Class<?> returnType = method.getReturnType();
              if (ProductVersion.class.isAssignableFrom(returnType)) {
                return new BaseProductVersion(productName, productVersion);
              }
              return returnEmpty(method);
          }
        };

    return (DatabaseInfo)
        Proxy.newProxyInstance(
            LightCatalogUtility.class.getClassLoader(), new Class<?>[] {clazz}, handler);
  }

  public static <T extends NamedObject> T lightNamedObject(
      final Class<T> clazz, final String name) {
    return (T)
        Proxy.newProxyInstance(
            NamedObject.class.getClassLoader(),
            new Class<?>[] {clazz},
            new NamedObjectInvocationHandler(name));
  }
}
