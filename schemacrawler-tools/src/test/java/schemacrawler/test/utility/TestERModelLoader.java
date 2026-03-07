/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility;

import schemacrawler.tools.loader.ermodel.AbstractERModelLoader;
import us.fatehi.utility.SystemExitException;
import us.fatehi.utility.property.PropertyName;

class TestERModelLoader extends AbstractERModelLoader {

  TestERModelLoader(final PropertyName loaderName) {
    super(loaderName, 3);
  }

  @Override
  public void execute() {
    forceLoadFailureIfConfigured();
    // No-op for testing: does not build an ERModel
  }

  private void forceLoadFailureIfConfigured() {
    final String key = this.getClass().getName() + ".force-load-failure";
    final String propertyValue = System.getProperty(key);
    if (propertyValue != null) {
      throw new SystemExitException(2, key);
    }
  }
}
