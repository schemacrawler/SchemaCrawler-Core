/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.catalog.counts;

import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.loader.catalog.AbstractCatalogLoader;
import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.exceptions.ExecutionRuntimeException;
import us.fatehi.utility.property.PropertyName;
import us.fatehi.utility.scheduler.TaskDefinition;
import us.fatehi.utility.scheduler.TaskRunner;
import us.fatehi.utility.scheduler.TaskRunners;

public class TableRowCountsLoader extends AbstractCatalogLoader<TableRowCountsLoaderOptions> {

  private static final Logger LOGGER = Logger.getLogger(TableRowCountsLoader.class.getName());

  TableRowCountsLoader(final PropertyName catalogLoaderName) {
    super(catalogLoaderName);
  }

  @Override
  public void execute() {
    if (!hasCatalog()) {
      return;
    }

    LOGGER.log(Level.INFO, "Retrieving table row counts");
    try (final TaskRunner taskRunner = TaskRunners.getTaskRunner("loadTableRowCounts")) {
      final Catalog catalog = getCatalog();
      final TableRowCountsRetriever rowCountsRetriever =
          new TableRowCountsRetriever(getConnectionSource(), catalog);
      final TableRowCountsLoaderOptions commandOptions = getCommandOptions();

      final boolean loadRowCounts = commandOptions.loadRowCounts();
      if (!loadRowCounts) {
        LOGGER.log(Level.INFO, "Not retrieving table row counts, since this was not requested");
        return;
      }
      taskRunner.add(
          new TaskDefinition(
              "retrieveTableRowCounts", () -> rowCountsRetriever.retrieveTableRowCounts()));
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
