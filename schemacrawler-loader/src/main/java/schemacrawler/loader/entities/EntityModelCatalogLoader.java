/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.entities;

import static schemacrawler.schemacrawler.SchemaInfoRetrieval.retrieveIndexes;
import static schemacrawler.schemacrawler.SchemaInfoRetrieval.retrievePrimaryKeys;
import static schemacrawler.schemacrawler.SchemaInfoRetrieval.retrieveTableColumns;
import static schemacrawler.schemacrawler.SchemaInfoRetrieval.retrieveTables;

import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.crawl.EntityModelBuilder;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.ForeignKeyCardinality;
import schemacrawler.schema.PartialDatabaseObject;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaInfoLevel;
import schemacrawler.schemacrawler.exceptions.ExecutionRuntimeException;
import schemacrawler.tools.catalogloader.BaseCatalogLoader;
import schemacrawler.tools.executable.commandline.PluginCommand;
import schemacrawler.tools.options.Config;
import us.fatehi.utility.property.PropertyName;
import us.fatehi.utility.scheduler.TaskDefinition;
import us.fatehi.utility.scheduler.TaskRunner;
import us.fatehi.utility.scheduler.TaskRunners;

public final class EntityModelCatalogLoader extends BaseCatalogLoader {

  private static final Logger LOGGER = Logger.getLogger(EntityModelCatalogLoader.class.getName());

  private static final String OPTION_ENTITY_MODELS = "entity-models";

  public EntityModelCatalogLoader() {
    super(new PropertyName("entitymodelsloader", "Loader for modeling entities"), 1);
  }

  @Override
  public PluginCommand getCommandLineCommand() {
    final PropertyName catalogLoaderName = getCatalogLoaderName();
    final PluginCommand pluginCommand = PluginCommand.newCatalogLoaderCommand(catalogLoaderName);
    pluginCommand.addOption(
        OPTION_ENTITY_MODELS,
        Boolean.class,
        "Analyzes the schema to identify entity models",
        "This can be a time consuming operation",
        "Optional, defaults to false");
    return pluginCommand;
  }

  @Override
  public void loadCatalog() {
    if (!isLoaded()) {
      return;
    }

    LOGGER.log(Level.INFO, "Identifying entity models");
    try (final TaskRunner taskRunner = TaskRunners.getTaskRunner("identifyEntityModels", 1)) {
      taskRunner.add(
          new TaskDefinition(
              "identifyEntityModels",
              () -> {
                final SchemaInfoLevel schemaInfoLevel =
                    getSchemaCrawlerOptions().loadOptions().schemaInfoLevel();
                final boolean hasData =
                    schemaInfoLevel.is(retrieveTables)
                        && schemaInfoLevel.is(retrieveTableColumns)
                        && schemaInfoLevel.is(retrievePrimaryKeys)
                        && schemaInfoLevel.is(retrieveIndexes);

                final Config config = getAdditionalConfiguration();
                // Default identify entity models to true
                final boolean identifyEntityModels =
                    config.getBooleanValue(OPTION_ENTITY_MODELS, true);

                if (hasData && identifyEntityModels) {
                  identifyEntityModels();
                } else {
                  LOGGER.log(
                      Level.INFO, "Not identifying entity models, since this was not requested");
                }
              }));
      taskRunner.submit();
      LOGGER.log(Level.INFO, taskRunner.report());
    } catch (final Exception e) {
      throw new ExecutionRuntimeException("Exception identifying entities", e);
    }
  }

  private void identifyEntityModels() {
    final EntityModelBuilder modelBuilder = EntityModelBuilder.builder();
    for (final Table table : getCatalog().getTables()) {
      if (table instanceof PartialDatabaseObject) {
        continue;
      }
      final TableEntityModel tableEntityModel = new TableEntityModel(table);

      for (final ForeignKey fk : table.getImportedForeignKeys()) {
        final ForeignKeyCardinality fkCardinality = tableEntityModel.inferForeignKeyCardinality(fk);
        modelBuilder.updateForeignKeyCardinality(fk, fkCardinality);
      }
    }
  }
}
