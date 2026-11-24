/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.associations;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static us.fatehi.utility.Utility.isBlank;
import static us.fatehi.utility.Utility.toSnakeCase;

import java.util.Locale;
import java.util.regex.Pattern;
import schemacrawler.schema.Column;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public class ImplicitAssociationsUtility {

  public static final Pattern ID_PATTERN = Pattern.compile("_?(id|key|keyid)$", CASE_INSENSITIVE);
  private static final Pattern NOT_ALPHANUMERIC_PATTERN = Pattern.compile("[^\\p{L}\\d]");

  /**
   * Normalizes a table or column name to the common form used for match keys, so that inference
   * does not depend on naming style. camelCase, PascalCase and acronym boundaries, spaces, hyphens
   * and underscores all become single underscores between words, and all letters are lowercase. For
   * example, {@code CustomerOrder}, {@code customerOrder}, {@code Customer Order} and {@code
   * CUSTOMER_ORDER} all become {@code customer_order}.
   *
   * <p>Case boundaries are detected only between ASCII letters and digits. Boundaries between
   * non-ASCII letters are not split, so such names keep their words glued together.
   *
   * @param name Table or column name, may be null
   * @return Normalized name, or an empty string if the name is null or blank
   */
  public static String normalizeMatchKeyName(final String name) {
    if (isBlank(name)) {
      return "";
    }
    return toSnakeCase(name);
  }

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
