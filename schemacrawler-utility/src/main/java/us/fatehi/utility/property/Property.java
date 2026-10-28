/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.property;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;
import static us.fatehi.utility.Utility.isBlank;

import java.io.Serializable;
import java.util.Comparator;

public interface Property extends Serializable, Comparable<Property> {

  Comparator<Property> NAME_COMPARATOR =
      comparing(Property::getName, nullsFirst(String.CASE_INSENSITIVE_ORDER));

  @Override
  default int compareTo(final Property otherProperty) {
    if (this == otherProperty) {
      return 0;
    }
    if (otherProperty == null) {
      return 1;
    }
    return NAME_COMPARATOR.compare(this, otherProperty);
  }

  /**
   * Gets the description of the property.
   *
   * @return Description
   */
  String getDescription();

  /**
   * Gets the name of the property.
   *
   * @return Name
   */
  String getName();

  /**
   * Gets the value of the property.
   *
   * @return Value
   */
  Object getValue();

  default boolean hasDescription() {
    return !isBlank(getDescription());
  }

  default boolean hasValue() {
    return getValue() != null;
  }
}
