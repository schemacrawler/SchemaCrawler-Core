/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.implementation;

import java.io.Serial;
import java.util.Collection;
import java.util.stream.Collectors;
import schemacrawler.ermodel.model.EntityAttribute;
import schemacrawler.ermodel.model.TableBacked;
import schemacrawler.schema.Table;

public class AbstractTableBacked extends AbstractDatabaseObjectBacked<Table>
    implements TableBacked {

  @Serial private static final long serialVersionUID = 7423406592008806690L;

  public AbstractTableBacked(final Table table) {
    super(table);
  }

  @Override
  public Collection<EntityAttribute> getEntityAttributes() {
    return getDatabaseObject().getColumns().stream()
        .filter(column -> !column.isPartOfPrimaryKey() && !column.isPartOfForeignKey())
        .map(MutableEntityAttribute::new)
        .collect(Collectors.toList());
  }

  @Override
  public Table getTable() {
    return getDatabaseObject();
  }
}
