/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.readconfig;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.isBlank;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import us.fatehi.utility.string.StringFormat;

final class SystemConfig implements ReadConfig {

  private static final Logger LOGGER = Logger.getLogger(SystemConfig.class.getName());

  private final Map<String, List<String>> aliases;
  private final ReadConfig env;
  private final ReadConfig sysProp;

  SystemConfig(
      final ReadConfig sysProp, final ReadConfig env, final Map<String, List<String>> aliases) {
    this.sysProp = requireNonNull(sysProp, "No system property reader provided");
    this.env = requireNonNull(env, "No environmental variable reader provided");
    this.aliases = requireNonNull(aliases, "No aliases provided");
  }

  @Override
  public boolean containsKey(final String key) {
    final boolean containsKey = resolveKey(sysProp, key) != null || resolveKey(env, key) != null;
    return containsKey;
  }

  /**
   * Returns the stripped value as a string, even if the underlying value is not a String. Uses the
   * default when no name is present, and converts null to an empty string.
   */
  @Override
  public String getStringValue(final String key, final String defaultValue) {

    String value = null;

    final String sysPropKey = resolveKey(sysProp, key);
    final String envKey = sysPropKey == null ? resolveKey(env, key) : null;
    if (sysPropKey != null) {
      value = sysProp.getStringValue(sysPropKey, defaultValue);
      LOGGER.log(Level.CONFIG, new StringFormat("Using system property for <%s>", sysPropKey));
    } else if (envKey != null) {
      value = env.getStringValue(envKey, defaultValue);
      LOGGER.log(Level.CONFIG, new StringFormat("Using environmental variable for <%s>", envKey));
    } else {
      value = defaultValue;
    }

    if (value == null) {
      value = "";
    }
    value = value.strip();
    LOGGER.log(Level.CONFIG, new StringFormat("Configuration value <%s>=<%s>", key, value));

    return value;
  }

  private String resolveKey(final ReadConfig source, final String key) {
    if (isBlank(key)) {
      return null;
    }
    if (source.containsKey(key)) {
      return key;
    }
    for (final String alias : aliases.getOrDefault(key, List.of())) {
      if (source.containsKey(alias)) {
        return alias;
      }
    }
    return null;
  }
}
