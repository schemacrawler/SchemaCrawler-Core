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
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import schemacrawler.schema.Catalog;
import us.fatehi.utility.Utility;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public final class SerializedCatalogUtility {

  private static final Logger LOGGER = Logger.getLogger(SerializedCatalogUtility.class.getName());

  private static final Predicate<String> CATALOG_CLASS_NAME =
      Utility.patternsToPredicate(
          "us\\.fatehi\\.utility\\.[A-Z].*",
          "us\\.fatehi\\.utility\\.property\\.[A-Z].*",
          "us\\.fatehi\\.utility\\.database\\.[A-Z].*",
          "us\\.fatehi\\.utility\\.jdbc\\.driver\\.[A-Z].*",
          "us\\.fatehi\\.utility\\.jdbc\\.serverfingerprint\\.[A-Z].*",
          "schemacrawler\\.(schema(crawler)?|crawl)\\.[A-Z].*",
          "schemacrawler\\.filter\\.CatalogProjection",
          "schemacrawler\\.namedobject\\.[A-Z].*",
          "schemacrawler\\.utility\\.[A-Z].*",
          "schemacrawler\\.[A-Z].*");

  public static Catalog deserializeCatalog(final Path offlineDatabasePath) throws IOException {
    final Catalog catalog;
    try (final InputStream inputFileStream =
        new GZIPInputStream(newInputStream(offlineDatabasePath)); ) {
      catalog = readCatalog(inputFileStream);
      LOGGER.log(Level.INFO, () -> "Deserialized catalog from <%s>".formatted(offlineDatabasePath));
    }
    return catalog;
  }

  public static Catalog readCatalog(final InputStream in) {
    requireNonNull(in, "No input stream provided");
    return SerializedObjectInputStream.read(in, CATALOG_CLASS_NAME);
  }

  public static void saveCatalog(final Catalog catalog, final OutputStream out) {
    requireNonNull(catalog, "No catalog provided");
    requireNonNull(out, "No output stream provided");
    SerializedObjectInputStream.save(catalog, out);
  }

  private SerializedCatalogUtility() {
    // Prevent instantiation
  }
}
