/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.ermodel;

import us.fatehi.utility.property.PropertyName;

/** Provider for {@link SchemaCrawlerERModelLoader}. */
public class SchemaCrawlerERModelLoaderProvider extends BaseERModelLoaderProvider {

  private static final PropertyName NAME =
      new PropertyName("schemacrawlerermodelloader", "Loader for SchemaCrawler ERModel");

  @Override
  public PropertyName getLoaderName() {
    return NAME;
  }

  @Override
  public SchemaCrawlerERModelLoader newLoader() {
    return new SchemaCrawlerERModelLoader(NAME);
  }
}
