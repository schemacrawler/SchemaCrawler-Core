/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.associations;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.Locale;
import java.util.regex.Pattern;
import schemacrawler.schema.Column;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public class ImplicitAssociationsUtility {

  public static final Pattern ID_PATTERN = Pattern.compile("_?(id|key|keyid)$", CASE_INSENSITIVE);
  private static final Pattern NOT_ALPHANUMERIC_PATTERN = Pattern.compile("[^\\p{L}\\d]");

  public static String normalizeColumnName(final Column column) {
    if (column == null) {
      return "";
    }
    final String columnName =
        NOT_ALPHANUMERIC_PATTERN.matcher(column.getName()).replaceAll("").toLowerCase(Locale.ROOT);
    return columnName;
  }

  public static String removeId(final Column column) {
    if (column == null) {
      return "";
    }
    final String columnName =
        ImplicitAssociationsUtility.ID_PATTERN
            .matcher(column.getName())
            .replaceFirst("")
            .toLowerCase(Locale.ROOT);
    return columnName;
  }

  private ImplicitAssociationsUtility() {
    // Prevent instantiation
  }
}
