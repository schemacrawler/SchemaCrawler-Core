/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.executable;

import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.options.OutputOptions;

public interface SchemaCrawlerCommandProvider extends CommandProvider {

  boolean supportsOutputFormat(String command, OutputOptions outputOptions);

  boolean supportsSchemaCrawlerCommand(
      String command,
      SchemaCrawlerOptions schemaCrawlerOptions,
      Config additionalConfig,
      OutputOptions outputOptions);
}
