/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.executable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.exceptions.ExecutionRuntimeException;
import schemacrawler.schemacrawler.exceptions.InternalRuntimeException;
import schemacrawler.schemacrawler.exceptions.SchemaCrawlerException;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.registry.BasePluginCommandRegistry;
import us.fatehi.utility.string.StringFormat;

/** Command registry for mapping command to executable. */
public final class CommandRegistry extends BasePluginCommandRegistry {

  private static final Logger LOGGER = Logger.getLogger(CommandRegistry.class.getName());

  private static CommandRegistry commandRegistrySingleton;

  public static final Comparator<? super SchemaCrawlerCommandProvider> commandComparator =
      (commandProvider1, commandProvider2) -> {
        final String fallbackProviderTypeName = "OperationCommandProvider";
        if (commandProvider1 == null || commandProvider2 == null) {
          throw new IllegalArgumentException("Null command provider found");
        }
        final String typeName1 = commandProvider1.getClass().getSimpleName();
        final String typeName2 = commandProvider2.getClass().getSimpleName();
        if (typeName1.equals(typeName2)) {
          return 0;
        }
        if (fallbackProviderTypeName.equals(typeName1)) {
          return 1;
        }
        if (fallbackProviderTypeName.equals(typeName2)) {
          return -1;
        }
        return typeName1.compareTo(typeName2);
      };

  public static CommandRegistry getCommandRegistry() {
    if (commandRegistrySingleton == null) {
      commandRegistrySingleton = new CommandRegistry();
      commandRegistrySingleton.log();
    }
    return commandRegistrySingleton;
  }

  private static List<SchemaCrawlerCommandProvider> loadCommandRegistry() {

    // Use thread-safe list
    final List<SchemaCrawlerCommandProvider> schemaCrawlerCommandProviders =
        new CopyOnWriteArrayList<>();

    try {
      final ServiceLoader<SchemaCrawlerCommandProvider> serviceLoader =
          ServiceLoader.load(
              SchemaCrawlerCommandProvider.class, CommandRegistry.class.getClassLoader());
      for (final SchemaCrawlerCommandProvider schemaCrawlerCommandProvider : serviceLoader) {
        LOGGER.log(
            Level.CONFIG,
            new StringFormat(
                "Loading command %s, provided by %s",
                schemaCrawlerCommandProvider.getSupportedCommands(),
                schemaCrawlerCommandProvider.getClass().getName()));
        schemaCrawlerCommandProviders.add(schemaCrawlerCommandProvider);
      }
    } catch (final Throwable e) {
      throw new InternalRuntimeException("Could not load extended command registry", e);
    }

    return schemaCrawlerCommandProviders;
  }

  private CommandRegistry() {
    super("SchemaCrawler Commands", loadCommandRegistry());
  }

  public SchemaCrawlerCommand configureNewCommand(
      final String command,
      final SchemaCrawlerOptions schemaCrawlerOptions,
      final Config additionalConfig,
      final OutputOptions outputOptions) {
    final List<SchemaCrawlerCommandProvider> executableCommandProviders = new ArrayList<>();
    findSupportedCommands(
        command, schemaCrawlerOptions, additionalConfig, outputOptions, executableCommandProviders);
    findSupportedOutputFormats(command, outputOptions, executableCommandProviders);

    Collections.sort(executableCommandProviders, commandComparator);

    final SchemaCrawlerCommandProvider executableCommandProvider =
        executableCommandProviders.get(0);
    LOGGER.log(Level.INFO, new StringFormat("Matched provider <%s>", executableCommandProvider));

    final String errorMessage = "Cannot run command <%s>".formatted(command);
    final SchemaCrawlerCommand scCommand;
    try {
      scCommand =
          (SchemaCrawlerCommand<?>) executableCommandProvider.newCommand(command, additionalConfig);
      if (scCommand == null) {
        throw new ExecutionRuntimeException("No SchemaCrawler command instantiated");
      }
      scCommand.setSchemaCrawlerOptions(schemaCrawlerOptions);
      scCommand.setOutputOptions(outputOptions);
    } catch (final SchemaCrawlerException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      throw new ExecutionRuntimeException(errorMessage, e);
    } catch (final Throwable e) {
      // Mainly catch NoClassDefFoundError, which is a Throwable,
      // for missing third-party jars
      LOGGER.log(Level.CONFIG, e.getMessage(), e);
      throw new InternalRuntimeException(errorMessage);
    }

    return scCommand;
  }

  private void findSupportedCommands(
      final String command,
      final SchemaCrawlerOptions schemaCrawlerOptions,
      final Config additionalConfig,
      final OutputOptions outputOptions,
      final List<SchemaCrawlerCommandProvider> executableCommandProviders) {
    for (final CommandProvider commandProvider : getCommandProviders()) {
      if ((commandProvider
              instanceof final SchemaCrawlerCommandProvider schemaCrawlerCommandProvider)
          && schemaCrawlerCommandProvider.supportsSchemaCrawlerCommand(
              command, schemaCrawlerOptions, additionalConfig, outputOptions)) {
        executableCommandProviders.add(schemaCrawlerCommandProvider);
        LOGGER.log(
            Level.FINE,
            new StringFormat("Adding command-provider <%s>", schemaCrawlerCommandProvider));
      }
    }
    if (executableCommandProviders.isEmpty()) {
      throw new ExecutionRuntimeException("Unknown command <%s>".formatted(command));
    }
  }

  private void findSupportedOutputFormats(
      final String command,
      final OutputOptions outputOptions,
      final List<SchemaCrawlerCommandProvider> executableCommandProviders) {
    final Iterator<SchemaCrawlerCommandProvider> iterator = executableCommandProviders.iterator();
    while (iterator.hasNext()) {
      final SchemaCrawlerCommandProvider executableCommandProvider = iterator.next();
      if (!executableCommandProvider.supportsOutputFormat(command, outputOptions)) {
        LOGGER.log(
            Level.FINE,
            new StringFormat(
                "Removing command-provider, since output format is not supported <%s>",
                executableCommandProvider));
        iterator.remove();
      }
    }
    if (executableCommandProviders.isEmpty()) {
      throw new ExecutionRuntimeException(
          "Output format <%s> not supported for command <%s>"
              .formatted(outputOptions.getOutputFormatValue(), command));
    }
  }
}
