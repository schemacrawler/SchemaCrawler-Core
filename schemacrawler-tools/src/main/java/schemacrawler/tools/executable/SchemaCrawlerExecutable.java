/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.executable;

import static java.util.Objects.requireNonNull;
import static schemacrawler.tools.utility.SchemaCrawlerUtility.matchSchemaRetrievalOptions;
import static schemacrawler.tools.utility.SchemaCrawlerUtility.updateConnectionDataSource;
import static us.fatehi.utility.Utility.requireNotBlank;

import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;
import schemacrawler.schemacrawler.exceptions.ExecutionRuntimeException;
import schemacrawler.schemacrawler.exceptions.SchemaCrawlerException;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.options.ConfigUtility;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.options.OutputOptionsBuilder;
import schemacrawler.tools.state.AbstractExecutableState;
import schemacrawler.tools.state.ExecutableState;
import schemacrawler.tools.utility.SchemaCrawlerUtility;
import us.fatehi.utility.datasource.DatabaseConnectionSource;
import us.fatehi.utility.string.ObjectToStringFormat;
import us.fatehi.utility.string.StringFormat;

/**
 * Wrapper executable for any SchemaCrawler command. Looks up the command registry, and instantiates
 * the registered executable for the command. If the command is not a known command,
 * SchemaCrawlerExecutable will check if it is a query configured in the properties. If not, it will
 * assume that a query is specified on the command-line, and execute that.
 */
public final class SchemaCrawlerExecutable extends AbstractExecutableState
    implements ExecutableState {

  private static final Logger LOGGER = Logger.getLogger(SchemaCrawlerExecutable.class.getName());

  private final String command;
  private Config additionalConfig;
  private OutputOptions outputOptions;
  private SchemaCrawlerOptions schemaCrawlerOptions;
  private SchemaRetrievalOptions schemaRetrievalOptions;

  public SchemaCrawlerExecutable(final String command) {
    this.command = requireNotBlank(command, "No command specified");

    schemaCrawlerOptions = SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions();
    outputOptions = OutputOptionsBuilder.newOutputOptions();
    additionalConfig = ConfigUtility.newConfig();
  }

  public void execute() {

    if (!hasConnectionSource() && !hasCatalog()) {
      throw new ExecutionRuntimeException("Cannot execute command");
    }

    if (!hasConnectionSource()) {
      if (schemaRetrievalOptions == null) {
        schemaRetrievalOptions = SchemaRetrievalOptionsBuilder.newSchemaRetrievalOptions();
      }
    } else {
      // Match schema retrieval options, and update data source before any connections
      // are used
      DatabaseConnectionSource connectionSource = getConnectionSource();
      if (schemaRetrievalOptions == null) {
        schemaRetrievalOptions = matchSchemaRetrievalOptions(connectionSource);
      }
      updateConnectionDataSource(connectionSource, schemaRetrievalOptions);
    }

    try {

      // Load the command to see if it is available
      // Fail early (before loading the catalog) if the command is not
      // available
      final SchemaCrawlerCommand<?> scCommand = loadCommand();

      // Set identifiers strategy
      scCommand.setIdentifiers(schemaRetrievalOptions.getIdentifiers());
      // Set information schema queries
      scCommand.setInformationSchemaViews(schemaRetrievalOptions.getInformationSchemaViews());

      // Initialize, and check if the command is available
      scCommand.initialize();

      if (!hasCatalog()) {
        loadCatalog();
      }
      if (!hasERModel()) {
        buildERModel();
      }

      // Prepare to execute
      scCommand.setCatalog(getCatalog());
      scCommand.setERModel(getERModel());

      if (scCommand.usesConnection()) {
        scCommand.setConnectionSource(getConnectionSource());
      }

      // Execute
      LOGGER.log(Level.INFO, new StringFormat("Executing SchemaCrawler command <%s>", command));
      LOGGER.log(Level.CONFIG, new ObjectToStringFormat(scCommand.getIdentifiers()));
      LOGGER.log(Level.CONFIG, new ObjectToStringFormat(scCommand.getCommandOptions()));
      scCommand.execute();
    } catch (final SchemaCrawlerException e) {
      throw e;
    } catch (final Exception e) {
      throw new ExecutionRuntimeException(e);
    }
  }

  public OutputOptions getOutputOptions() {
    return outputOptions;
  }

  public SchemaCrawlerOptions getSchemaCrawlerOptions() {
    return schemaCrawlerOptions;
  }

  public SchemaRetrievalOptions getSchemaRetrievalOptions() {
    return schemaRetrievalOptions;
  }

  public void setAdditionalConfiguration(final Config additionalConfig) {
    // Make a defensive copy
    this.additionalConfig = ConfigUtility.fromConfig(additionalConfig);
  }

  public void setOutputOptions(final OutputOptions outputOptions) {
    if (outputOptions == null) {
      this.outputOptions = OutputOptionsBuilder.newOutputOptions();
    } else {
      this.outputOptions = outputOptions;
    }
  }

  public void setSchemaCrawlerOptions(final SchemaCrawlerOptions schemaCrawlerOptions) {
    if (schemaCrawlerOptions == null) {
      this.schemaCrawlerOptions = SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions();
    } else {
      this.schemaCrawlerOptions = schemaCrawlerOptions;
    }
  }

  public void setSchemaRetrievalOptions(final SchemaRetrievalOptions schemaRetrievalOptions) {
    this.schemaRetrievalOptions = schemaRetrievalOptions;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return command;
  }

  private void buildERModel() {
    final ERModel erModel = SchemaCrawlerUtility.buildERModel(getCatalog(), additionalConfig);
    requireNonNull(erModel, "ER model could not be built");
    setERModel(erModel);
  }

  private void loadCatalog() {
    final Catalog catalog =
        SchemaCrawlerUtility.getCatalog(
            getConnectionSource(), schemaRetrievalOptions, schemaCrawlerOptions, additionalConfig);
    requireNonNull(catalog, "Catalog could not be retrieved");
    setCatalog(catalog);
  }

  private SchemaCrawlerCommand<?> loadCommand() {
    final CommandRegistry commandRegistry = CommandRegistry.getCommandRegistry();
    final SchemaCrawlerCommand<?> scCommand =
        commandRegistry.configureNewCommand(
            command, schemaCrawlerOptions, additionalConfig, outputOptions);

    return requireNonNull(scCommand, "No SchemaCrawler command instantiated");
  }
}
