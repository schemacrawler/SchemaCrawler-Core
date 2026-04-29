/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.ermodel;

import schemacrawler.tools.command.CommandProvider;
import schemacrawler.tools.options.Config;

/** Provider interface for creating ERModel loaders. */
public interface ERModelLoaderProvider extends CommandProvider {

  /**
   * Creates a new ERModel loader.
   *
   * @return New ERModel loader instance
   */
  ERModelLoader<?> newCommand(Config config);
}
