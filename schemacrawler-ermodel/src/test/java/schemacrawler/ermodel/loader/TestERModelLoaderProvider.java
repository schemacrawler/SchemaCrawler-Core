/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.loader;

import us.fatehi.utility.property.PropertyName;

public class TestERModelLoaderProvider extends BaseERModelLoaderProvider {

  private static final PropertyName NAME =
      new PropertyName("testermodelloader", "Loader for testing ERModel");

  public TestERModelLoaderProvider() {
    forceInstantiationFailureIfConfigured();
  }

  @Override
  public PropertyName getLoaderName() {
    return NAME;
  }

  @Override
  public TestERModelLoader newLoader() {
    forceLoadFailureIfConfigured();
    return new TestERModelLoader(NAME);
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
      throw new RuntimeException("Forced load error");
    }
  }
}
