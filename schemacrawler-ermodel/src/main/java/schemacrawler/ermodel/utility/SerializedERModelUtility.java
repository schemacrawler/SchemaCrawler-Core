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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.utility.SerializedObjectInputStream;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public final class SerializedERModelUtility {

  private static final List<Pattern> ERMODEL_CLASS_PATTERNS =
      Arrays.asList(
          Pattern.compile("us\\.fatehi\\.utility\\.property\\.[A-Z].*"),
          Pattern.compile("us\\.fatehi\\.utility\\.database\\.[A-Z].*"),
          Pattern.compile("us\\.fatehi\\.utility\\.Multimap"),
          Pattern.compile("schemacrawler\\.(schema(crawler)?|crawl)\\.[A-Z].*"),
          Pattern.compile("schemacrawler\\.ermodel\\.model\\.[A-Z].*"),
          Pattern.compile("schemacrawler\\.ermodel\\.implementation\\.[A-Z].*"),
          Pattern.compile("schemacrawler\\.ermodel\\.associations\\.[A-Z].*"),
          Pattern.compile("schemacrawler\\.[A-Z].*"),
          Pattern.compile("(\\[L)?java\\.(lang|util)\\..*"),
          Pattern.compile("java\\.(sql|math|time|net)\\..*"),
          Pattern.compile("\\[[BC]"));

  public static ERModel readERModel(final InputStream in) {
    requireNonNull(in, "No input stream provided");
    return SerializedObjectInputStream.read(in, ERMODEL_CLASS_PATTERNS);
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
