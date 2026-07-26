/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.ioresource;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/** Converts a provided URL into an input resource. */
public class URLInputResource extends BaseInputResource {

  private final URL url;

  public URLInputResource(final URL url) {
    this.url = requireNonNull(url, "No URL provided");
  }

  @Override
  public InputStream openNewInputStream() throws IOException {
    final InputStream inputStream = url.openStream();
    return inputStream;
  }

  @Override
  public String toString() {
    return url.toExternalForm();
  }
}
