/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility;

import schemacrawler.test.utility.TestCatalogLoader.TestCatalogLoaderOptions;
import schemacrawler.tools.catalogloader.BaseCatalogLoader;
import schemacrawler.tools.executable.CommandOptions;
import us.fatehi.utility.SystemExitException;
import us.fatehi.utility.property.PropertyName;

class TestCatalogLoader extends BaseCatalogLoader<TestCatalogLoaderOptions> {

  public static class TestCatalogLoaderOptions implements CommandOptions {}

  TestCatalogLoader(final PropertyName catalogLoaderName) {
    super(catalogLoaderName, 3);
    forceInstantiationFailureIfConfigured();
  }

  @Override
  public void execute() {
    forceLoadFailureIfConfigured();
  }

  private void forceInstantiationFailureIfConfigured() {
    final String propertyValue =
        System.getProperty(this.getClass().getName() + ".force-instantiation-failure");
    if (propertyValue != null) {
      throw new RuntimeException("Forced instantiation error");
    }
  }

  private void forceLoadFailureIfConfigured() {
    final String key = this.getClass().getName() + ".force-load-failure";
    final String propertyValue = System.getProperty(key);
    if (propertyValue != null) {
      throw new SystemExitException(2, key);
    }
  }
}
