/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility;

import static java.util.Objects.requireNonNull;

import schemacrawler.ermodel.implementation.ERModelBuilder;
import schemacrawler.ermodel.implementation.ImplicitAssociationBuilder;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.schema.Catalog;
import us.fatehi.utility.UtilityMarker;

/** Utility for inferring entity model information from tables and foreign keys. */
@UtilityMarker
public class TestERModelUtility {

  public static ERModel buildERModel(final Catalog catalog) {
    requireNonNull(catalog, "No catalog provided");
    final ERModel erModel = new ERModelBuilder(catalog).build();
    ImplicitAssociationBuilder.builder(erModel);
    return erModel;
  }

  private TestERModelUtility() {
    // Prevent instantiation
  }
}
