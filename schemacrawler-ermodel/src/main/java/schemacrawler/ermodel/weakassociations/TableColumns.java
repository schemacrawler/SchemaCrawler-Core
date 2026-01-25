/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.weakassociations;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Index;
import schemacrawler.schema.IndexColumn;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.PrimaryKey;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableConstraintColumn;

/**
 * Provides candidate key columns used for weak association inference.
 *
 * <p>To ensure high-confidence weak associations, this only considers single-column keys:
 *
 * <ul>
 *   <li>Single-column primary keys are included.
 *   <li>Single-column unique indexes are included.
 * </ul>
 */
final class TableColumns {

  private final Table table;
  private final Set<Column> candidateKeys;
  private final Set<Column> importedForeignKeys;

  TableColumns(final Table table) {
    this.table = requireNonNull(table, "No table provided");

    candidateKeys = new HashSet<>();
    importedForeignKeys = new HashSet<>();

    buildLookups();
  }

  public Set<Column> getCandidateKeys() {
    return Set.copyOf(candidateKeys);
  }

  public Table getTable() {
    return table;
  }

  public boolean hasImportedForeignKey(final Column fkColumn) {
    if (fkColumn == null) {
      return false;
    }
    for (final Column importedForeignKeyColumn : importedForeignKeys) {
      if (importedForeignKeyColumn.key().equals(fkColumn.key())) {
        return true;
      }
    }
    return false;
  }

  public NamedObjectKey key() {
    return table.key();
  }

  @Override
  public String toString() {
    return "%s: %s".formatted(table, candidateKeys);
  }

  private void buildLookups() {
    final PrimaryKey primaryKey = table.getPrimaryKey();
    if (primaryKey != null && primaryKey.getConstrainedColumns().size() == 1) {
      final TableConstraintColumn tableConstraintColumn = primaryKey.getConstrainedColumns().get(0);
      candidateKeys.add(tableConstraintColumn);
    }

    for (final Index index : table.getIndexes()) {
      if (index != null && index.isUnique() && index.getColumns().size() == 1) {
        final IndexColumn indexColumn = index.getColumns().get(0);
        candidateKeys.add(indexColumn);
      }
    }

    for (final ForeignKey foreignKey : table.getImportedForeignKeys()) {
      final List<ColumnReference> columnReferences = foreignKey.getColumnReferences();
      if (columnReferences.size() == 1) {
        final Column fkColumn = columnReferences.get(0).getForeignKeyColumn();
        importedForeignKeys.add(fkColumn);
      }
    }
  }
}
