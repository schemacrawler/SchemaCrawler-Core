/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.datasource;

import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PooledConnectionUtility {

  private static class PooledConnectionInvocationHandler implements InvocationHandler {

    @FunctionalInterface
    private interface MethodHandler {
      Object handle(Object proxy, Object[] args) throws Exception;
    }

    private static final Set<String> CLOSED_EXEMPT_METHODS = Set.of("isClosed", "unwrap");

    private final Connection connection;
    private final DatabaseConnectionSource connectionSource;
    private volatile boolean isClosed;
    private final Map<String, MethodHandler> handlers;

    PooledConnectionInvocationHandler(
        final Connection connection, final DatabaseConnectionSource connectionSource) {
      requireNonNull(connection, "No database connnection provided");
      if (connection instanceof DatabaseConnectionSourceConnection) {
        try {
          this.connection = connection.unwrap(Connection.class);
        } catch (final SQLException e) {
          throw new UnsupportedOperationException("Could not unwrap proxy connection");
        }
      } else {
        this.connection = connection;
      }
      this.connectionSource =
          requireNonNull(connectionSource, "No database connection source provided");
      isClosed = false;
      this.handlers = buildHandlers();
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
        throws Exception {
      final String methodName = method.getName();
      if (!CLOSED_EXEMPT_METHODS.contains(methodName) && isClosed) {
        throw new SQLException("Cannot call <%s> since connection is closed".formatted(method));
      }
      final MethodHandler handler = handlers.get(methodName);
      if (handler != null) {
        return handler.handle(proxy, args);
      }
      return delegateToConnection(method, args);
    }

    private Map<String, MethodHandler> buildHandlers() {
      final Map<String, MethodHandler> map = new HashMap<>();
      map.put("close", (proxy, args) -> handleClose());
      map.put("isClosed", (proxy, args) -> isClosed);
      map.put("isWrapperFor", (proxy, args) -> handleIsWrapperFor(args));
      map.put("unwrap", (proxy, args) -> connection);
      map.put(
          "toString",
          (proxy, args) ->
              "Pooled connection <%s@%d> for <%s>"
                  .formatted(proxy.getClass().getName(), proxy.hashCode(), connection));
      return map;
    }

    private synchronized Object handleClose() {
      if (!isClosed) {
        connectionSource.releaseConnection(connection);
      }
      isClosed = true;
      return null;
    }

    private Object handleIsWrapperFor(final Object[] args) {
      final Class<?> clazz = (Class<?>) args[0];
      return clazz.isAssignableFrom(connection.getClass());
    }

    private Object delegateToConnection(final Method method, final Object[] args) throws Exception {
      try {
        if (isClosed) {
          throw new IllegalAccessException("Connection is closed");
        }
        return method.invoke(connection, args);
      } catch (final IllegalAccessException
          | IllegalArgumentException
          | InvocationTargetException e) {
        final Throwable cause = e.getCause();
        if (cause instanceof final Exception exception) {
          throw exception;
        }
        throw new SQLException("Could not delegate method <%s>".formatted(method), e);
      }
    }
  }

  public static Connection newPooledConnection(
      final Connection connection, final DatabaseConnectionSource connectionSource) {

    return (Connection)
        newProxyInstance(
            PooledConnectionUtility.class.getClassLoader(),
            new Class[] {Connection.class, DatabaseConnectionSourceConnection.class},
            new PooledConnectionInvocationHandler(connection, connectionSource));
  }

  private PooledConnectionUtility() {
    // Prevent instantiation
  }
}
