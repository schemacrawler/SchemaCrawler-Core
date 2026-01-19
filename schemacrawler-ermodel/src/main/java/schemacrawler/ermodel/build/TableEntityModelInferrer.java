/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.build;

import static java.util.Objects.requireNonNull;
import static schemacrawler.utility.MetaDataUtility.isPartial;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.ermodel.model.ForeignKeyCardinality;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Index;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableReference;
import us.fatehi.utility.OptionalBoolean;

/**
 * Algorithm for identifying entity types and foreign key cardinality and other attributes for a
 * table.
 */
public final class TableEntityModelInferrer {

  private final Table table;

  private final Set<Set<Column>> uniqueIndexes;
  private final Set<Set<Column>> indexes;

  private final Set<ForeignKey> importedForeignKeys;

  private final Set<Column> tablePkColumns;

  private final Map<NamedObjectKey, Set<Column>> importedColumnsMap;
  private final Map<NamedObjectKey, Set<Column>> pkColumnsMap;
  private final Map<NamedObjectKey, Set<Column>> parentPkColumnsMap;

  /**
   * Creates a new model for a table.
   *
   * @param table Table, cannot be null or partial
   */
  public TableEntityModelInferrer(final Table table) {
    this.table = requireNonNull(table, "No table provided");
    if (isPartial(table)) {
      throw new IllegalArgumentException("Table cannot be partial");
    }

    uniqueIndexes = new HashSet<>();
    indexes = new HashSet<>();

    importedForeignKeys = new HashSet<>();
    tablePkColumns = new HashSet<>();

    importedColumnsMap = new HashMap<>();
    pkColumnsMap = new HashMap<>();
    parentPkColumnsMap = new HashMap<>();

    buildSupportingLookups();
    buildIndexesLookup();
  }

  /**
   * Identifies if a table is a bridge table. A table T is treated as a bridge for an M..N
   * relationship between two tables if:
   *
   * <ul>
   *   <li>T has at least two foreign keys, each to a different parent table; and
   *   <li>there is a primary key or unique index whose columns are exactly those two foreign key
   *       columns; and
   *   <li>there are no other columns in T that participate in the PK/ unique index beyond those two
   *       FKs.
   * </ul>
   *
   * @return Whether the table is a bridge table
   */
  public boolean inferBridgeTable() {
    if (importedForeignKeys.size() < 2) {
      return false;
    }

    final long countDistinctFk =
        importedForeignKeys.stream().map(ForeignKey::getPrimaryKeyTable).distinct().count();
    if (countDistinctFk < 2) {
      return false;
    }

    for (final ForeignKey fk1 : importedForeignKeys) {
      for (final ForeignKey fk2 : importedForeignKeys) {
        if (fk1.equals(fk2) || fk1.getPrimaryKeyTable().equals(fk2.getPrimaryKeyTable())) {
          continue;
        }

        final Set<Column> combinedFkColumns = new HashSet<>(importedColumnsMap.get(fk1.key()));
        combinedFkColumns.addAll(importedColumnsMap.get(fk2.key()));

        if (uniqueIndexes.contains(combinedFkColumns)) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Identifies the entity type of a table. The algorithm uses a set of rules to classify the table
   * into one of several predefined entity types.
   *
   * @return Entity type
   */
  public EntityType inferEntityType() {

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

  /**
   * Identifies the cardinality of a foreign key.
   *
   * @param fk Foreign key
   * @return Foreign key cardinality
   */
  public ForeignKeyCardinality inferForeignKeyCardinality(final TableReference fk) {

    if (fk == null) {
      return ForeignKeyCardinality.unknown;
    }

    final ForeignKeyCardinality cardinality;

    final Set<Column> importedColumns = findOrGetImportedKeys(fk);
    final boolean isForeignKeyUnique = uniqueIndexes.contains(importedColumns);
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

  /**
   * Checks if the columns of a foreign key are covered by an index on this table.
   *
   * @param fk Foreign key, can be null
   * @return Whether the foreign key columns are covered by an index
   */
  public OptionalBoolean isForeignKeyCoveredByIndex(final TableReference fk) {

    if (fk == null) {
      return OptionalBoolean.unknown;
    }

    final Set<Column> importedColumns = findOrGetImportedKeys(fk);
    for (final Set<Column> indexColumns : indexes) {
      if (indexColumns.containsAll(importedColumns)) {
        return OptionalBoolean.true_value;
      }
    }
    return OptionalBoolean.false_value;
  }

  /**
   * Checks if the columns of a foreign key are covered by a unique index on this table.
   *
   * @param fk Foreign key, can be null
   * @return Whether the foreign key columns are covered by a unique index
   */
  public OptionalBoolean isForeignKeyCoveredByUniqueIndex(final TableReference fk) {

    if (fk == null) {
      return OptionalBoolean.unknown;
    }

    final Set<Column> importedColumns = findOrGetImportedKeys(fk);
    return OptionalBoolean.fromBoolean(uniqueIndexes.contains(importedColumns));
  }

  @Override
  public String toString() {
    return table.toString();
  }

  /** Builds a lookup of all known index column combinations for this table. */
  private void buildIndexesLookup() {
    if (table.hasPrimaryKey()) {
      final HashSet<Column> pkColumns =
          new HashSet<>(table.getPrimaryKey().getConstrainedColumns());
      uniqueIndexes.add(pkColumns);
      indexes.add(pkColumns);
    }
    if (table.hasIndexes()) {
      for (final Index index : table.getIndexes()) {
        final Set<Column> indexColumns = new HashSet<>(index.getColumns());
        indexes.add(indexColumns);
        if (index.isUnique()) {
          uniqueIndexes.add(indexColumns);
        }
      }
    }
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

      findOrGetImportedKeys(fk);

      final Table parentTable = fk.getPrimaryKeyTable();
      if (!isPartial(parentTable) && parentTable.hasPrimaryKey()) {
        final Set<Column> parentPkColumns =
            new HashSet<>(parentTable.getPrimaryKey().getConstrainedColumns());
        parentPkColumnsMap.put(fk.key(), parentPkColumns);
      } else {
        parentPkColumnsMap.put(fk.key(), Collections.emptySet());
      }
    }
  }

  private Set<Column> findOrGetImportedKeys(final TableReference fk) {
    requireNonNull(fk, "No foreign key provided");

    final Set<Column> importedColumns;
    if (importedColumnsMap.containsKey(fk.key())) {
      importedColumns = importedColumnsMap.get(fk.key());
    } else {
      importedColumns =
          fk.getColumnReferences().stream()
              .map(ColumnReference::getForeignKeyColumn)
              .collect(Collectors.toSet());
      importedColumnsMap.put(fk.key(), importedColumns);
    }
    return importedColumns;
  }
}
