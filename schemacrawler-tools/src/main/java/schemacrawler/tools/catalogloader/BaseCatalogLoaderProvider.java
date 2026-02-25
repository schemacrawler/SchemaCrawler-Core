/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.catalogloader;

import schemacrawler.tools.executable.commandline.PluginCommand;

public abstract class BaseCatalogLoaderProvider implements CatalogLoaderProvider {

  @Override
  public PluginCommand getCommandLineCommand() {
    return PluginCommand.empty();
  }
}
