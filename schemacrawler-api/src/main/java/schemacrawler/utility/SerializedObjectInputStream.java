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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import schemacrawler.schemacrawler.exceptions.ExecutionRuntimeException;
import schemacrawler.schemacrawler.exceptions.IORuntimeException;
import us.fatehi.utility.string.StringFormat;

public final class SerializedObjectInputStream extends ObjectInputStream {

  private static final Logger LOGGER =
      Logger.getLogger(SerializedObjectInputStream.class.getName());

  public static <C extends Serializable> C read(
      final InputStream in, final List<Pattern> acceptClassPatterns) {
    requireNonNull(in, "No input stream provided");
    try (final SerializedObjectInputStream objIn =
        new SerializedObjectInputStream(in, acceptClassPatterns)) {
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
      throw new IORuntimeException("Could not serialize object", e);
    }
  }

  private final List<Pattern> acceptClassPatterns;

  private SerializedObjectInputStream(
      final InputStream input, final List<Pattern> acceptClassPatterns) throws IOException {
    super(requireNonNull(input, "No input stream provided"));
    if (acceptClassPatterns == null || acceptClassPatterns.isEmpty()) {
      throw new IllegalArgumentException("No accept class patterns provided");
    }
    this.acceptClassPatterns = List.copyOf(acceptClassPatterns);
  }

  @Override
  protected Class<?> resolveClass(final ObjectStreamClass objectStreamClass)
      throws IOException, ClassNotFoundException {
    validateClassName(objectStreamClass.getName());
    return super.resolveClass(objectStreamClass);
  }

  private void validateClassName(final String className) throws InvalidClassException {
    for (final Pattern pattern : acceptClassPatterns) {
      if (pattern.matcher(className).matches()) {
        LOGGER.log(Level.FINER, new StringFormat("Deserializing class <%s>", className));
        return;
      }
    }
    throw new InvalidClassException("Not deserializing class <%s>".formatted(className));
  }
}
