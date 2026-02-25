/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.counts;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import schemacrawler.tools.catalogloader.BaseCatalogLoaderProvider;
import schemacrawler.tools.executable.commandline.PluginCommand;
import schemacrawler.tools.options.Config;
import us.fatehi.utility.property.PropertyName;

public class TableRowCountsCatalogLoaderProvider extends BaseCatalogLoaderProvider {

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
  public TableRowCountsCatalogLoader newCommand(final Config config) {
    requireNonNull(config, "No config provided");

    final TableRowCountsCatalogLoader loader = new TableRowCountsCatalogLoader(NAME);
    final TableRowCountsCatalogLoaderOptions options = createOptionsfromConfig(config);
    loader.configure(options);

    return loader;
  }

  private TableRowCountsCatalogLoaderOptions createOptionsfromConfig(final Config config) {
    final boolean loadRowCounts = config.getBooleanValue(OPTION_LOAD_ROW_COUNTS, false);
    final boolean noEmptyTables = config.getBooleanValue(OPTION_NO_EMPTY_TABLES, false);
    return new TableRowCountsCatalogLoaderOptions(loadRowCounts, noEmptyTables);
  }
}
