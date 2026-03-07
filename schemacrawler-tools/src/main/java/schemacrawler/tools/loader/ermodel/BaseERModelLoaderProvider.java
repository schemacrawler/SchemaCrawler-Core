/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.ermodel;

import java.util.Collection;
import java.util.List;

/** Abstract base class for ERModel loader providers. */
public abstract class BaseERModelLoaderProvider implements ERModelLoaderProvider {

  @Override
  public final Collection<us.fatehi.utility.property.PropertyName> getSupportedLoaders() {
    return List.of(getLoaderName());
  }
}
