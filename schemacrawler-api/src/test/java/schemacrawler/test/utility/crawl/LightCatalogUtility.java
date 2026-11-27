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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.DatabaseInfo;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.JdbcDriverInfo;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
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
    return lightCatalog(List.of(), List.of(), List.of(), List.of());
  }

  /**
   * Makes a catalog that holds the given objects, and nothing else. The schemas of the catalog are
   * the distinct schemas of the objects, in the order first seen, and the lookups by schema return
   * the objects of that schema. Methods that are not covered return empty values.
   */
  public static Catalog lightCatalog(
      final Collection<Table> tables,
      final Collection<Routine> routines,
      final Collection<Sequence> sequences,
      final Collection<Synonym> synonyms) {
    final List<Table> tablesList = copyOf(tables);
    final List<Routine> routinesList = copyOf(routines);
    final List<Sequence> sequencesList = copyOf(sequences);
    final List<Synonym> synonymsList = copyOf(synonyms);
    final List<Schema> schemasList =
        schemasOf(tablesList, routinesList, sequencesList, synonymsList);

    final InvocationHandler handler =
        (proxy, method, args) -> {
          final String methodName = method.getName();

          return switch (methodName) {
            case "key" -> new NamedObjectKey("light-catalog");
            case "getName", "getFullName", "toString" -> "light-catalog";
            case "equals" -> proxy == args[0];
            case "hashCode" -> System.identityHashCode(proxy);
            case "getCrawlInfo" -> new LightCrawlInfo();
            case "getJdbcDriverInfo" -> TestObjectUtility.makeTestObject(JdbcDriverInfo.class);
            case "getDatabaseInfo" -> lightDatabaseInfo();
            case "getSchemas" -> schemasList;
            case "hasSchemas" -> !schemasList.isEmpty();
            case "getTables" -> inSchema(tablesList, args);
            case "hasTables" -> !inSchema(tablesList, args).isEmpty();
            case "getRoutines" -> inSchema(routinesList, args);
            case "hasRoutines" -> !inSchema(routinesList, args).isEmpty();
            case "getSequences" -> inSchema(sequencesList, args);
            case "hasSequences" -> !inSchema(sequencesList, args).isEmpty();
            case "getSynonyms" -> inSchema(synonymsList, args);
            case "hasSynonyms" -> !inSchema(synonymsList, args).isEmpty();
            default -> returnEmpty(method);
          };
        };

    return (Catalog)
        Proxy.newProxyInstance(
            Catalog.class.getClassLoader(), new Class<?>[] {Catalog.class}, handler);
  }

  public static Catalog lightCatalog(final Table... tables) {
    return lightCatalog(
        tables == null ? List.of() : List.of(tables), List.of(), List.of(), List.of());
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
            case "getProductName":
            case "getName":
              return productName;
            case "getDatabaseProductVersion":
            case "getProductVersion":
            case "getVaue":
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

  private static <T> List<T> copyOf(final Collection<T> items) {
    return items == null ? List.of() : List.copyOf(items);
  }

  /**
   * Selects the objects for a call of a lookup method. With no arguments, all objects are returned.
   * With a schema, the objects of that schema are returned, and with a schema and a name, those of
   * the schema that have the name.
   */
  private static <T extends DatabaseObject> List<T> inSchema(
      final List<T> items, final Object[] args) {
    if (args == null || args.length == 0) {
      return items;
    }
    final Schema schema = (Schema) args[0];
    final String name = args.length > 1 ? (String) args[1] : null;
    final List<T> selected = new ArrayList<>();
    for (final T item : items) {
      final boolean isInSchema =
          item.getSchema() != null
              && schema != null
              && Objects.equals(item.getSchema().key(), schema.key());
      if (isInSchema && (name == null || name.equals(item.getName()))) {
        selected.add(item);
      }
    }
    return selected;
  }

  /** The distinct schemas of the objects, in the order in which they are first seen. */
  @SafeVarargs
  private static List<Schema> schemasOf(final List<? extends DatabaseObject>... groups) {
    final Map<NamedObjectKey, Schema> schemas = new LinkedHashMap<>();
    for (final List<? extends DatabaseObject> group : groups) {
      for (final DatabaseObject item : group) {
        final Schema schema = item.getSchema();
        if (schema != null) {
          schemas.putIfAbsent(schema.key(), schema);
        }
      }
    }
    return List.copyOf(schemas.values());
  }
}
