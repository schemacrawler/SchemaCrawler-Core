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
import java.util.regex.Pattern;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.utility.SerializedObjectInputStream;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public final class SerializedERModelUtility {

  private static final Predicate<String> ERMODEL_CLASS_NAME =
      matching("us\\.fatehi\\.utility\\.CloudProvider")
          .or(matching("us\\.fatehi\\.utility\\.HostType"))
          .or(matching("us\\.fatehi\\.utility\\.property\\.[A-Z].*"))
          .or(matching("us\\.fatehi\\.utility\\.database\\.[A-Z].*"))
          .or(matching("us\\.fatehi\\.utility\\.Multimap"))
          .or(matching("schemacrawler\\.(schema(crawler)?|crawl)\\.[A-Z].*"))
          .or(matching("schemacrawler\\.namedobject\\.[A-Z].*"))
          .or(matching("schemacrawler\\.ermodel\\.model\\.[A-Z].*"))
          .or(matching("schemacrawler\\.ermodel\\.implementation\\.[A-Z].*"))
          .or(matching("schemacrawler\\.ermodel\\.associations\\.[A-Z].*"))
          .or(matching("schemacrawler\\.utility\\.[A-Z].*"))
          .or(matching("schemacrawler\\.[A-Z].*"))
          .or(matching("(\\[L)?java\\.(lang|util)\\..*"))
          .or(matching("java\\.(sql|math|time|net)\\..*"))
          .or(matching("\\[[BC]"));

  private static Predicate<String> matching(final String regex) {
    return Pattern.compile(regex).asMatchPredicate();
  }

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
