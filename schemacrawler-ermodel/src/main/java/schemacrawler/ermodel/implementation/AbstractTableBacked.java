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
import java.util.List;
import java.util.stream.Collectors;
import schemacrawler.ermodel.model.EntityAttribute;
import schemacrawler.ermodel.model.TableBacked;
import schemacrawler.schema.Table;

public class AbstractTableBacked extends AbstractDatabaseObjectBacked<Table>
    implements TableBacked {

  @Serial private static final long serialVersionUID = 7423406592008806690L;

  private final Collection<EntityAttribute> entityAttributes;

  public AbstractTableBacked(final Table table) {
    super(table);

    entityAttributes =
        getDatabaseObject().getColumns().stream()
            .filter(column -> !column.isPartOfPrimaryKey() && !column.isPartOfForeignKey())
            .map(column -> new MutableEntityAttribute(this, column))
            .collect(Collectors.toList());
  }

  @Override
  public Collection<EntityAttribute> getEntityAttributes() {
    return List.copyOf(entityAttributes);
  }

  @Override
  public Table getTable() {
    return getDatabaseObject();
  }
}
