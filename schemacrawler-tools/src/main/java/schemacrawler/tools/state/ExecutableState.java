/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.state;

import schemacrawler.ermodel.model.ERModel;
import schemacrawler.schema.Catalog;
import us.fatehi.utility.datasource.DatabaseConnectionSource;

public interface ExecutableState {

  void clearConnectionSource();

  Catalog getCatalog();

  DatabaseConnectionSource getConnectionSource();

  ERModel getERModel();

  boolean hasCatalog();

  boolean hasConnectionSource();

  boolean hasERModel();

  void setCatalog(Catalog catalog);

  void setConnectionSource(DatabaseConnectionSource connectionSource);

  void setERModel(ERModel erModel);
}
