/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.entities;

import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schemacrawler.exceptions.ExecutionRuntimeException;
import schemacrawler.tools.catalogloader.BaseCatalogLoader;
import us.fatehi.utility.property.PropertyName;
import us.fatehi.utility.scheduler.TaskDefinition;
import us.fatehi.utility.scheduler.TaskRunner;
import us.fatehi.utility.scheduler.TaskRunners;

public final class EntitiesLoader extends BaseCatalogLoader {

  private static final Logger LOGGER = Logger.getLogger(EntitiesLoader.class.getName());

  public EntitiesLoader() {
    super(new PropertyName("entitiesloader", "Loader for modeling entities"), 1);
  }

  @Override
  public void loadCatalog() {
    if (!isLoaded()) {
      return;
    }

    LOGGER.log(Level.INFO, "Identifying entities");
    try (final TaskRunner taskRunner = TaskRunners.getTaskRunner("identifyEntities", 1)) {
      taskRunner.add(
          new TaskDefinition(
              "identifyEntities",
              () -> {
                // TODO: identify entities
              }));
      taskRunner.submit();
      LOGGER.log(Level.INFO, taskRunner.report());
    } catch (final Exception e) {
      throw new ExecutionRuntimeException("Exception identifying entities", e);
    }
  }
}
