/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.databaseconnector;

import static java.util.Objects.requireNonNull;
import static schemacrawler.tools.executable.commandline.PluginCommand.newDatabasePluginCommand;
import static us.fatehi.utility.Utility.isBlank;

import java.sql.Connection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import schemacrawler.schemacrawler.InformationSchemaViewsBuilder;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.OptionsBuilder;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;
import schemacrawler.tools.executable.commandline.PluginCommand;
import us.fatehi.utility.datasource.DatabaseConnectionSourceBuilder;
import us.fatehi.utility.datasource.DatabaseServerType;

public class DatabaseConnectorOptionsBuilder
    implements OptionsBuilder<DatabaseConnectorOptionsBuilder, DatabaseConnectorOptions> {

  public static DatabaseConnectorOptionsBuilder builder(final DatabaseServerType dbServerType) {
    return new DatabaseConnectorOptionsBuilder(dbServerType);
  }

  private final DatabaseServerType dbServerType;
  private Predicate<String> supportsUrl;

  private BiConsumer<InformationSchemaViewsBuilder, Connection> informationSchemaViewsBuildProcess =
      (builder, conn) -> {};

  private BiConsumer<SchemaRetrievalOptionsBuilder, Connection> schemaRetrievalOptionsBuildProcess =
      (builder, conn) -> {};

  private Consumer<LimitOptionsBuilder> limitOptionsBuildProcess = builder -> {};
  private Supplier<DatabaseConnectionSourceBuilder> dbConnectionSourceBuildProcess;
  private PluginCommand helpCommand;

  private DatabaseConnectorOptionsBuilder(final DatabaseServerType dbServerType) {
    this.dbServerType = requireNonNull(dbServerType, "No database server type provided");
    supportsUrl = url -> false;
    informationSchemaViewsBuildProcess = (builder, conn) -> {};
    schemaRetrievalOptionsBuildProcess = (builder, conn) -> {};
    limitOptionsBuildProcess = builder -> {};
    dbConnectionSourceBuildProcess = () -> DatabaseConnectionSourceBuilder.builder("");
    helpCommand = newDatabasePluginCommand(dbServerType);
  }

  @Override
  public OptionsBuilder<DatabaseConnectorOptionsBuilder, DatabaseConnectorOptions> fromOptions(
      final DatabaseConnectorOptions options) {
    if (options == null) {
      return this;
    }

    if (!dbServerType.equals(options.dbServerType())) {
      throw new IllegalArgumentException("Cannot convert from options");
    }
    supportsUrl = options.supportsUrl();
    informationSchemaViewsBuildProcess = options.informationSchemaViewsBuildProcess();
    schemaRetrievalOptionsBuildProcess = options.schemaRetrievalOptionsBuildProcess();
    limitOptionsBuildProcess = options.limitOptionsBuildProcess();
    dbConnectionSourceBuildProcess = options.dbConnectionSourceBuildProcess();

    return this;
  }

  @Override
  public DatabaseConnectorOptions toOptions() {
    return new DatabaseConnectorOptions(
        dbServerType,
        supportsUrl,
        informationSchemaViewsBuildProcess,
        schemaRetrievalOptionsBuildProcess,
        limitOptionsBuildProcess,
        dbConnectionSourceBuildProcess,
        helpCommand);
  }

  public DatabaseConnectorOptionsBuilder withDatabaseConnectionSourceBuilder(
      final Supplier<DatabaseConnectionSourceBuilder> process) {
    if (process != null) {
      dbConnectionSourceBuildProcess = process;
    }
    return this;
  }

  public DatabaseConnectorOptionsBuilder withHelpCommand(final PluginCommand helpCommand) {
    if (helpCommand != null) {
      this.helpCommand = helpCommand;
    }
    return this;
  }

  public DatabaseConnectorOptionsBuilder withInformationSchemaViewsBuilder(
      final BiConsumer<InformationSchemaViewsBuilder, Connection> process) {
    if (process != null) {
      informationSchemaViewsBuildProcess = process;
    }
    return this;
  }

  public DatabaseConnectorOptionsBuilder withInformationSchemaViewsFromResourceFolder(
      final String resourceFolder) {
    if (!isBlank(resourceFolder)) {
      informationSchemaViewsBuildProcess =
          (informationSchemaViewsBuilder, connection) ->
              informationSchemaViewsBuilder.fromResourceFolder(resourceFolder);
    }
    return this;
  }

  public DatabaseConnectorOptionsBuilder withLimitOptionsBuilder(
      final Consumer<LimitOptionsBuilder> process) {
    if (process != null) {
      limitOptionsBuildProcess = process;
    }
    return this;
  }

  public DatabaseConnectorOptionsBuilder withSchemaRetrievalOptionsBuilder(
      final BiConsumer<SchemaRetrievalOptionsBuilder, Connection> process) {
    if (process != null) {
      schemaRetrievalOptionsBuildProcess = process;
    }
    return this;
  }

  public DatabaseConnectorOptionsBuilder withUrlStartsWith(final String urlStartsWith) {
    if (!isBlank(urlStartsWith)) {
      supportsUrl = url -> url != null && url.startsWith(urlStartsWith);
    }
    return this;
  }

  public DatabaseConnectorOptionsBuilder withUrlSupportPredicate(
      final Predicate<String> supportsUrl) {
    if (supportsUrl != null) {
      this.supportsUrl = supportsUrl;
    }
    return this;
  }
}
