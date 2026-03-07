/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.loader;

import schemacrawler.ermodel.model.ERModel;
import schemacrawler.schema.Catalog;
import us.fatehi.utility.property.PropertyName;

/** A loader that builds or enriches an ERModel from a catalog. */
public interface ERModelLoader {

  /**
   * Executes the loader, producing or enriching an ERModel.
   *
   * <p>May throw runtime exceptions on errors.
   */
  void execute();

  /**
   * Returns the catalog used as input for loading.
   *
   * @return Catalog
   */
  Catalog getCatalog();

  /**
   * Returns the ERModel produced by this loader.
   *
   * @return ERModel, or null if not yet loaded
   */
  ERModel getERModel();

  /**
   * Returns the name of this loader.
   *
   * @return Loader name
   */
  PropertyName getLoaderName();

  /**
   * Returns the priority of this loader. Lower values run first.
   *
   * @return Priority
   */
  int getPriority();

  /**
   * Initializes the loader before execution. May throw runtime exceptions if pre-conditions are not
   * met.
   */
  void initialize();

  /**
   * Sets the catalog to use as input for loading.
   *
   * @param catalog Catalog to load the ERModel from
   */
  void setCatalog(Catalog catalog);

  /**
   * Sets an existing ERModel to be enriched. If null, a new ERModel will be built.
   *
   * @param erModel Existing ERModel to enrich, or null
   */
  void setERModel(ERModel erModel);
}
