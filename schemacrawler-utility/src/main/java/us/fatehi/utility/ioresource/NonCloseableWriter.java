/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.ioresource;

import static us.fatehi.utility.Utility.requireNotBlank;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

final class NonCloseableWriter extends BufferedWriter {

  private final String description;

  NonCloseableWriter(final Writer out, final String description) {
    super(out);
    this.description = requireNotBlank(description, "No writer description provided");
  }

  /** Flush but do not close. */
  @Override
  public void close() throws IOException {
    super.flush();
  }

  @Override
  public String toString() {
    return description;
  }
}
