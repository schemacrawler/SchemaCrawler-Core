/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.registry;

import static java.util.Comparator.naturalOrder;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import schemacrawler.tools.executable.CommandProvider;
import schemacrawler.tools.executable.commandline.PluginCommand;
import us.fatehi.utility.property.PropertyName;

public abstract class BasePluginCommandRegistry extends BasePluginRegistry
    implements PluginCommandRegistry {

  protected final List<? extends CommandProvider> getCommandProviders() {
    return List.copyOf(commandProviders);
  }

  private final List<? extends CommandProvider> commandProviders;

  protected BasePluginCommandRegistry(
      final String registryName, final List<? extends CommandProvider> commandProviders) {
    super(registryName);
    this.commandProviders =
        requireNonNull(commandProviders, "No command provider registry provided");
  }

  @Override
  public final Collection<PluginCommand> getCommandLineCommands() {
    final Collection<PluginCommand> commandLineCommands = new HashSet<>();
    for (final CommandProvider commandProvider : commandProviders) {
      if (commandProvider != null) {
        commandLineCommands.add(commandProvider.getCommandLineCommand());
      }
    }
    return commandLineCommands;
  }

  @Override
  public final Collection<PluginCommand> getHelpCommands() {
    final Collection<PluginCommand> commandLineCommands = new HashSet<>();
    for (final CommandProvider commandProvider : commandProviders) {
      if (commandProvider != null) {
        commandLineCommands.add(commandProvider.getHelpCommand());
      }
    }
    return commandLineCommands;
  }

  @Override
  public final Collection<PropertyName> getRegisteredPlugins() {
    final Collection<PropertyName> supportedCommands = new HashSet<>();
    for (final CommandProvider commandProvider : commandProviders) {
      if (commandProvider != null) {
        supportedCommands.addAll(commandProvider.getSupportedCommands());
      }
    }

    final List<PropertyName> supportedCommandsOrdered = new ArrayList<>(supportedCommands);
    supportedCommandsOrdered.sort(naturalOrder());
    return supportedCommandsOrdered;
  }
}
