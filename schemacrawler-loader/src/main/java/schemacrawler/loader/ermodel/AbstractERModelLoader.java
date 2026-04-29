/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.ermodel;

import schemacrawler.tools.command.AbstractCommand;
import schemacrawler.tools.command.CommandOptions;
import us.fatehi.utility.property.PropertyName;

/** Abstract base class for ERModel loaders. */
public abstract class AbstractERModelLoader<P extends CommandOptions> extends AbstractCommand<P>
    implements ERModelLoader<P> {

  protected AbstractERModelLoader(final PropertyName loaderName) {
    super(loaderName);
  }

  @Override
  public void initialize() {
    // Default no-op stub
  }

  @Override
  public String toString() {
    return getCommandName().toString();
  }
}
