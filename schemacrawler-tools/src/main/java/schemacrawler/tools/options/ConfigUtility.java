/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.options;

import java.util.Map;
import java.util.Properties;
import us.fatehi.utility.PropertiesUtility;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public final class ConfigUtility {

  /**
   * Create `Config` copied from another `Config`.
   *
   * @param config Provided `Config`
   */
  public static Config fromConfig(final Config config) {
    final Config newConfig = newConfig();
    if (config != null) {
      newConfig.merge(config);
    }
    return newConfig;
  }

  /**
   * Create `Config` from map.
   *
   * @param map Provided map for `Config`
   */
  public static Config fromMap(final Map<String, ? extends Object> map) {
    final Map<String, ? extends Object> filteredMap = PropertiesUtility.filterMap(map);
    return new Config(filteredMap);
  }

  /**
   * Create `Config` from properties.
   *
   * @param properties Provided properties for `Config`
   */
  public static Config fromProperties(final Properties properties) {
    final Map<String, ? extends Object> filteredMap = PropertiesUtility.filterMap(properties);
    return new Config(filteredMap);
  }

  /**
   * Creates a new empty `Config`.
   *
   * @return New `Config`
   */
  public static Config newConfig() {
    return fromMap(Map.of());
  }

  private ConfigUtility() {
    // Prevent instantiation
  }
}
