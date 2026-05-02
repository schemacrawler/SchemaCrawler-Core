/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.offline.jdbc;

import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Map;
import schemacrawler.schemacrawler.exceptions.IORuntimeException;
import us.fatehi.utility.IOUtility;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public class OfflineConnectionUtility {

  private static class OfflineConnectionInvocationHandler implements InvocationHandler {

    @FunctionalInterface
    private interface MethodHandler {
      Object handle(Object proxy, Object[] args) throws SQLFeatureNotSupportedException;
    }

    private final Path offlineDatabasePath;
    private boolean isClosed;
    private final Map<String, MethodHandler> handlers;

    public OfflineConnectionInvocationHandler(final Path offlineDatabasePath) {
      this.offlineDatabasePath = offlineDatabasePath;
      handlers = buildHandlers();
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
        throws SQLFeatureNotSupportedException {
      final MethodHandler handler = handlers.get(method.getName());
      if (handler != null) {
        return handler.handle(proxy, args);
      }
      throw new SQLFeatureNotSupportedException(
          "Offline catalog snapshot connection does not support <%s>".formatted(method), "HYC00");
    }

    private Map<String, MethodHandler> buildHandlers() {
      final Map<String, MethodHandler> map = new HashMap<>();
      map.put(
          "close",
          (proxy, args) -> {
            isClosed = true;
            return null;
          });
      map.put("setAutoCommit", (proxy, args) -> null);
      map.put("isClosed", (proxy, args) -> isClosed);
      map.put("isValid", (proxy, args) -> !isClosed);
      map.put("isWrapperFor", (proxy, args) -> handleIsWrapperFor(args));
      map.put("unwrap", (proxy, args) -> proxy);
      map.put("getTypeMap", (proxy, args) -> Map.of());
      map.put("getOfflineDatabasePath", (proxy, args) -> offlineDatabasePath);
      map.put("hashCode", (proxy, args) -> offlineDatabasePath.hashCode());
      map.put(
          "toString",
          (proxy, args) ->
              "%s@%d".formatted(OfflineConnection.class.getName(), offlineDatabasePath.hashCode()));
      map.put("equals", (proxy, args) -> handleEquals(args));
      return Map.copyOf(map);
    }

    private Object handleEquals(final Object[] args) throws SQLFeatureNotSupportedException {
      if (args != null
          && args.length > 0
          && args[0] instanceof final OfflineConnection otherOfflineConnection) {
        return otherOfflineConnection.hashCode() == offlineDatabasePath.hashCode();
      }
      throw new SQLFeatureNotSupportedException(
          "Offline catalog snapshot connection does not support <equals>", "HYC00");
    }

    private Object handleIsWrapperFor(final Object[] args) {
      if (args[0] == null) {
        return false;
      }
      final Class<?> clazz = (Class<?>) args[0];
      return clazz.isAssignableFrom(Connection.class);
    }
  }

  public static OfflineConnection newOfflineConnection(final Path offlineDatabasePath) {
    requireNonNull(offlineDatabasePath, "No offline catalog snapshot path provided");

    final Path absoluteOfflineDatabasePath = offlineDatabasePath.toAbsolutePath();
    if (!IOUtility.isFileReadable(absoluteOfflineDatabasePath)) {
      throw new IORuntimeException(
          "Cannot read offline database <%s>".formatted(absoluteOfflineDatabasePath));
    }
    final OfflineConnection offlineConnection =
        (OfflineConnection)
            newProxyInstance(
                OfflineConnectionUtility.class.getClassLoader(),
                new Class[] {OfflineConnection.class},
                new OfflineConnectionInvocationHandler(absoluteOfflineDatabasePath));
    return offlineConnection;
  }

  private OfflineConnectionUtility() {
    // Prevent instantiation
  }
}
