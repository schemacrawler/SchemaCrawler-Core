/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.utility;

import static java.util.Objects.requireNonNull;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Predicate;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.utility.SerializedObjectInputStream;
import us.fatehi.utility.Utility;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public final class SerializedERModelUtility {

  private static final Predicate<String> ERMODEL_CLASS_NAME =
      Utility.patternsToPredicate(
          "us\\.fatehi\\.utility\\.property\\.[A-Z].*",
          "us\\.fatehi\\.utility\\.database\\.[A-Z].*",
          "us\\.fatehi\\.utility\\.Multimap",
          "schemacrawler\\.(schema(crawler)?|crawl)\\.[A-Z].*",
          "schemacrawler\\.namedobject\\.[A-Z].*",
          "schemacrawler\\.ermodel\\.model\\.[A-Z].*",
          "schemacrawler\\.ermodel\\.implementation\\.[A-Z].*",
          "schemacrawler\\.ermodel\\.associations\\.[A-Z].*",
          "schemacrawler\\.utility\\.[A-Z].*",
          "schemacrawler\\.[A-Z].*");

  public static ERModel readERModel(final InputStream in) {
    requireNonNull(in, "No input stream provided");
    return SerializedObjectInputStream.read(in, ERMODEL_CLASS_NAME);
  }

  public static void saveERModel(final ERModel erModel, final OutputStream out) {
    requireNonNull(erModel, "No ER model provided");
    requireNonNull(out, "No output stream provided");
    SerializedObjectInputStream.save(erModel, out);
  }

  private SerializedERModelUtility() {
    // Prevent instantiation
  }
}
