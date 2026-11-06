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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import us.fatehi.utility.Builder;
import us.fatehi.utility.UtilityMarker;

/** Builds a configuration with explicitly selected sources and ordered compatibility aliases. */
@UtilityMarker
public final class SystemConfigBuilder implements Builder<ReadConfig> {

  private static class NullReadConfig implements ReadConfig {

    @Override
    public boolean containsKey(final String key) {
      return false;
    }

    @Override
    public String getStringValue(final String key, final String defaultValue) {
      return defaultValue;
    }
  }

  /**
   * Creates a builder with no sources selected and no aliases registered.
   *
   * @return Configuration builder
   */
  public static SystemConfigBuilder builder() {
    return new SystemConfigBuilder();
  }

  private final Map<String, List<String>> aliases;
  private ReadConfig env;
  private ReadConfig sysProp;

  private SystemConfigBuilder() {
    aliases = new HashMap<>();
    env = new NullReadConfig();
    sysProp = new NullReadConfig();
  }

  /**
   * Builds an immutable configuration view. Values remain live rather than cached.
   *
   * @return Configuration view
   * @throws IllegalStateException If no lookup source was selected
   */
  @Override
  public SystemConfig build() {
    if (sysProp instanceof NullReadConfig && env instanceof NullReadConfig) {
      throw new IllegalStateException("No configuration source selected");
    }
    return new SystemConfig(sysProp, env, Map.copyOf(aliases));
  }

  /**
   * Defines aliases in lookup order, replacing any previous definition for this key.
   *
   * @param key Canonical key
   * @param aliases Compatibility names, checked only when the canonical key is absent
   * @return This builder
   */
  public SystemConfigBuilder withAlias(final String key, final String... aliases) {
    if (isBlank(key) || aliases == null || aliases.length == 0) {
      return this;
    }
    final List<String> filteredAliases =
        List.copyOf(Arrays.stream(aliases).filter(alias -> !isBlank(alias)).toList());
    if (filteredAliases.isEmpty()) {
      return this;
    }
    this.aliases.put(key, filteredAliases);
    return this;
  }

  /**
   * Enables environment-variable lookup.
   *
   * @return This builder
   */
  public SystemConfigBuilder withEnvironmentVariables() {
    return withEnvironmentVariables((EnvironmentVariableConfig) System::getenv);
  }

  /**
   * Enables system-property lookup, which precedes environment-variable lookup.
   *
   * @return This builder
   */
  public SystemConfigBuilder withSystemProperties() {
    return withSystemProperties(new SystemPropertiesConfig());
  }

  SystemConfigBuilder withEnvironmentVariables(final ReadConfig env) {
    this.env = requireNonNull(env, "No environment accessor provided");
    return this;
  }

  SystemConfigBuilder withSystemProperties(final ReadConfig sysProp) {
    this.sysProp = requireNonNull(sysProp, "No system-property accessor provided");
    return this;
  }
}
