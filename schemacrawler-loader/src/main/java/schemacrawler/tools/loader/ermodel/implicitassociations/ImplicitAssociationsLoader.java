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
import schemacrawler.ermodel.associations.ImplicitAssociationAnalyzer;
import schemacrawler.ermodel.associations.ImplicitAssociationAnalyzerBuilder;
import schemacrawler.ermodel.implementation.ImplicitRelationshipBuilder;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schemacrawler.exceptions.ExecutionRuntimeException;
import schemacrawler.tools.loader.ermodel.AbstractERModelLoader;
import us.fatehi.utility.property.PropertyName;
import us.fatehi.utility.scheduler.TaskDefinition;
import us.fatehi.utility.scheduler.TaskRunner;
import us.fatehi.utility.scheduler.TaskRunners;

/**
 * ER model loader that discovers implicit associations between tables and adds them to the ER
 * model.
 *
 * <p>This loader runs after the primary ER model has been built and enriches it with implicit
 * relationships inferred from naming patterns across all tables.
 */
final class ImplicitAssociationsLoader
    extends AbstractERModelLoader<ImplicitAssociationsLoaderOptions> {

  private static final Logger LOGGER = Logger.getLogger(ImplicitAssociationsLoader.class.getName());

  ImplicitAssociationsLoader(final PropertyName loaderName) {
    super(loaderName);
  }

  @Override
  public void execute() {
    if (!hasERModel()) {
      LOGGER.log(Level.INFO, "ER model not available; skipping implicit association loading");
      return;
    }

    final ImplicitAssociationsLoaderOptions commandOptions = getCommandOptions();
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

  /**
   * The ER model is enhanced with implicit associations, so no need to set it back into the
   * execution state
   */
  private void loadImplicitAssociations() {
    final Catalog catalog = getCatalog();
    final ERModel erModel = getERModel();

    final ImplicitAssociationAnalyzer associationAnalyzer =
        ImplicitAssociationAnalyzerBuilder.completeBuilder(erModel.getTables()).build();

    final Collection<ColumnReference> implicitAssociations = associationAnalyzer.analyzeTables();
    if (implicitAssociations == null || implicitAssociations.isEmpty()) {
      return;
    }

    final ImplicitRelationshipBuilder implicitRelationshipBuilder =
        ImplicitRelationshipBuilder.builder(catalog, erModel);

    // Implicit associations have only one column reference each, so convert each
    // column reference to a relationship
    for (final ColumnReference implicitAssociation : implicitAssociations) {
      implicitRelationshipBuilder.addColumnReference(
          implicitAssociation.getForeignKeyColumn(), implicitAssociation.getPrimaryKeyColumn());
      implicitRelationshipBuilder.build();
    }
  }
}
