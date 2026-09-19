/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.isBlank;
import static us.fatehi.utility.Utility.requireNotBlank;
import static us.fatehi.utility.database.DatabaseUtility.normalizeDatabaseObjectName;

import java.util.regex.Pattern;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Routine;
import schemacrawler.schema.RoutineType;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.GrepOptions;
import us.fatehi.utility.UtilityMarker;

/** Factory methods for filters over named objects. */
@UtilityMarker
public final class NamedObjectFilters {

  /**
   * Creates a filter that tests a named object's full (schema-qualified) name against a regular
   * expression. The pattern is matched, case-insensitively, against two candidate strings and
   * accepted if either matches: the object's original (raw) full name, exactly as returned by
   * {@link NamedObject#getFullName()}, and a normalized full name built by stripping quoting
   * (double quotes, backticks, or brackets) and lower-casing each name part - schema, table, and so
   * on - individually, then joining the parts with a "." separator.
   *
   * <p>Matching against both forms means a single regex can be written either the way a user would
   * type an unquoted, lower-case identifier, or the way the database itself would print a quoted,
   * case-sensitive identifier, without the caller having to know or guess which form a particular
   * database driver returns.
   *
   * <p>Use this filter when tables, routines, or other named objects need to be included or
   * excluded based on their fully qualified name - for example, "sales.orders" or a catalog- and
   * schema-qualified name - rather than just the simple (unqualified) name. For simple-name-only
   * matching, prefer {@link #nameRegex(String)}.
   *
   * @param regex regular expression to match against the raw and normalized full name
   * @return a filter that accepts named objects whose full name matches the regex
   * @throws IllegalArgumentException if the regex is blank
   */
  public static NamedObjectFilter<NamedObject> fullNameRegex(final String regex) {
    requireNotBlank(regex, "No regular expression provided");
    final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    return namedObject -> {
      if (namedObject == null) {
        return false;
      }
      // Normalize each name part (schema, table, etc.) individually via the object's key - which
      // was built up from separately-captured parts, never by concatenating then splitting a
      // single string - before joining with ".". This avoids ambiguity when a quoted identifier
      // part itself contains a literal dot, since that dot is never mistaken for a separator.
      final String fullName = namedObject.getFullName();
      if (isBlank(fullName)) {
        return false;
      }
      final String normalizedFullName = namedObject.key().normalized().join();
      return pattern.matcher(fullName).matches() || pattern.matcher(normalizedFullName).matches();
    };
  }

  /**
   * Creates a filter that tests a named object's simple (unqualified) name against a regular
   * expression. The pattern is matched, case-insensitively, against two candidate strings and
   * accepted if either matches: the object's original (raw) name, exactly as returned by {@link
   * NamedObject#getName()}, and a normalized name with quoting (double quotes, backticks, or
   * brackets) stripped and the value lower-cased.
   *
   * <p>Matching against both forms means a single regex can be written either the way a user would
   * type an unquoted, lower-case identifier, or the way the database itself would print a quoted,
   * case-sensitive identifier, without the caller having to know or guess which form a particular
   * database driver returns.
   *
   * <p>Use this filter when named objects need to be included or excluded by their simple name
   * alone - for example, matching any table named "orders" regardless of which schema it is in.
   * When the schema (or other qualifying parts) of the name also matter, use {@link
   * #fullNameRegex(String)} instead, since this filter never looks beyond the simple name.
   *
   * @param regex regular expression to match against the raw and normalized name
   * @return a filter that accepts named objects whose simple name matches the regex
   * @throws IllegalArgumentException if the regex is blank
   */
  public static NamedObjectFilter<NamedObject> nameRegex(final String regex) {
    requireNotBlank(regex, "No regular expression provided");
    final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    return namedObject -> {
      if (namedObject == null) {
        return false;
      }
      final String name = namedObject.getName();
      if (isBlank(name)) {
        return false;
      }
      final String normalizedName = normalizeDatabaseObjectName(name);
      return pattern.matcher(name).matches() || pattern.matcher(normalizedName).matches();
    };
  }

  /**
   * Creates a compound filter over routines built from {@link GrepOptions}, which can, for example,
   * include or exclude routines whose definition (source text) matches a pattern, in addition to
   * name-based inclusion rules.
   *
   * <p>Use this filter when routine selection needs to look inside the routine's definition (or
   * other grep-able content), not just its name - typically driven by a "grep" style command-line
   * or configuration option. For filtering purely by name, prefer {@link #nameRegex(String)},
   * {@link #fullNameRegex(String)}.
   *
   * @param grepOptions options describing what content of a routine to search, and with what
   *     patterns
   * @return a compound routine grep filter
   * @throws NullPointerException if the grep options are null
   */
  public static NamedObjectFilter<Routine> routineGrep(final GrepOptions grepOptions) {
    requireNonNull(grepOptions, "No grep options provided");
    return new RoutineGrepFilter(grepOptions);
  }

  /**
   * Creates a filter that accepts only routines of a given {@link RoutineType} (for example,
   * procedures versus functions).
   *
   * <p>Use this filter to restrict crawling or reporting to just one kind of routine, independent
   * of any name-based filtering, which can be composed alongside it.
   *
   * @param routineType the routine type to accept; a null routine is never matched, but a null
   *     {@code routineType} results in an exception rather than a permissive filter, since a
   *     missing type usually indicates a configuration error
   * @return a filter that accepts only routines of the given type
   * @throws NullPointerException if the routine type is null
   */
  public static NamedObjectFilter<Routine> routineTypes(final RoutineType routineType) {
    requireNonNull(routineType, "No routine type provided");
    return routine -> routine != null && routine.getRoutineType() == routineType;
  }

  /**
   * Creates a compound filter over tables built from {@link GrepOptions}, which can, for example,
   * include or exclude tables whose column definitions or remarks match a pattern, in addition to
   * name-based inclusion rules.
   *
   * <p>Use this filter when table selection needs to look inside the table's columns, remarks, or
   * other grep-able content, not just its name - typically driven by a "grep" style command-line or
   * configuration option. For filtering purely by name, prefer {@link #nameRegex(String)}, {@link
   * #fullNameRegex(String)}.
   *
   * @param grepOptions options describing what content of a table to search, and with what patterns
   * @return a compound table grep filter
   * @throws NullPointerException if the grep options are null
   */
  public static NamedObjectFilter<Table> tableGrep(final GrepOptions grepOptions) {
    requireNonNull(grepOptions, "No grep options provided");
    return new TableGrepFilter(grepOptions);
  }

  /**
   * Creates a filter that accepts only tables whose table type name (for example, "table" or
   * "view") is one of the given values.
   *
   * <p>Use this filter to restrict crawling or reporting to specific kinds of tables, such as views
   * only, independent of any name-based filtering, which can be composed alongside it. An empty (or
   * unspecified) list of table types matches no tables; pass no arguments only when the intent is
   * to exclude all tables via this filter.
   *
   * @param tableTypes table type names to accept, matched case-insensitively; may be empty
   * @return a filter that accepts only tables whose table type name is in the given list
   */
  public static NamedObjectFilter<Table> tableTypes(final String... tableTypes) {
    return new TableTypesFilter(tableTypes);
  }

  private NamedObjectFilters() {
    // Prevent instantiation
  }
}
