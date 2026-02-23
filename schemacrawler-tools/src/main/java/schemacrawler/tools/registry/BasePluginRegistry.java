/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.registry;

import static us.fatehi.utility.Utility.requireNotBlank;

import java.util.logging.Level;
import java.util.logging.Logger;
import us.fatehi.utility.property.PropertyNameUtility;

public abstract class BasePluginRegistry implements PluginRegistry {

  private static final Logger LOGGER = Logger.getLogger(BasePluginRegistry.class.getName());

  private final String registryName;

  protected BasePluginRegistry(final String registryName) {
    this.registryName = requireNotBlank(registryName, "No registry name provided");
  }

  @Override
  public final String getName() {
    return registryName;
  }

  @Override
  public final void log() {
    final boolean log = LOGGER.isLoggable(Level.CONFIG);
    if (!log) {
      return;
    }

    final String title = "Registered %s:".formatted(getName());
    final String registeredPlugins = PropertyNameUtility.tableOf(title, getRegisteredPlugins());
    LOGGER.log(Level.CONFIG, registeredPlugins);
  }
}
