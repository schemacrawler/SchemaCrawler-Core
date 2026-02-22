/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.executable;

import schemacrawler.tools.executable.commandline.PluginCommand;
import schemacrawler.tools.options.Config;

public interface CommandProvider {

  PluginCommand getCommandLineCommand();

  default PluginCommand getHelpCommand() {
    return getCommandLineCommand();
  }

  SchemaCrawlerCommand<?> newCommand(String command, Config config);
}
