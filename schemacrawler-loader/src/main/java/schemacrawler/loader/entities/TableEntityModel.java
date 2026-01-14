/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.entities;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.EntityType;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.ForeignKeyCardinality;
import schemacrawler.schema.Index;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.PartialDatabaseObject;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableReference;

public final class TableEntityModel {

  private final Table table;

  private final Set<Set<Column>> uniqueKeys;

  private final Set<ForeignKey> importedForeignKeys;
  private final Set<Column> tablePkColumns;

  private final Map<NamedObjectKey, Set<Column>> importedColumnsMap;
  private final Map<NamedObjectKey, Set<Column>> pkColumnsMap;
  private final Map<NamedObjectKey, Set<Column>> parentPkColumnsMap;

  public TableEntityModel(final Table table) {
    this.table = requireNonNull(table, "No table provided");
    if (table instanceof PartialDatabaseObject) {
      throw new IllegalArgumentException("Table cannot be partial");
    }

    uniqueKeys = new HashSet<>();

    importedForeignKeys = new HashSet<>();
    tablePkColumns = new HashSet<>();

    importedColumnsMap = new HashMap<>();
    pkColumnsMap = new HashMap<>();
    parentPkColumnsMap = new HashMap<>();

    buildSupportingLookups();
    buildUniquenessLookup();
  }

  public EntityType identifyEntityType() {

    // Step 1: Check for non-entity pattern: Non-entity tables do not have a primary
    // key.
    if (!table.hasPrimaryKey()) {
      return EntityType.non_entity;
    }

    for (final ForeignKey fk : importedForeignKeys) {
      final Set<Column> fkParentColumns = pkColumnsMap.get(fk.key());
      final Set<Column> parentPkColumns = parentPkColumnsMap.get(fk.key());
      final Set<Column> fkChildColumns = importedColumnsMap.get(fk.key());

      // Step 2: Check for subtype pattern: Subtype tables inherit their entire
      // primary key from a single supertype table.
      // If PK(T) exactly matches the child columns of a FK to a parent
      // table P primary key PK(P), classify T as SUBTYPE of P.
      if (!parentPkColumns.isEmpty()
          && parentPkColumns.equals(fkParentColumns)
          && tablePkColumns.equals(fkChildColumns)) {
        return EntityType.subtype;
      }

      // Step 3: Check for weak entity pattern: Weak entities combine a parent's full
      // primary key (via identifying FK) with their own discriminator column(s).
      // Else if PK(T) contains (as a proper subset) the child columns of some FK to
      // parent P primary key PK(P), classify T as WEAK_ENTITY owned by P.
      if (!parentPkColumns.isEmpty()
          && parentPkColumns.equals(fkParentColumns)
          && tablePkColumns.containsAll(fkChildColumns)
          && tablePkColumns.size() > fkChildColumns.size()) {
        return EntityType.weak_entity;
      }
    }

    // Step 4: Check for strong entity pattern: Strong entities have self-sufficient
    // primary keys (no FK columns in PK) and low referential connectivity to other
    // tables.
    // Else if no FK columns participate in PK(T) AND T has foreign keys to fewer
    // than 2 other tables (excluding self-references), classify T as
    // STRONG_ENTITY. (If there are 2 or more relationships, it may be a bridge
    // table.)
    final boolean pkHasFkColumn = tablePkColumns.stream().anyMatch(Column::isPartOfForeignKey);

    if (!pkHasFkColumn) {
      final Set<Table> referencedTables = new HashSet<>(table.getReferencedTables());
      // Self-references don't count towards the limit of 2 other tables
      referencedTables.remove(table);
      if (referencedTables.size() < 2) {
        return EntityType.strong_entity;
      }
    }

    // Step 5: Default classification: Tables with ambiguous patterns (high
    // connectivity, composite FK-based PKs, etc.) cannot be confidently
    // classified.
    return EntityType.unknown;
  }

  public ForeignKeyCardinality identifyForeignKeyCardinality(final TableReference fk) {
    ForeignKeyCardinality cardinality = ForeignKeyCardinality.unknown;
    if (fk == null) {
      return cardinality;
    }

    final Set<Column> importedColumns;
    if (!importedColumnsMap.containsKey(fk.key())) {
      importedColumns = getImportedColumns(fk);
    } else {
      importedColumns = importedColumnsMap.get(fk.key());
    }

    final boolean isForeignKeyUnique = uniqueKeys.contains(importedColumns);
    final boolean isForeignKeyOptional = fk.isOptional();

    if (isForeignKeyUnique) {
      if (isForeignKeyOptional) {
        cardinality = ForeignKeyCardinality.zero_one;
      } else {
        cardinality = ForeignKeyCardinality.one_one;
      }
    } else if (isForeignKeyOptional) {
      cardinality = ForeignKeyCardinality.zero_many;
    } else {
      cardinality = ForeignKeyCardinality.one_many;
    }

    return cardinality;
  }

  @Override
  public String toString() {
    return table.toString();
  }

  private void buildSupportingLookups() {
    // Foreign keys imported from other tables
    for (final ForeignKey fk : table.getImportedForeignKeys()) {
      if (!fk.isSelfReferencing()) {
        importedForeignKeys.add(fk);
      }
    }

    if (table.hasPrimaryKey()) {
      tablePkColumns.addAll(table.getPrimaryKey().getConstrainedColumns());
    }

    for (final ForeignKey fk : importedForeignKeys) {
      final Set<Column> fkParentColumns =
          fk.getColumnReferences().stream()
              .map(ColumnReference::getPrimaryKeyColumn)
              .collect(Collectors.toSet());
      pkColumnsMap.put(fk.key(), fkParentColumns);

      final Set<Column> fkChildColumns = getImportedColumns(fk);
      importedColumnsMap.put(fk.key(), fkChildColumns);

      final Table parentTable = fk.getPrimaryKeyTable();
      if (!(parentTable instanceof PartialDatabaseObject) && parentTable.hasPrimaryKey()) {
        final Set<Column> parentPkColumns =
            new HashSet<>(parentTable.getPrimaryKey().getConstrainedColumns());
        parentPkColumnsMap.put(fk.key(), parentPkColumns);
      } else {
        parentPkColumnsMap.put(fk.key(), Collections.emptySet());
      }
    }
  }

  private Set<Column> getImportedColumns(final TableReference fk) {
    final Set<Column> fkChildColumns =
        fk.getColumnReferences().stream()
            .map(ColumnReference::getForeignKeyColumn)
            .collect(Collectors.toSet());
    return fkChildColumns;
  }

  /**
   * Builds a lookup of all known unique column combinations for this table. Uses exact ordered
   * column sequences (no subsets).
   */
  private void buildUniquenessLookup() {
    if (table.hasPrimaryKey()) {
      uniqueKeys.add(new HashSet<>(table.getPrimaryKey().getConstrainedColumns()));
    }
    if (table.hasIndexes()) {
      for (final Index index : table.getIndexes()) {
        if (index.isUnique()) {
          uniqueKeys.add(new HashSet<>(index.getColumns()));
        }
      }
    }
  }
}
