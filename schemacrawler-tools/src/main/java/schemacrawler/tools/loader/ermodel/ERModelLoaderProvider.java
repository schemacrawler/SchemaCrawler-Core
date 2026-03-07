/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.ermodel;

import java.util.Collection;
import us.fatehi.utility.property.PropertyName;

/** Provider interface for creating ERModel loaders. */
public interface ERModelLoaderProvider {

  /**
   * Returns the name of the loader provided.
   *
   * @return Loader name
   */
  PropertyName getLoaderName();

  /**
   * Returns the names of all loaders supported by this provider.
   *
   * @return Supported loader names
   */
  Collection<PropertyName> getSupportedLoaders();

  /**
   * Creates a new ERModel loader.
   *
   * @return New ERModel loader instance
   */
  ERModelLoader newLoader();
}
