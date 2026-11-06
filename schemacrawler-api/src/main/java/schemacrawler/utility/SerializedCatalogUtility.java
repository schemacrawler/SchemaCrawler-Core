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
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import schemacrawler.schema.Catalog;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public final class SerializedCatalogUtility {

  private static final Logger LOGGER = Logger.getLogger(SerializedCatalogUtility.class.getName());

  private static final Predicate<String> CATALOG_CLASS_NAME =
      matching("us\\.fatehi\\.utility\\.[A-Z].*")
          .or(matching("us\\.fatehi\\.utility\\.property\\.[A-Z].*"))
          .or(matching("us\\.fatehi\\.utility\\.database\\.[A-Z].*"))
          .or(matching("us\\.fatehi\\.utility\\.jdbc\\.driver\\.[A-Z].*"))
          .or(matching("us\\.fatehi\\.utility\\.jdbc\\.serverfingerprint\\.[A-Z].*"))
          .or(matching("schemacrawler\\.(schema(crawler)?|crawl)\\.[A-Z].*"))
          .or(matching("schemacrawler\\.filter\\.CatalogProjection"))
          .or(matching("schemacrawler\\.namedobject\\.[A-Z].*"))
          .or(matching("schemacrawler\\.utility\\.[A-Z].*"))
          .or(matching("schemacrawler\\.[A-Z].*"))
          .or(matching("(\\[L)?java\\.(lang|util)\\..*"))
          .or(matching("java\\.(sql|math|time|net)\\..*"))
          .or(matching("\\[[BC]"));

  private static Predicate<String> matching(final String regex) {
    return Pattern.compile(regex).asMatchPredicate();
  }

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
