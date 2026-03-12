/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.ermodel;

import schemacrawler.ermodel.model.ERModel;
import schemacrawler.tools.command.CommandOptions;
import schemacrawler.tools.command.ExecutableCommand;

/** A loader that builds or enriches an ERModel from a catalog. */
public interface ERModelLoader<P extends CommandOptions> extends ExecutableCommand<P> {

  /**
   * Returns the ERModel produced by this loader.
   *
   * @return ERModel, or null if not yet loaded
   */
  @Override
  ERModel getERModel();

  /**
   * Sets an existing ERModel to be enriched. If null, a new ERModel will be built.
   *
   * @param erModel Existing ERModel to enrich, or null
   */
  @Override
  void setERModel(ERModel erModel);
}
