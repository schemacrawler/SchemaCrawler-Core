/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.registry;

import java.util.Collection;
import java.util.List;
import schemacrawler.tools.executable.commandline.PluginCommand;

public interface PluginCommandRegistry extends PluginRegistry {

  default Collection<PluginCommand> getCommandLineCommands() {
    return List.of();
  }

  default Collection<PluginCommand> getHelpCommands() {
    return List.of();
  }
}
