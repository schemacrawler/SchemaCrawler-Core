/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import static java.util.Objects.requireNonNull;

import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;

public class LightColumnReference implements ColumnReference {

  private static final long serialVersionUID = 8536957732822472828L;

  private final Column fkColumn;
  private final Column pkColumn;

  public LightColumnReference(final Column fkColumn, final Column pkColumn) {
    this.fkColumn = requireNonNull(fkColumn, "No foreign key column provided");
    this.pkColumn = requireNonNull(pkColumn, "No primary key column provided");
  }

  @Override
  public int compareTo(final ColumnReference o) {
    return 0;
  }

  @Override
  public Column getForeignKeyColumn() {
    return fkColumn;
  }

  @Override
  public int getKeySequence() {
    return 0;
  }

  @Override
  public Column getPrimaryKeyColumn() {
    return pkColumn;
  }

  @Override
  public boolean isSelfReferencing() {
    return pkColumn.equals(fkColumn);
  }
}
