/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.catalog.counts;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import schemacrawler.tools.executable.commandline.PluginCommand;
import schemacrawler.loader.catalog.AbstractCatalogLoaderProvider;
import schemacrawler.tools.options.Config;
import us.fatehi.utility.property.PropertyName;

public class TableRowCountsLoaderProvider extends AbstractCatalogLoaderProvider {

  private static final PropertyName NAME =
      new PropertyName("countsloader", "Loader for table row counts");

  private static final String OPTION_NO_EMPTY_TABLES = "no-empty-tables";
  private static final String OPTION_LOAD_ROW_COUNTS = "load-row-counts";

  @Override
  public PluginCommand getCommandLineCommand() {
    final PluginCommand pluginCommand = PluginCommand.newCatalogLoaderCommand(NAME);
    pluginCommand
        .addOption(
            OPTION_LOAD_ROW_COUNTS,
            Boolean.class,
            "Loads row counts for each table",
            "This can be a time consuming operation",
            "Optional, defaults to false")
        .addOption(
            OPTION_NO_EMPTY_TABLES,
            Boolean.class,
            "Includes only tables that have rows of data",
            "Requires table row counts to be loaded",
            "Optional, default is false");
    return pluginCommand;
  }

  @Override
  public Collection<PropertyName> getSupportedCommands() {
    return List.of(NAME);
  }

  @Override
  public TableRowCountsLoader newCommand(final Config config) {
    requireNonNull(config, "No config provided");

    final TableRowCountsLoader loader = new TableRowCountsLoader(NAME);
    final TableRowCountsLoaderOptions options = createOptionsfromConfig(config);
    loader.configure(options);

    return loader;
  }

  private TableRowCountsLoaderOptions createOptionsfromConfig(final Config config) {
    final boolean loadRowCounts = config.getBooleanValue(OPTION_LOAD_ROW_COUNTS, false);
    final boolean noEmptyTables = config.getBooleanValue(OPTION_NO_EMPTY_TABLES, false);
    return new TableRowCountsLoaderOptions(loadRowCounts, noEmptyTables);
  }
}
