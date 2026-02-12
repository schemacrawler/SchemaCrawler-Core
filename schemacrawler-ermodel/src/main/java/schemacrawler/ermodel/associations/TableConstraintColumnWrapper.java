package schemacrawler.ermodel.associations;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import schemacrawler.schema.Column;
import schemacrawler.schema.TableConstraint;
import schemacrawler.schema.TableConstraintColumn;
import schemacrawler.schemacrawler.exceptions.ExecutionRuntimeException;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
final class TableConstraintColumnWrapper {

  static final class TableConstraintColumnHandler implements InvocationHandler, Serializable {
    @Serial private static final long serialVersionUID = -5987628834605976612L;

    private final Column fkColumn;
    private final TableConstraint tableConstraint;

    private TableConstraintColumnHandler(
        final Column fkColumn, final TableConstraint tableConstraint) {
      this.fkColumn = requireNonNull(fkColumn, "No foreign key provided");
      this.tableConstraint = requireNonNull(tableConstraint, "No table constraint provided");
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
        throws Throwable {

      try {
        return switch (method.getName()) {
          case "getTableConstraint" -> tableConstraint;
          case "getTableConstraintOrdinalPosition" -> 1;
          default -> method.invoke(fkColumn, args);
        };
      } catch (final IllegalAccessException
          | IllegalArgumentException
          | InvocationTargetException e) {
        final Throwable cause = e.getCause();
        if (cause instanceof final Exception exception) {
          throw exception;
        }
        throw new ExecutionRuntimeException("Could not invoke " + method, e);
      }
    }
  }

  public static TableConstraintColumn createConstrainedColumn(
      final Column fkColumn, final TableConstraint tableConstraint) {
    final TableConstraintColumn tableConstraintColumn =
        (TableConstraintColumn)
            Proxy.newProxyInstance(
                TableConstraintColumn.class.getClassLoader(),
                new Class<?>[] {TableConstraintColumn.class},
                new TableConstraintColumnHandler(fkColumn, tableConstraint));
    return tableConstraintColumn;
  }

  private TableConstraintColumnWrapper() {
    // Prevent instantiation
  }
}
