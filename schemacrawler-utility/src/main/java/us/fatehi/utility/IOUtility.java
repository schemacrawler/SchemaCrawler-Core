/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isReadable;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.isWritable;
import static java.nio.file.Files.size;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;
import static us.fatehi.utility.Utility.isBlank;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import us.fatehi.utility.ioresource.ClasspathInputResource;
import us.fatehi.utility.ioresource.InputResource;

@UtilityMarker
public final class IOUtility {

  private static final Logger LOGGER = Logger.getLogger(IOUtility.class.getName());

  public static Path createTempFilePath(final String stem, final String extension)
      throws IOException {
    final String filename = "%s%s.%s".formatted(Utility.trimToEmpty(stem), randomUUID(), extension);
    final Path tempFilePath =
        createTempDirectory(null).resolve(filename).normalize().toAbsolutePath();
    tempFilePath.toFile().deleteOnExit();
    return tempFilePath;
  }

  public static String getFileExtension(final Path file) {
    if (file == null) {
      return "";
    }
    final String fileName = file.toString();
    return getFileExtension(fileName == null ? "" : fileName);
  }

  public static String getFileExtension(final String fileName) {
    final String ext;
    if (fileName != null) {
      ext =
          fileName.lastIndexOf('.') == -1 ? "" : fileName.substring(fileName.lastIndexOf('.') + 1);
    } else {
      ext = "";
    }
    return ext;
  }

  /**
   * Checks if an input file can be read. The file must contain some data.
   *
   * @param file Input file to read
   * @return True if the file can be read, false otherwise.
   */
  public static boolean isFileReadable(final Path file) {
    if (file == null || !isReadable(file) || !isRegularFile(file)) {
      return false;
    }
    try {
      if (size(file) == 0) {
        return false;
      }
    } catch (final IOException e) {
      // Not a critical check, so ignore exception
    }
    return true;
  }

  /**
   * Checks if an output file can be written. The file does not need to exist.
   *
   * @param file Output file to write
   * @return True if the file can be written, false otherwise.
   */
  public static boolean isFileWritable(final Path file) {
    if (file == null || isDirectory(file)) {
      return false;
    }
    final Path parentPath = file.getParent();
    return parentPath != null
        && exists(parentPath)
        && isDirectory(parentPath)
        && isWritable(parentPath);
  }

  /**
   * Returns true if dir refers to a directory that is outside the current working directory (i.e.
   * not the CWD itself and not any subdirectory of it).
   */
  public static boolean isOutsideWorkingDirectory(final Path dir) {
    final Path cwd = normalize(Paths.get(""));
    final Path absDir = normalize(dir);
    final boolean inside = absDir.equals(cwd) || absDir.startsWith(cwd);
    return !inside;
  }

  /**
   * Locates the resource based on the current thread's classloader. Always assumes that resources
   * are absolute. When running on the module path, this method tries multiple strategies to locate
   * the resource.
   *
   * @param classpathResource The classpath resource to locate
   * @return URL for the located resource, or null if not found
   */
  public static URL locateResource(final String classpathResource) {
    if (isBlank(classpathResource)) {
      return null;
    }
    final String resolvedClasspathResource;
    if (classpathResource.startsWith("/")) {
      resolvedClasspathResource = classpathResource.substring(1);
    } else {
      resolvedClasspathResource = classpathResource;
    }

    // Try context classloader first (works for classpath-based execution)
    final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    if (contextClassLoader != null) {
      final URL url = contextClassLoader.getResource(resolvedClasspathResource);
      if (url != null) {
        return url;
      }
    }

    // Try the classloader that loaded this class (works for module path)
    final ClassLoader classClassLoader = IOUtility.class.getClassLoader();
    if (classClassLoader != null) {
      final URL url = classClassLoader.getResource(resolvedClasspathResource);
      if (url != null) {
        return url;
      }
    }

    // Try using Class.getResource with absolute path (module-aware)
    return IOUtility.class.getResource("/" + resolvedClasspathResource);
  }

  /**
   * Reads the stream fully, and returns a byte array of data.
   *
   * @param reader Reader to read.
   * @return Byte array
   */
  public static String readFully(final Reader reader) {
    if (reader == null) {
      LOGGER.log(Level.FINE, "No reader provided");
      return "";
    }

    try (final StringWriter writer = new StringWriter()) {
      reader.transferTo(writer);
      return writer.toString();
    } catch (final IOException e) {
      LOGGER.log(Level.CONFIG, e.getMessage());
      LOGGER.log(Level.FINE, e.getMessage(), e);
      return "";
    }
  }

  public static String readResourceFully(final String resource) {
    try {
      final InputResource inputResource = new ClasspathInputResource(resource);
      return readFully(inputResource.openNewInputReader(UTF_8));
    } catch (final Exception e) {
      LOGGER.log(Level.CONFIG, e.getMessage());
      LOGGER.log(Level.FINE, e.getMessage(), e);
      return "";
    }
  }

  /**
   * Resolves a candidate output path under a trusted parent directory and blocks path traversal.
   *
   * <p>This method is used to enforce a write boundary. If the resolved path escapes {@code
   * parentPath} (for example via {@code ../}), it throws {@link UncheckedIOException} immediately
   * because this is a security violation, not a recoverable formatting issue.
   *
   * @param parentPath Trusted parent directory for output files
   * @param filename Candidate file path relative to {@code parentPath}
   * @return Absolute normalized path, guaranteed to remain under {@code parentPath}
   * @throws IllegalArgumentException If the parent path or filename is invalid
   * @throws UncheckedIOException If the resolved path escapes {@code parentPath}
   */
  public static Path sanitizeFilePath(final Path parentPath, final String filename) {
    requireNonNull(parentPath, "No parent path provided");
    if (isBlank(filename)) {
      throw new IllegalArgumentException("Bad filename <%s>".formatted(filename));
    }
    if (isOutsideWorkingDirectory(parentPath)) {
      LOGGER.log(
          Level.SEVERE,
          "Attempt to write outside current working directory to path <%s>".formatted(parentPath));
    }

    final Path absoluteParentPath = normalize(parentPath);
    final Path filePath = normalize(parentPath.resolve(filename));
    if (!filePath.startsWith(absoluteParentPath)) {
      throw new UncheckedIOException(
          new IOException("Resolved output path escapes parent <%s>".formatted(filePath)));
    }

    return filePath;
  }

  private static Path normalize(final Path parentPath) {
    return parentPath.toAbsolutePath().normalize();
  }

  private IOUtility() {
    // Prevent instantiation
  }
}
