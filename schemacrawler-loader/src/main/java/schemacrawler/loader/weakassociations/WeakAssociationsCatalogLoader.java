/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.weakassociations;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.crawl.WeakAssociationBuilder;
import schemacrawler.crawl.WeakAssociationBuilder.WeakAssociationColumn;
import schemacrawler.ermodel.associations.ImplicitAssociationsAnalyzer;
import schemacrawler.ermodel.associations.ImplicitAssociationsAnalyzerBuilder;
import schemacrawler.ermodel.associations.ImplicitColumnReference;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schemacrawler.exceptions.ExecutionRuntimeException;
import schemacrawler.tools.catalogloader.BaseCatalogLoader;
import schemacrawler.tools.executable.commandline.PluginCommand;
import schemacrawler.tools.options.Config;
import us.fatehi.utility.property.PropertyName;
import us.fatehi.utility.scheduler.TaskDefinition;
import us.fatehi.utility.scheduler.TaskRunner;
import us.fatehi.utility.scheduler.TaskRunners;
import us.fatehi.utility.string.StringFormat;

/**
 * Catalog loader that infers weak associations by applying a composite rule (ID-based matching and
 * optional extension-table matching) across all tables and adds the results to the catalog.
 *
 * <p>This is a {@link PluginCommand} that provides options to enable weak association discovery.
 * Enabling these options can have a significant performance impact on schema loading, as it
 * involves analyzing naming patterns across all tables.
 */
public final class WeakAssociationsCatalogLoader extends BaseCatalogLoader {

  private static final Logger LOGGER =
      Logger.getLogger(WeakAssociationsCatalogLoader.class.getName());

  private static final String OPTION_WEAK_ASSOCIATIONS = "weak-associations";
  private static final String OPTION_INFER_EXTENSION_TABLES = "infer-extension-tables";

  public WeakAssociationsCatalogLoader() {
    super(new PropertyName("weakassociationsloader", "Loader for weak associations"), 3);
  }

  @Override
  public PluginCommand getCommandLineCommand() {
    final PropertyName catalogLoaderName = getCommandName();
    final PluginCommand pluginCommand = PluginCommand.newCatalogLoaderCommand(catalogLoaderName);
    pluginCommand.addOption(
        OPTION_WEAK_ASSOCIATIONS,
        Boolean.class,
        "Analyzes the schema to find weak associations between tables, based on table and column"
            + " naming patterns",
        "This can be a time consuming operation",
        "Optional, defaults to false");
    pluginCommand.addOption(
        OPTION_INFER_EXTENSION_TABLES,
        Boolean.class,
        "Infers extension tables that have similarly named primary keys, and reports them as weak"
            + " associations",
        "Optional, defaults to false");
    return pluginCommand;
  }

  @Override
  public void execute() {
    if (!isLoaded()) {
      return;
    }

    LOGGER.log(Level.INFO, "Finding weak associations");
    try (final TaskRunner taskRunner = TaskRunners.getTaskRunner("loadWeakAssociations", 1); ) {
      taskRunner.add(
          new TaskDefinition(
              "retrieveWeakAssociations",
              () -> {
                final Config config = getAdditionalConfiguration();
                final boolean findWeakAssociations =
                    config.getBooleanValue(OPTION_WEAK_ASSOCIATIONS, false);
                final boolean inferExtensionTables =
                    config.getBooleanValue(OPTION_INFER_EXTENSION_TABLES, false);
                if (findWeakAssociations) {
                  findWeakAssociations(inferExtensionTables);
                } else {
                  LOGGER.log(
                      Level.INFO, "Not retrieving weak associations, since this was not requested");
                }
              }));
      taskRunner.submit();
      LOGGER.log(Level.INFO, taskRunner.report());
    } catch (final Exception e) {
      throw new ExecutionRuntimeException("Exception retrieving weak association information", e);
    }
  }

  private void findWeakAssociations(final boolean inferExtensionTables) {

    final Catalog catalog = getCatalog();
    final ImplicitAssociationsAnalyzerBuilder analyzerBuilder =
        ImplicitAssociationsAnalyzerBuilder.builder(catalog.getTables()).withIdMatcher();
    if (inferExtensionTables) {
      analyzerBuilder.withExtensionTableMatcher();
    }

    final ImplicitAssociationsAnalyzer implicitAssociationsAnalyzer = analyzerBuilder.build();
    final Collection<ImplicitColumnReference> weakAssociations =
        implicitAssociationsAnalyzer.analyzeTables();

    for (final ColumnReference weakAssociation : weakAssociations) {
      LOGGER.log(Level.INFO, new StringFormat("Adding weak association <%s> ", weakAssociation));

      final Column fkColumn = weakAssociation.getForeignKeyColumn();
      final Column pkColumn = weakAssociation.getPrimaryKeyColumn();

      final WeakAssociationBuilder builder = WeakAssociationBuilder.builder(catalog);
      builder.addColumnReference(
          new WeakAssociationColumn(fkColumn), new WeakAssociationColumn(pkColumn));
      builder.build();
    }
  }
}
