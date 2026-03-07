/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility;

import java.util.Collection;
import java.util.List;
import schemacrawler.tools.loader.ermodel.BaseERModelLoaderProvider;
import schemacrawler.tools.options.Config;
import us.fatehi.utility.property.PropertyName;

public class TestERModelLoaderProvider extends BaseERModelLoaderProvider {

  private static final PropertyName NAME =
      new PropertyName("testmodelloader", "Loader for testing ERModel");

  public TestERModelLoaderProvider() {
    forceInstantiationFailureIfConfigured();
  }

  @Override
  public Collection<PropertyName> getSupportedCommands() {
    return List.of(NAME);
  }

  @Override
  public TestERModelLoader newCommand(final Config config) {
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
