/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.ioresource;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.IOUtility.locateResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

/**
 * Converts a provided classpath resource into an input resource. NOTE: Always assumes that
 * resources are absolute. Leading slashes are not required, but ignored if provided.
 */
public class ClasspathInputResource extends URLInputResource {

  private static URL locateClasspathResource(final String classpathResource) {
    requireNonNull(classpathResource, "No classpath resource provided");
    final URL url = locateResource(classpathResource);
    if (url == null) {
      final IOException e =
          new IOException("Cannot read classpath resource, <%s>".formatted(classpathResource));
      throw new UncheckedIOException(e);
    }
    return url;
  }

  public ClasspathInputResource(final String classpathResource) {
    super(locateClasspathResource(classpathResource));
  }
}
