/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.ermodel.implicitassociations;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.ermodel.associations.ImplicitAssociationsAnalyzer;
import schemacrawler.ermodel.associations.ImplicitAssociationsAnalyzerBuilder;
import schemacrawler.ermodel.associations.ImplicitColumnReference;
import schemacrawler.ermodel.implementation.ImplicitAssociationBuilder;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.exceptions.ExecutionRuntimeException;
import schemacrawler.tools.loader.ermodel.AbstractERModelLoader;
import us.fatehi.utility.property.PropertyName;
import us.fatehi.utility.scheduler.TaskDefinition;
import us.fatehi.utility.scheduler.TaskRunner;
import us.fatehi.utility.scheduler.TaskRunners;
import us.fatehi.utility.string.StringFormat;

/**
 * ER model loader that discovers implicit associations between tables and adds them to the ER
 * model.
 *
 * <p>This loader runs after the primary ER model has been built and enriches it with implicit
 * relationships inferred from naming patterns across all tables.
 */
final class ImplicitAssociationsERModelLoader
    extends AbstractERModelLoader<ImplicitAssociationsERModelLoaderOptions> {

  private static final Logger LOGGER =
      Logger.getLogger(ImplicitAssociationsERModelLoader.class.getName());

  static final String OPTION_IMPLICIT_ASSOCIATIONS = "implicit-associations";

  ImplicitAssociationsERModelLoader(final PropertyName loaderName) {
    super(loaderName);
  }

  @Override
  public void execute() {
    if (!hasERModel()) {
      LOGGER.log(Level.INFO, "ER model not available; skipping implicit association loading");
      return;
    }

    final ImplicitAssociationsERModelLoaderOptions commandOptions = getCommandOptions();
    if (!commandOptions.loadImplicitAssociations()) {
      LOGGER.log(
          Level.INFO, "Not loading implicit associations into ER model, since not requested");
      return;
    }

    LOGGER.log(Level.INFO, "Loading implicit associations into ER model");

    try (final TaskRunner taskRunner = TaskRunners.getTaskRunner("loadImplicitAssociations", 1)) {
      taskRunner.add(
          new TaskDefinition("loadImplicitAssociations", this::loadImplicitAssociations));
      taskRunner.submit();
      LOGGER.log(Level.INFO, taskRunner.report());
    } catch (final Exception e) {
      throw new ExecutionRuntimeException(
          "Exception loading implicit association information into ER model", e);
    }
  }

  private void loadImplicitAssociations() {
    final Catalog catalog = getCatalog();
    final ERModel erModel = getERModel();
    final ImplicitAssociationsAnalyzer implicitAssociationsAnalyzer =
        ImplicitAssociationsAnalyzerBuilder.builder(catalog.getTables())
            .withIdMatcher()
            .withExtensionTableMatcher()
            .build();
    final Collection<ImplicitColumnReference> implicitAssociations =
        implicitAssociationsAnalyzer.analyzeTables();
    if (implicitAssociations == null || implicitAssociations.isEmpty()) {
      return;
    }

    final ImplicitAssociationBuilder builder = ImplicitAssociationBuilder.builder(erModel);
    for (final ImplicitColumnReference implicitAssociation : implicitAssociations) {
      LOGGER.log(
          Level.INFO,
          new StringFormat("Adding implicit association <%s> to ER model", implicitAssociation));
      builder.addImplicitAssociation(implicitAssociation);
    }
  }
}
