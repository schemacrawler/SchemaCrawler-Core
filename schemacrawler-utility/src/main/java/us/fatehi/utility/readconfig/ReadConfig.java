/*
 * SchemaCrawler AI
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.readconfig;

/**
 * Interface for accessing properties from a map, without needing to make a copy of the underlying
 * data.
 */
public interface ReadConfig {

  boolean containsKey(final String key);

  default boolean getBooleanValue(final String propertyName) {
    return Boolean.parseBoolean(getStringValue(propertyName, Boolean.FALSE.toString()).strip());
  }

  default String getStringValue(final String propertyName) {
    return getStringValue(propertyName, "").strip();
  }

  /**
   * Gets the value of the specified environment variable.
   *
   * @param propertyName the name of the environment variable
   * @param defaultValue Default value for the property
   * @return The string value of the variable, or null if the variable is not defined
   */
  String getStringValue(String propertyName, String defaultValue);
}
