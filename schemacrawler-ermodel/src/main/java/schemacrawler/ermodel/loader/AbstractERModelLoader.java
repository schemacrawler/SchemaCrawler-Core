/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.loader;

import static java.util.Objects.requireNonNull;

import schemacrawler.ermodel.model.ERModel;
import schemacrawler.schema.Catalog;
import us.fatehi.utility.property.PropertyName;

/** Abstract base class for ERModel loaders. */
public abstract class AbstractERModelLoader implements ERModelLoader {

  protected final PropertyName loaderName;
  protected final int priority;
  protected Catalog catalog;
  protected ERModel erModel;

  protected AbstractERModelLoader(final PropertyName loaderName, final int priority) {
    this.loaderName = requireNonNull(loaderName, "No loader name provided");
    this.priority = priority;
  }

  @Override
  public final Catalog getCatalog() {
    return catalog;
  }

  @Override
  public final ERModel getERModel() {
    return erModel;
  }

  @Override
  public final PropertyName getLoaderName() {
    return loaderName;
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
  public final void setCatalog(final Catalog catalog) {
    this.catalog = requireNonNull(catalog, "No catalog provided");
  }

  @Override
  public final void setERModel(final ERModel erModel) {
    this.erModel = erModel;
  }

  @Override
  public String toString() {
    return loaderName.toString();
  }

  /** Returns whether an ERModel has already been loaded. */
  protected final boolean isLoaded() {
    return erModel != null;
  }
}
