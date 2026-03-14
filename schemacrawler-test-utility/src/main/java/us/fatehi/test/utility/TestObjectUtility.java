/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.test.utility;

import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.AccessMode;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import tools.jackson.databind.ObjectMapper;

public class TestObjectUtility {

  public record Results(String resultSetDescription, String[] columnNames, Object[][] data) {
    public Results {
      resultSetDescription =
          requireNonNull(resultSetDescription, "No result set description provided");
      columnNames = requireNonNull(columnNames, "No column names provided");
    }
  }

  private static final class ResultSetInvocationHandler implements InvocationHandler {

    private final Results results;
    private int rowIndex;
    private boolean wasNull;
    private final ResultSetMetaData rsmd;

    private ResultSetInvocationHandler(final Results results) throws SQLException {
      this.results = requireNonNull(results);
      rsmd = createResultSetMetaData();
      rowIndex = -1;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
        throws Throwable {
      // Result set
      final String methodName = method.getName();
      switch (methodName) {
        case "close":
          return null;
        case "getMetaData":
          return rsmd;
        case "next":
          wasNull = false;
          if (results.data == null) {
            return false;
          }
          rowIndex = rowIndex + 1;
          return rowIndex < results.data.length;
        case "getColumnName":
        case "getColumnLabel":
          if (args[0] instanceof final Integer index) {
            return results.columnNames[index];
          }
          return "columnName";
        case "getCatalogName":
          return "catalogName";
        case "getSchemaName":
          return "schemaName";
        case "getTableName":
          return "tableName";
        case "getObject":
        case "getString":
        case "getInt":
        case "getShort":
          wasNull = false;
          int index = -1;
          if (args[0] instanceof final Integer integer) {
            index = integer - 1;
          }
          if (args[0] instanceof final String columnName) {
            index = List.of(results.columnNames).indexOf(columnName);
          }
          Object columnData;
          if (results.data == null || rowIndex < 0 || index < 0) {
            columnData = null;
          } else {
            columnData = results.data[rowIndex][index];
          }
          if (columnData == null) {
            wasNull = true;
            if ("getInt".equals(methodName) || "getShort".equals(methodName)) {
              throw new SQLException("Cannot convert <null> to an integer".formatted());
            }
            return null;
          }
          if (methodName != null) {
            switch (methodName) {
              case "getObject":
                return columnData;
              case "getString":
                return String.valueOf(columnData);
              case "getInt":
                return ((Number) columnData).intValue();
              case "getShort":
                return ((Number) columnData).shortValue();
              default:
                break;
            }
          }
        case "setFetchSize":
          return null;
        case "wasNull":
          return wasNull;
        case "toString":
          return "ResultSet: " + results.resultSetDescription;
        default:
          fail("%s(%s)".formatted(method, args));
          return null;
      }
    }

    private ResultSetMetaData createResultSetMetaData() throws SQLException {
      final ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
      lenient().when(rsmd.getColumnCount()).thenReturn(results.columnNames.length);
      for (int i = 0; i < results.columnNames.length; i++) {
        lenient().when(rsmd.getColumnName(i + 1)).thenReturn(results.columnNames[i]);
        lenient().when(rsmd.getColumnLabel(i + 1)).thenReturn(results.columnNames[i]);
      }
      lenient()
          .when(rsmd.toString())
          .thenReturn("ResultSetMetaData: " + results.resultSetDescription);
      return rsmd;
    }
  }

  public static Map<String, Object> fakeObjectMapFor(final Class<?> clazz) {
    final Map<String, Object> fakeObjectMap = new HashMap<>();
    fakeObjectMap.put("@object", clazz.getName());
    return fakeObjectMap;
  }

  public static TestObject makeTestObject() {
    final TestObject testObject1 = new TestObject();
    testObject1.setPlainString("hello world");
    testObject1.setPrimitiveInt(99);
    testObject1.setPrimitiveDouble(99.99);
    testObject1.setPrimitiveBoolean(true);
    testObject1.setPrimitiveArray(new int[] {1, 1, 2, 3, 5, 8});
    testObject1.setPrimitiveEnum(AccessMode.READ);
    testObject1.setObjectArray(new String[] {"a", "b", "c"});
    testObject1.setIntegerList(List.of(1, 1, 2, 3, 5, 8));
    final HashMap<Integer, String> map = new HashMap<>();
    map.put(1, "a");
    map.put(2, "b");
    map.put(3, "c");
    testObject1.setMap(map);
    final TestObject testObject = testObject1;
    return testObject;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Object> T makeTestObject(final Class<T> clazz) {
    final InvocationHandler handler =
        (proxy, method, args) -> {
          final String methodName = method.getName();

          return switch (methodName) {
            case "equals" -> proxy == args[0];
            case "hashCode" -> System.identityHashCode(proxy);
            default -> returnEmpty(method);
          };
        };

    return (T)
        Proxy.newProxyInstance(
            TestObjectUtility.class.getClassLoader(), new Class<?>[] {clazz}, handler);
  }

  public static Map<String, Object> makeTestObjectMap() {

    final TestObject testObject = makeTestObject();

    final ObjectMapper mapper = new ObjectMapper();

    final Map<String, Object> testObjectMap =
        new TreeMap<>(mapper.convertValue(testObject, Map.class));
    testObjectMap.put("@object", testObject.getClass().getName());

    return testObjectMap;
  }

  public static Connection mockConnection() {
    return mockConnection(new Results("MOCKED RESULTS", new String[] {"col1"}, null));
  }

  public static Connection mockConnection(final Results results) {
    try {
      final DatabaseMetaData mockDbMetaData = mockDatabaseMetaData();

      final Connection mockConnection = mock(Connection.class);
      lenient().when(mockConnection.toString()).thenReturn("Connection: Mocked Connection");
      lenient().when(mockConnection.getMetaData()).thenReturn(mockDbMetaData);
      lenient().when(mockConnection.isClosed()).thenReturn(false);
      lenient().when(mockConnection.isValid(anyInt())).thenReturn(true);

      final Statement mockStatement = mockStatement(results);
      lenient().when(mockConnection.createStatement()).thenReturn(mockStatement);

      return mockConnection;
    } catch (final SQLException e) {
      return mock(Connection.class);
    }
  }

  public static DatabaseMetaData mockDatabaseMetaData() {
    try {
      final DatabaseMetaData mockDbMetaData = mock(DatabaseMetaData.class);
      lenient().when(mockDbMetaData.toString()).thenReturn("DatabaseMetaData: Mocked Database");
      lenient().when(mockDbMetaData.getDatabaseProductName()).thenReturn("Mocked Database");
      lenient().when(mockDbMetaData.getDatabaseProductVersion()).thenReturn("0.0.1");
      lenient().when(mockDbMetaData.getURL()).thenReturn("jdbc:mocked://mockedconnection");
      lenient().when(mockDbMetaData.getDriverName()).thenReturn("Mocked Driver");
      lenient().when(mockDbMetaData.getDriverVersion()).thenReturn("0.0.1-ALPHA");
      lenient().when(mockDbMetaData.supportsCatalogsInTableDefinitions()).thenReturn(false);
      lenient().when(mockDbMetaData.supportsSchemasInTableDefinitions()).thenReturn(true);
      return mockDbMetaData;
    } catch (final SQLException e) {
      return mock(DatabaseMetaData.class);
    }
  }

  public static ResultSet mockResultSet(final Results results) throws SQLException {
    return (ResultSet)
        newProxyInstance(
            ResultSet.class.getClassLoader(),
            new Class[] {ResultSet.class},
            new ResultSetInvocationHandler(results));
  }

  public static Statement mockStatement() {
    return mockStatement(new Results("MOCKED RESULTS", new String[] {"col1"}, null));
  }

  public static Statement mockStatement(final Results results) {
    try {
      final ResultSet mockResultSet = mockResultSet(results);
      final Statement mockStatement = mock(Statement.class);
      lenient().when(mockStatement.execute(anyString())).thenReturn(true);
      lenient().when(mockStatement.getResultSet()).thenReturn(mockResultSet);
      lenient().when(mockStatement.getUpdateCount()).thenReturn(0);
      lenient().when(mockStatement.execute(anyString())).thenReturn(true);
      return mockStatement;
    } catch (final SQLException e) {
      return mock(Statement.class);
    }
  }

  public static Object returnEmpty(final Method method) {
    if (method == null) {
      return null;
    }

    final Class<?> returnType = method.getReturnType();

    if (returnType == Void.TYPE) {
      return null;
    }
    if (Optional.class.isAssignableFrom(returnType)) {
      return Optional.empty();
    }
    if (Collection.class.isAssignableFrom(returnType)) {
      return List.of();
    }

    // Handle primitives
    if (returnType == boolean.class) {
      return false;
    }
    if (returnType == int.class) {
      return 0;
    }
    if (returnType == long.class) {
      return 0L;
    }
    if (returnType == double.class) {
      return 0.0d;
    }
    if (returnType == float.class) {
      return 0.0f;
    }
    if (returnType == byte.class) {
      return (byte) 0;
    }
    if (returnType == short.class) {
      return (short) 0;
    }
    if (returnType == char.class) {
      return '\0';
    }

    // Handle primitive wrappers
    if (returnType == Boolean.class) {
      return Boolean.FALSE;
    }
    if (returnType == Integer.class) {
      return Integer.valueOf(0);
    }
    if (returnType == Long.class) {
      return Long.valueOf(0L);
    }
    if (returnType == Double.class) {
      return Double.valueOf(0.0d);
    }
    if (returnType == Float.class) {
      return Float.valueOf(0.0f);
    }
    if (returnType == Byte.class) {
      return Byte.valueOf((byte) 0);
    }
    if (returnType == Short.class) {
      return Short.valueOf((short) 0);
    }
    if (returnType == Character.class) {
      return Character.valueOf('\0');
    }

    throw new UnsupportedOperationException(method.toString());
  }

  private TestObjectUtility() {
    // Prevent instantiation
  }
}
