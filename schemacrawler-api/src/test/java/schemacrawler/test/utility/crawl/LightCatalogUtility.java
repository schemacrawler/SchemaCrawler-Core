/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import static us.fatehi.test.utility.TestObjectUtility.mockConnection;
import static us.fatehi.test.utility.TestObjectUtility.returnEmpty;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.CrawlInfo;
import schemacrawler.schema.DatabaseInfo;
import schemacrawler.schema.JdbcDriverInfo;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.Version;
import us.fatehi.test.utility.TestObjectUtility;
import us.fatehi.utility.UtilityMarker;
import us.fatehi.utility.datasource.DatabaseConnectionSource;
import us.fatehi.utility.property.BaseProductVersion;
import us.fatehi.utility.property.ProductVersion;

@UtilityMarker
public class LightCatalogUtility {

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

  public static DatabaseConnectionSource lightDatabaseConnectionSource() {

    final InvocationHandler handler =
        (proxy, method, args) ->
            switch (method.getName()) {
              case "get" -> mockConnection();
              case "releaseConnection" -> true;
              case "toString" -> "light-database-connection-source";
              default -> returnEmpty(method);
            };

    return (DatabaseConnectionSource)
        Proxy.newProxyInstance(
            DatabaseConnectionSource.class.getClassLoader(),
            new Class<?>[] {DatabaseConnectionSource.class},
            handler);
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
}
