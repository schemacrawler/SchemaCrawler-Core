/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.catalog;

import schemacrawler.tools.command.CommandProvider;
import schemacrawler.tools.options.Config;

public interface CatalogLoaderProvider extends CommandProvider {

  CatalogLoader<?> newCommand(Config config);
}
