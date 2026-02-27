/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.executable;

import java.util.Collection;
import schemacrawler.tools.executable.commandline.PluginCommand;
import schemacrawler.tools.options.Config;
import us.fatehi.utility.property.PropertyName;

public interface CommandProvider {

  PluginCommand getCommandLineCommand();

  default PluginCommand getHelpCommand() {
    return getCommandLineCommand();
  }

  Collection<PropertyName> getSupportedCommands();

  <C extends Command<P, R>, P extends CommandOptions, R> C newCommand(
      String command, Config config);
}
