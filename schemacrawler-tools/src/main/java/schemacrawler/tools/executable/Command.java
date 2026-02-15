/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.executable;

import java.sql.Connection;
import java.util.concurrent.Callable;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.schema.Catalog;
import us.fatehi.utility.property.PropertyName;

/** A SchemaCrawler executable unit. */
public interface Command<P, R> extends Callable<R> {

  void configure(P parameters);

  Catalog getCatalog();

  PropertyName getCommandName();

  Connection getConnection();

  ERModel getERModel();

  /** Initializes the command for execution. */
  void initialize();

  void setCatalog(Catalog catalog);

  void setConnection(Connection connection);

  void setERModel(ERModel erModel);

  default boolean usesConnection() {
    return false;
  }
}
