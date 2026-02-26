/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.registry;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import schemacrawler.tools.executable.CommandProvider;
import schemacrawler.tools.executable.commandline.PluginCommand;
import us.fatehi.utility.property.PropertyName;

public abstract class BasePluginCommandRegistry<R extends CommandProvider>
    extends BasePluginRegistry implements PluginCommandRegistry<R> {

  private final List<R> commandProviders;

  protected BasePluginCommandRegistry(final String name, final List<R> commandProviders) {
    super(name);
    this.commandProviders =
        List.copyOf(requireNonNull(commandProviders, "No command providers provided"));
  }

  @Override
  public final Collection<PluginCommand> getCommandLineCommands() {
    final Collection<PluginCommand> commandLineCommands = new HashSet<>();
    for (final R commandProvider : commandProviders) {
      if (commandProvider != null) {
        commandLineCommands.add(commandProvider.getCommandLineCommand());
      }
    }
    return commandLineCommands;
  }

  protected List<R> getCommandProviders() {
    return List.copyOf(commandProviders);
  }

  @Override
  public final Collection<PluginCommand> getHelpCommands() {
    final Collection<PluginCommand> commandLineCommands = new HashSet<>();
    for (final R commandProvider : commandProviders) {
      if (commandProvider != null) {
        commandLineCommands.add(commandProvider.getHelpCommand());
      }
    }
    return commandLineCommands;
  }

  @Override
  public final Collection<PropertyName> getRegisteredPlugins() {
    final Collection<PropertyName> supportedCommands = new HashSet<>();
    for (final R commandProvider : commandProviders) {
      if (commandProvider != null) {
        supportedCommands.addAll(commandProvider.getSupportedCommands());
      }
    }

    final List<PropertyName> supportedCommandsOrdered = new ArrayList<>(supportedCommands);
    Collections.sort(supportedCommandsOrdered);
    return List.copyOf(supportedCommandsOrdered);
  }
}
