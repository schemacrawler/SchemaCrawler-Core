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
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schemacrawler.exceptions.ExecutionRuntimeException;
import us.fatehi.utility.string.StringFormat;

public final class SerializedObjectInputStream extends ObjectInputStream {

  private static final Logger LOGGER =
      Logger.getLogger(SerializedObjectInputStream.class.getName());

  public static <C extends Serializable> C read(
      final InputStream in, final Predicate<String> allowedClassName) {
    requireNonNull(in, "No input stream provided");
    requireNonNull(allowedClassName, "No allowed class predicate provided");

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

    requireNonNull(allowedClassName, "No allowed class predicate provided");
    this.allowedClassName = Predicate.not(new DeniedClassesFilter()).and(allowedClassName);
  }

  @Override
  protected Class<?> resolveClass(final ObjectStreamClass objectStreamClass)
      throws IOException, ClassNotFoundException {
    validateClassName(objectStreamClass.getName());
    return super.resolveClass(objectStreamClass);
  }

  private void validateClassName(final String className) throws InvalidClassException {
    if (allowedClassName.test(className)) {
      LOGGER.log(Level.FINER, new StringFormat("Deserializing class <%s>", className));
      return;
    }
    throw new InvalidClassException("Not deserializing class <%s>".formatted(className));
  }
}
