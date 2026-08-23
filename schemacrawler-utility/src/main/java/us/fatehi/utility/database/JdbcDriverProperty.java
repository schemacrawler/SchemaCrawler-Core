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

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import us.fatehi.utility.property.Property;

/**
 * Immutable and secure version of JDBC driver property definition extracted from {@link
 * java.sql.DriverPropertyInfo}.
 */
public record JdbcDriverProperty(
    String name, String description, boolean required, String value, List<String> choices)
    implements Property {

  private static final Predicate<String> passwordMatcher =
      Pattern.compile("password", Pattern.CASE_INSENSITIVE).asPredicate();

  public JdbcDriverProperty {
    name = requireNotBlank(name, "Property name required");
    description = isBlank(description) ? "" : description;
    value = passwordMatcher.test(name) ? null : value;
    choices = choices == null ? List.of() : List.copyOf(choices);
  }

  /** {@inheritDoc} */
  @Override
  public String getDescription() {
    return description;
  }

  /** {@inheritDoc} */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Gets the the current value of the property, based on a combination of the information supplied
   * to the method <code>getPropertyInfo</code>, the Java environment, and the driver-supplied
   * default values. This field may be null if no value is known.
   *
   * @return Value of the property
   */
  @Override
  public String getValue() {
    return value;
  }

  /**
   * Gets the array of possible values if the value for the field <code>DriverPropertyInfo.value
   * </code> may be selected from a particular set of values.
   *
   * @return Available choices for the value of a property
   */
  public Collection<String> getChoices() {
    return choices;
  }

  /**
   * The <code>required</code> field is <code>true</code> if a value must be supplied for this
   * property during <code>Driver.connect</code> and <code>false</code> otherwise.
   *
   * @return Whether the property is required
   */
  public boolean isRequired() {
    return required;
  }

  @Override
  public String toString() {
    final StringBuilder buffer = new StringBuilder();
    buffer.append("%s = %s%n".formatted(getName(), getValue()));
    if (hasDescription()) {
      buffer.append(getDescription()).append("%n".formatted());
    }
    buffer.append("  is required? %b%n  choices: %s".formatted(isRequired(), getChoices()));
    return buffer.toString();
  }
}
