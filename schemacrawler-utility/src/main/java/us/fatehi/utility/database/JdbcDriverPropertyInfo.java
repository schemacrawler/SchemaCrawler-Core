/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.database;

import static us.fatehi.utility.Utility.isBlank;
import static us.fatehi.utility.Utility.requireNotBlank;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import us.fatehi.utility.property.Property;

/**
 * Immutable and secure version of JDBC driver property definition extracted from {@link
 * java.sql.DriverPropertyInfo}.
 */
public record JdbcDriverPropertyInfo(
    String name, String description, boolean required, String value, List<String> choices)
    implements Property {

  private static final Predicate<String> passwordMatcher =
      Pattern.compile("password", Pattern.CASE_INSENSITIVE).asPredicate();

  public JdbcDriverPropertyInfo {
    name = requireNotBlank(name, "Property name required");
    description = isBlank(description) ? "" : description;
    value = passwordMatcher.test(name) ? null : value;
    choices = choices == null ? List.of() : List.copyOf(choices);
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Object getValue() {
    return value;
  }
}
