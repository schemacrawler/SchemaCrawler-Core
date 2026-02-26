/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.catalogloader;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.requireNotBlank;

import schemacrawler.tools.executable.commandline.PluginCommand;
import schemacrawler.tools.options.Config;

public abstract class BaseCatalogLoaderProvider implements CatalogLoaderProvider {

  @Override
  public PluginCommand getCommandLineCommand() {
    return PluginCommand.empty();
  }

  @Override
  public final CatalogLoader<?> newCommand(final String command, final Config config) {
    requireNonNull(config, "No config provided");
    requireNotBlank(command, "No command provided");
    // Note that no check is done to ensure that the command matches the provider
    return newCommand(config);
  }
}
