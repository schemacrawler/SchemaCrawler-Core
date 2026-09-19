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

import java.util.regex.Pattern;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Routine;
import schemacrawler.schema.RoutineType;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.GrepOptions;
import us.fatehi.utility.UtilityMarker;

/**
 * Factory methods for filters over named database objects (tables, routines, and so on).
 *
 * <p>Use {@link #nameRegex(String)} or {@link #fullNameRegex(String)} to select objects by name;
 * use {@link #tableGrep(GrepOptions)} or {@link #routineGrep(GrepOptions)} to also search inside an
 * object's columns, parameters, remarks, or definition; and use {@link #tableTypes(String...)} or
 * {@link #routineTypes(RoutineType)} to restrict output to specific kinds of objects. Filters can
 * be combined as needed.
 */
@UtilityMarker
public final class NamedObjectFilters {

  /**
   * Creates a filter that matches a named object's full (schema-qualified) name against a regular
   * expression - for example, to select "sales.orders" or a catalog- and schema-qualified name.
   * Matching is case-insensitive, and works whether or not the regex accounts for quoting (such as
   * quotes added around names with special characters). Use {@link #nameRegex(String)} instead if
   * only the simple (unqualified) name matters.
   *
   * @param regex regular expression to match against the full name
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
      final String fullName = namedObject.getFullName();
      // IMPORTANT: Schema names may be blank. Do not do a blank check at this point.
      if (fullName == null) {
        return false;
      }
      // Also test the qualified-name key (schema, table, etc. parts joined with "."), exactly as
      // captured when the object was created. This lets an unquoted regular expression match a
      // full name that includes quoting added purely for display, without making the match
      // case-insensitive.
      final String unquotedFullName = NamedObjectUtility.unquotedFullName(namedObject);
      return pattern.matcher(fullName).matches() || pattern.matcher(unquotedFullName).matches();
    };
  }

  /**
   * Creates a filter that matches a named object's simple (unqualified) name against a regular
   * expression - for example, to select any table named "orders", regardless of which schema it is
   * in. Matching is case-insensitive. Use {@link #fullNameRegex(String)} instead when the schema
   * (or other qualifying parts) of the name also matter.
   *
   * @param regex regular expression to match against the name
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
      return pattern.matcher(name).matches();
    };
  }

  /**
   * Creates a "grep" style filter over routines, using {@link GrepOptions} to match a routine's
   * definition (source text), parameters, or remarks, in addition to any name-based inclusion
   * rules. Use this when routine selection needs to search inside the routine, not just its name;
   * for name-only matching, use {@link #nameRegex(String)} or {@link #fullNameRegex(String)}
   * instead.
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
   * Creates a filter that accepts only routines of a given {@link RoutineType} - for example,
   * procedures only, or functions only. Use this to restrict output to one kind of routine; it can
   * be combined with name-based or grep filters.
   *
   * @param routineType the routine type to accept
   * @return a filter that accepts only routines of the given type
   * @throws NullPointerException if the routine type is null
   */
  public static NamedObjectFilter<Routine> routineTypes(final RoutineType routineType) {
    requireNonNull(routineType, "No routine type provided");
    return routine -> routine != null && routine.getRoutineType() == routineType;
  }

  /**
   * Creates a "grep" style filter over tables, using {@link GrepOptions} to match a table's
   * columns, remarks, or definition, in addition to any name-based inclusion rules. Use this when
   * table selection needs to search inside the table, not just its name; for name-only matching,
   * use {@link #nameRegex(String)} or {@link #fullNameRegex(String)} instead.
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
   * "view") is one of the given values, matched case-insensitively. Use this to restrict output to
   * specific kinds of tables, such as views only; it can be combined with name-based filters. Pass
   * no arguments to exclude all tables via this filter.
   *
   * @param tableTypes table type names to accept; may be empty
   * @return a filter that accepts only tables whose table type name is in the given list
   */
  public static NamedObjectFilter<Table> tableTypes(final String... tableTypes) {
    return new TableTypesFilter(tableTypes);
  }

  private NamedObjectFilters() {
    // Prevent instantiation
  }
}
