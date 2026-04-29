/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.command;

import java.util.Collection;
import schemacrawler.tools.executable.commandline.PluginCommand;
import schemacrawler.tools.registry.PluginRegistry;

public interface PluginCommandRegistry<R extends CommandProvider> extends PluginRegistry {

  Collection<PluginCommand> getCommandLineCommands();

  Collection<PluginCommand> getHelpCommands();
}
