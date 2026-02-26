/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.registry;

import java.util.Collection;
import java.util.Collections;
import schemacrawler.tools.executable.CommandProvider;
import schemacrawler.tools.executable.commandline.PluginCommand;

public interface PluginCommandRegistry<R extends CommandProvider> extends PluginRegistry {

  default Collection<PluginCommand> getCommandLineCommands() {
    return Collections.emptyList();
  }

  default Collection<PluginCommand> getHelpCommands() {
    return Collections.emptyList();
  }
}
