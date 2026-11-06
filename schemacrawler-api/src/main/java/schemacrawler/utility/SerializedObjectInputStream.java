/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.utility;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import schemacrawler.schemacrawler.exceptions.ExecutionRuntimeException;
import us.fatehi.utility.string.StringFormat;

public final class SerializedObjectInputStream extends ObjectInputStream {

  private static final Logger LOGGER =
      Logger.getLogger(SerializedObjectInputStream.class.getName());

  private static final Predicate<String> DENIED_PACKAGE =
      className ->
          Stream.of(
                  "java.lang.reflect.",
                  "java.lang.invoke.",
                  "java.util.logging.",
                  "java.util.prefs.")
              .anyMatch(className::startsWith);

  private static final Predicate<String> DENIED_TYPE =
      Set.of(
              "java.lang.Class",
              "java.lang.System",
              "java.lang.ProcessBuilder",
              "java.lang.Process",
              "java.lang.Runtime",
              "java.lang.Thread",
              "java.lang.ThreadGroup",
              "java.lang.ClassLoader",
              "java.util.ServiceLoader")
          ::contains;

  private static final Predicate<String> DENIED_CLASS = DENIED_PACKAGE.or(DENIED_TYPE);

  static boolean isDenied(final String className) {
    return DENIED_CLASS.test(componentTypeName(className));
  }

  private static String componentTypeName(final String className) {
    int dimensions = 0;
    while (dimensions < className.length() && className.charAt(dimensions) == '[') {
      dimensions++;
    }
    if (dimensions == 0) {
      return className;
    }
    final boolean isReferenceArray =
        className.length() > dimensions + 1
            && className.charAt(dimensions) == 'L'
            && className.endsWith(";");
    return isReferenceArray
        ? className.substring(dimensions + 1, className.length() - 1)
        : className;
  }

  public static <C extends Serializable> C read(
      final InputStream in, final Predicate<String> allowedClassName) {
    requireNonNull(in, "No input stream provided");
    try (final SerializedObjectInputStream objIn =
        new SerializedObjectInputStream(in, allowedClassName)) {
      return (C) objIn.readObject();
    } catch (ClassNotFoundException | IOException e) {
      throw new ExecutionRuntimeException("Cannot deserialize object", e);
    }
  }

  public static <C extends Serializable> void save(final C object, final OutputStream out) {
    requireNonNull(object, "No object to serialize provided");
    requireNonNull(out, "No output stream provided");
    try (final ObjectOutputStream objOut = new ObjectOutputStream(out)) {
      objOut.writeObject(object);
    } catch (final IOException e) {
      throw new UncheckedIOException("Could not serialize object", e);
    }
  }

  private final Predicate<String> allowedClassName;

  private SerializedObjectInputStream(
      final InputStream input, final Predicate<String> allowedClassName) throws IOException {
    super(requireNonNull(input, "No input stream provided"));
    this.allowedClassName = requireNonNull(allowedClassName, "No allowed class predicate provided");
  }

  @Override
  protected Class<?> resolveClass(final ObjectStreamClass objectStreamClass)
      throws IOException, ClassNotFoundException {
    validateClassName(objectStreamClass.getName());
    return super.resolveClass(objectStreamClass);
  }

  private void validateClassName(final String className) throws InvalidClassException {
    if (!isDenied(className) && allowedClassName.test(className)) {
      LOGGER.log(Level.FINER, new StringFormat("Deserializing class <%s>", className));
      return;
    }
    throw new InvalidClassException("Not deserializing class <%s>".formatted(className));
  }
}
