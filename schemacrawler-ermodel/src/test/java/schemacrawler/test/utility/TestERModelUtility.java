/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import schemacrawler.ermodel.associations.ImplicitAssociationsAnalyzer;
import schemacrawler.ermodel.associations.ImplicitAssociationsAnalyzerBuilder;
import schemacrawler.ermodel.implementation.ERModelBuilder;
import schemacrawler.ermodel.implementation.ImplicitRelationshipBuilder;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ColumnReference;
import us.fatehi.utility.UtilityMarker;

/** Utility for inferring entity model information from tables and foreign keys. */
@UtilityMarker
public class TestERModelUtility {

  public static ERModel buildERModel(final Catalog catalog) {
    requireNonNull(catalog, "No catalog provided");
    final ERModel erModel = ERModelBuilder.builder(catalog).build();
    loadImplicitAssociations(catalog, erModel);
    return erModel;
  }

  private static void loadImplicitAssociations(final Catalog catalog, final ERModel erModel) {

    final ImplicitAssociationsAnalyzer implicitAssociationsAnalyzer =
        ImplicitAssociationsAnalyzerBuilder.completeBuilder(erModel.getTables()).build();

    final Collection<ColumnReference> implicitAssociations =
        implicitAssociationsAnalyzer.analyzeTables();
    final ImplicitRelationshipBuilder implicitRelationshipBuilder =
        ImplicitRelationshipBuilder.builder(catalog, erModel);
    for (final ColumnReference implicitAssociation : implicitAssociations) {
      implicitRelationshipBuilder.addColumnReference(
          implicitAssociation.getForeignKeyColumn(), implicitAssociation.getPrimaryKeyColumn());
      implicitRelationshipBuilder.build();
    }
  }

  private TestERModelUtility() {
    // Prevent instantiation
  }
}
