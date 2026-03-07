/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.loader.ermodel;

import schemacrawler.ermodel.model.ERModel;
import schemacrawler.tools.executable.AbstractCommand;
import schemacrawler.tools.executable.CommandOptions;
import us.fatehi.utility.property.PropertyName;

/** Abstract base class for ERModel loaders. */
public abstract class AbstractERModelLoader<P extends CommandOptions> extends AbstractCommand<P>
    implements ERModelLoader<P> {

  protected final int priority;
  protected ERModel erModel;

  protected AbstractERModelLoader(final PropertyName loaderName, final int priority) {
    super(loaderName);
    this.priority = priority;
  }

  @Override
  public final ERModel getERModel() {
    return erModel;
  }

  @Override
  public final int getPriority() {
    return priority;
  }

  @Override
  public void initialize() {
    // Default no-op stub
  }

  @Override
  public final void setERModel(final ERModel erModel) {
    this.erModel = erModel;
  }

  @Override
  public String toString() {
    return getCommandName().toString();
  }

  /** Returns whether an ERModel has already been loaded. */
  protected final boolean isLoaded() {
    return erModel != null;
  }
}
