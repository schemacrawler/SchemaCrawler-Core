/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import static us.fatehi.test.utility.TestObjectUtility.returnEmpty;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.DriverManager;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.CrawlInfo;
import schemacrawler.schema.DatabaseInfo;
import schemacrawler.schema.JdbcDriverInfo;
import us.fatehi.test.utility.TestObjectUtility;
import us.fatehi.utility.UtilityMarker;
import us.fatehi.utility.datasource.DatabaseConnectionSource;

@UtilityMarker
public class LightCatalogUtility {

  public static DatabaseInfo lightDatabaseInfo() {
    final Class<DatabaseInfo> clazz = DatabaseInfo.class;
    final InvocationHandler handler =
        (proxy, method, args) -> {
          final String methodName = method.getName();

          return switch (methodName) {
            case "getDatabaseProductName", "getDatabaseProductVersion" -> "fake";
            default -> returnEmpty(method);
          };
        };

    return (DatabaseInfo)
        Proxy.newProxyInstance(
            LightCatalogUtility.class.getClassLoader(), new Class<?>[] {clazz}, handler);
  }

  public static Catalog lightCatalog() {
    final InvocationHandler handler =
        (proxy, method, args) -> {
          final String methodName = method.getName();

          return switch (methodName) {
            case "getName", "getFullName", "toString" -> "empty-catalog";
            case "equals" -> proxy == args[0];
            case "hashCode" -> System.identityHashCode(proxy);
            case "getCrawlInfo" -> TestObjectUtility.makeTestObject(CrawlInfo.class);
            case "getJdbcDriverInfo" -> TestObjectUtility.makeTestObject(JdbcDriverInfo.class);
            case "getDatabaseInfo" -> lightDatabaseInfo();
            default -> returnEmpty(method);
          };
        };

    return (Catalog)
        Proxy.newProxyInstance(
            Catalog.class.getClassLoader(), new Class<?>[] {Catalog.class}, handler);
  }

  public static DatabaseConnectionSource lightDatabaseConnectionSource() {

    final InvocationHandler handler =
        (proxy, method, args) ->
            (switch (method.getName()) {
              case "get" -> DriverManager.getConnection("jdbc:hsqldb:mem:testdb");
              case "releaseConnection" -> true;
              case "toString" -> "empty-data-source";
              default -> returnEmpty(method);
            });

    return (DatabaseConnectionSource)
        Proxy.newProxyInstance(
            DatabaseConnectionSource.class.getClassLoader(),
            new Class<?>[] {DatabaseConnectionSource.class},
            handler);
  }
}
