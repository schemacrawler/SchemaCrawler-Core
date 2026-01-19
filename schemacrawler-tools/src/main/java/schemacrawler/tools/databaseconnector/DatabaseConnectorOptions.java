/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.databaseconnector;

import static java.util.Objects.requireNonNull;

import java.sql.Connection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import schemacrawler.schemacrawler.InformationSchemaViewsBuilder;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.Options;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;
import schemacrawler.tools.executable.commandline.PluginCommand;
import us.fatehi.utility.datasource.DatabaseConnectionSourceBuilder;
import us.fatehi.utility.datasource.DatabaseServerType;

public record DatabaseConnectorOptions(
    DatabaseServerType dbServerType,
    Predicate<String> supportsUrl,
    BiConsumer<InformationSchemaViewsBuilder, Connection> informationSchemaViewsBuildProcess,
    BiConsumer<SchemaRetrievalOptionsBuilder, Connection> schemaRetrievalOptionsBuildProcess,
    Consumer<LimitOptionsBuilder> limitOptionsBuildProcess,
    Supplier<DatabaseConnectionSourceBuilder> dbConnectionSourceBuildProcess,
    PluginCommand helpCommand)
    implements Options {

  public DatabaseConnectorOptions {
    requireNonNull(dbServerType, "Database server type not provided");
    requireNonNull(supportsUrl, "Supports URL predicate not provided");
    requireNonNull(
        informationSchemaViewsBuildProcess, "Information schema views builder not provided");
    requireNonNull(
        schemaRetrievalOptionsBuildProcess, "Schema retrieval options builder not provided");
    requireNonNull(limitOptionsBuildProcess, "Limit options builder not provided");
    requireNonNull(
        dbConnectionSourceBuildProcess, "Database connection source builder not provided");
    requireNonNull(helpCommand, "Help command not provided");
  }
}
