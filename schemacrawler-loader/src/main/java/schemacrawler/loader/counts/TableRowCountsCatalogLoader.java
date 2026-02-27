/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.counts;

import static schemacrawler.filter.ReducerFactory.getTableReducer;

import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.exceptions.ExecutionRuntimeException;
import schemacrawler.tools.catalogloader.BaseCatalogLoader;
import us.fatehi.utility.property.PropertyName;
import us.fatehi.utility.scheduler.TaskDefinition;
import us.fatehi.utility.scheduler.TaskRunner;
import us.fatehi.utility.scheduler.TaskRunners;

public class TableRowCountsCatalogLoader
    extends BaseCatalogLoader<TableRowCountsCatalogLoaderOptions> {

  private static final Logger LOGGER =
      Logger.getLogger(TableRowCountsCatalogLoader.class.getName());

  static final String OPTION_NO_EMPTY_TABLES = "no-empty-tables";
  static final String OPTION_LOAD_ROW_COUNTS = "load-row-counts";

  TableRowCountsCatalogLoader(final PropertyName catalogLoaderName) {
    super(catalogLoaderName, 2);
  }

  @Override
  public void execute() {
    if (!isLoaded()) {
      return;
    }

    LOGGER.log(Level.INFO, "Retrieving table row counts");
    try (final TaskRunner taskRunner = TaskRunners.getTaskRunner("loadTableRowCounts", 1); ) {
      final Catalog catalog = getCatalog();
      final TableRowCountsRetriever rowCountsRetriever =
          new TableRowCountsRetriever(getConnectionSource(), catalog);
      final TableRowCountsCatalogLoaderOptions commandOptions = getCommandOptions();

      final boolean loadRowCounts = commandOptions.loadRowCounts();
      if (!loadRowCounts) {
        LOGGER.log(Level.INFO, "Not retrieving table row counts, since this was not requested");
        return;
      }
      taskRunner.add(
          new TaskDefinition(
              "retrieveTableRowCounts", () -> rowCountsRetriever.retrieveTableRowCounts()));
      taskRunner.submit();

      final boolean noEmptyTables = commandOptions.noEmptyTables();
      if (!noEmptyTables) {
        LOGGER.log(Level.INFO, "Not removing empty tables");
        return;
      }
      taskRunner.add(
          new TaskDefinition(
              "filterEmptyTables",
              () ->
                  catalog.reduce(
                      Table.class, getTableReducer(new TableRowCountsFilter(noEmptyTables)))));
      taskRunner.submit();

      LOGGER.log(Level.INFO, taskRunner.report());
    } catch (final Exception e) {
      throw new ExecutionRuntimeException("Exception retrieving table row counts", e);
    }
  }

  @Override
  public boolean usesConnection() {
    return true;
  }
}
