/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility;

import static java.util.Objects.requireNonNull;

import schemacrawler.ermodel.implementation.ERModelBuilder;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.schema.Catalog;
import schemacrawler.tools.state.AbstractExecutionState;

/** Minimal execution state, holding just a catalog and an optional ER model, for tests. */
final class StubExecutionState extends AbstractExecutionState {

  StubExecutionState(final Catalog catalog) {
    this(catalog, ERModelBuilder.builder(catalog).build());
  }

  StubExecutionState(final Catalog catalog, final ERModel erModel) {
    requireNonNull(catalog, "No catalog provided");
    requireNonNull(erModel, "No ER model provided");
    setCatalog(catalog);
    setERModel(erModel);
  }
}
