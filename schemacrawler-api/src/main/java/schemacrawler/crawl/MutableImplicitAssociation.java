/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import java.io.Serial;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.TableConstraintType;

/** Represents a foreign-key mapping to a primary key in another table. */
final class MutableImplicitAssociation extends AbstractTableReference {

  @Serial private static final long serialVersionUID = -5164664131926303038L;

  public MutableImplicitAssociation(final String name, final ColumnReference columnReference) {
    super(name, columnReference);
  }

  @Override
  public String getDefinition() {
    return "";
  }

  @Override
  public TableConstraintType getType() {
    return TableConstraintType.implicit_association;
  }

  @Override
  public boolean isDeferrable() {
    return false;
  }

  @Override
  public boolean isInitiallyDeferred() {
    return false;
  }
}
