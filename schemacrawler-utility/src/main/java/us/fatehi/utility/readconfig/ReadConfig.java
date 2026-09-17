/*
 * SchemaCrawler AI
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.readconfig;

import java.util.logging.Level;
import java.util.logging.Logger;
import us.fatehi.utility.string.StringFormat;

/**
 * Interface for accessing properties from a map, without needing to make a copy of the underlying
 * data.
 */
public interface ReadConfig {

  static final Logger LOGGER = Logger.getLogger(ReadConfig.class.getName());

  boolean containsKey(final String key);

  default boolean getBooleanValue(final String propertyName) {
    final String stringValue = getStringValue(propertyName, Boolean.FALSE.toString()).strip();
    return Boolean.parseBoolean(stringValue);
  }

  default int getIntegerValue(final String propertyName, final int defaultValue) {
    try {
      final String stringValue =
          getStringValue(propertyName, Integer.toString(defaultValue)).strip();
      return Integer.parseInt(stringValue);
    } catch (final NumberFormatException e) {
      final Logger LOGGER = Logger.getLogger(ReadConfig.class.getName());
      LOGGER.log(
          Level.FINEST,
          e,
          new StringFormat("Could not parse integer value for property <%s>", propertyName));
      return defaultValue;
    }
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
