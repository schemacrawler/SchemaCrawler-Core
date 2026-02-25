/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.catalogloader;

import schemacrawler.tools.executable.CommandProvider;
import schemacrawler.tools.options.Config;

public interface CatalogLoaderProvider extends CommandProvider {

  @Override
  CatalogLoader<?> newCommand(String command, Config config);
}
