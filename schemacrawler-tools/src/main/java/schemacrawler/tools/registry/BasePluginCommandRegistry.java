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
import schemacrawler.schemacrawler.exceptions.InternalRuntimeException;
import schemacrawler.tools.command.CommandProvider;
import schemacrawler.tools.executable.commandline.PluginCommand;
import us.fatehi.utility.property.PropertyName;

public abstract class BasePluginCommandRegistry<R extends CommandProvider>
    extends BasePluginRegistry implements PluginCommandRegistry<R> {

  @SuppressWarnings("unchecked")
  protected static <R extends CommandProvider> List<R> instantiateProviders(
      final List<String> classNames) {
    final List<R> providers = new ArrayList<>();
    for (final String className : classNames) {
      try {
        final Class<?> cls = Class.forName(className);
        providers.add((R) cls.getDeclaredConstructor().newInstance());
      } catch (final Exception e) {
        throw new InternalRuntimeException(
            "Could not instantiate plugin provider <%s>".formatted(className), e);
      }
    }
    return List.copyOf(providers);
  }

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
        final PluginCommand commandLineCommand = commandProvider.getCommandLineCommand();
        if (!commandLineCommand.isEmpty()) {
          commandLineCommands.add(commandLineCommand);
        }
      }
    }
    return commandLineCommands;
  }

  @Override
  public final Collection<PluginCommand> getHelpCommands() {
    final Collection<PluginCommand> commandLineCommands = new HashSet<>();
    for (final R commandProvider : commandProviders) {
      if (commandProvider != null) {
        final PluginCommand helpCommand = commandProvider.getHelpCommand();
        if (!helpCommand.isEmpty()) {
          commandLineCommands.add(helpCommand);
        }
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

  protected List<R> getCommandProviders() {
    return List.copyOf(commandProviders);
  }
}
