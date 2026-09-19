/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.requireNotBlank;
import static us.fatehi.utility.database.DatabaseUtility.normalizeDatabaseObjectName;

import java.util.function.Function;
import java.util.regex.Pattern;
import schemacrawler.inclusionrule.InclusionRule;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Routine;
import schemacrawler.schema.RoutineType;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.GrepOptions;
import us.fatehi.utility.UtilityMarker;

/** Factory methods for filters over named objects. */
@UtilityMarker
public final class NamedObjectFilters {

  /** Creates a filter based on an inclusion rule, tested against a named object's full name. */
  public static <N extends NamedObject> NamedObjectFilter<N> inclusionRule(
      final InclusionRule inclusionRule) {
    requireNonNull(inclusionRule, "No inclusion rule provided");
    return new InclusionRuleFilter<>(inclusionRule, true);
  }

  /** Creates a filter for a whole normalized full-name regex. */
  public static NamedObjectFilter<NamedObject> normalizedFullNameRegex(final String regex) {
    // Normalize each name part (schema, table, etc.) individually via the object's key - which
    // was built up from separately-captured parts, never by concatenating then splitting a
    // single string - before joining with ".". This avoids ambiguity when a quoted identifier
    // part itself contains a literal dot, since that dot is never mistaken for a separator.
    return regexFilter(regex, namedObject -> namedObject.key().normalized().join());
  }

  /** Creates a filter for a whole normalized name regex. */
  public static NamedObjectFilter<NamedObject> normalizedNameRegex(final String regex) {
    return regexFilter(regex, namedObject -> normalizeDatabaseObjectName(namedObject.getName()));
  }

  /** Creates a compound routine grep filter. */
  public static NamedObjectFilter<Routine> routineGrep(final GrepOptions grepOptions) {
    requireNonNull(grepOptions, "No grep options provided");
    return new RoutineGrepFilter(grepOptions);
  }

  /** Creates a filter for routine types, based on limit options. */
  public static NamedObjectFilter<Routine> routineTypes(final RoutineType routineType) {
    requireNonNull(routineType, "No routine type provided");
    return routine -> routine != null && routine.getRoutineType() == routineType;
  }

  /** Creates a compound table grep filter. */
  public static NamedObjectFilter<Table> tableGrep(final GrepOptions grepOptions) {
    requireNonNull(grepOptions, "No grep options provided");
    return new TableGrepFilter(grepOptions);
  }

  /** Creates a filter for table types, by table type name. */
  public static NamedObjectFilter<Table> tableTypes(final String... tableTypes) {
    return new TableTypesFilter(tableTypes);
  }

  private static NamedObjectFilter<NamedObject> regexFilter(
      final String regex, final Function<NamedObject, String> normalizedValueProjection) {
    requireNotBlank(regex, "No regular expression provided");
    final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    return namedObject -> {
      if (namedObject == null) {
        return false;
      }
      final String value = normalizedValueProjection.apply(namedObject);
      if (value == null) {
        return false;
      }
      return pattern.matcher(value).matches();
    };
  }

  private NamedObjectFilters() {
    // Prevent instantiation
  }
}
