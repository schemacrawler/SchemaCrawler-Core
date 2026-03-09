/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.utility;

import static java.nio.file.Files.newInputStream;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import schemacrawler.schema.Catalog;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public final class SerializedCatalogUtility {

  private static final Logger LOGGER = Logger.getLogger(SerializedCatalogUtility.class.getName());

  private static final List<Pattern> CATALOG_CLASS_PATTERNS =
      List.of(
          Pattern.compile("us\\.fatehi\\.utility\\.property\\.[A-Z].*"),
          Pattern.compile("us\\.fatehi\\.utility\\.database\\.[A-Z].*"),
          Pattern.compile("schemacrawler\\.(schema(crawler)?|crawl)\\.[A-Z].*"),
          Pattern.compile("schemacrawler\\.[A-Z].*"),
          Pattern.compile("(\\[L)?java\\.(lang|util)\\..*"),
          Pattern.compile("java\\.(sql|math|time|net)\\..*"),
          Pattern.compile("\\[[BC]"));

  public static Catalog readCatalog(final InputStream in) {
    requireNonNull(in, "No input stream provided");
    return SerializedObjectInputStream.read(in, CATALOG_CLASS_PATTERNS);
  }

  public static void saveCatalog(final Catalog catalog, final OutputStream out) {
    requireNonNull(catalog, "No catalog provided");
    requireNonNull(out, "No output stream provided");
    SerializedObjectInputStream.save(catalog, out);
  }

  private SerializedCatalogUtility() {
    // Prevent instantiation
  }

  public static Catalog deserializeCatalog(final Path offlineDatabasePath) throws IOException {
    final Catalog catalog;
    try (final InputStream inputFileStream =
        new GZIPInputStream(newInputStream(offlineDatabasePath)); ) {
      catalog = readCatalog(inputFileStream);
      LOGGER.log(Level.INFO, () -> MetaDataUtility.summarizeCatalog(catalog));
    }
    return catalog;
  }
}
