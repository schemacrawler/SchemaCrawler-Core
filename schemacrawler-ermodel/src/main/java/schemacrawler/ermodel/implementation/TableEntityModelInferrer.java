/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.implementation;

import static java.util.Objects.requireNonNull;
import static schemacrawler.utility.MetaDataUtility.isPartial;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.ermodel.model.RelationshipCardinality;
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

    uniqueIndexes = new HashSet<>();
    indexes = new HashSet<>();

    importedForeignKeys = new HashSet<>();
    tablePkColumns = new HashSet<>();

    importedColumnsMap = new HashMap<>();
    pkColumnsMap = new HashMap<>();
    parentPkColumnsMap = new HashMap<>();

    if (!isPartial(table)) {
      buildSupportingLookups();
      buildIndexesLookup();
    }
  }

  /**
   * Checks if the columns of a foreign key are covered by an index on this table.
   *
   * @param fk Foreign key, can be null
   * @return Whether the foreign key columns are covered by an index
   */
  public OptionalBoolean coveredByIndex(final TableReference fk) {

    if (!isFkValid(fk)) {
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
  public OptionalBoolean coveredByUniqueIndex(final TableReference fk) {

    if (!isFkValid(fk)) {
      return OptionalBoolean.unknown;
    }

    final Set<Column> importedColumns = findOrGetImportedKeys(fk);
    return OptionalBoolean.fromBoolean(uniqueIndexes.contains(importedColumns));
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
   * Identifies the cardinality of a foreign key.
   *
   * @param fk Foreign key
   * @return Foreign key cardinality
   */
  public RelationshipCardinality inferCardinality(final TableReference fk) {

    if (!isFkValid(fk)) {
      return RelationshipCardinality.unknown;
    }

    final RelationshipCardinality cardinality;

    final Set<Column> importedColumns = findOrGetImportedKeys(fk);
    final boolean isForeignKeyUnique = uniqueIndexes.contains(importedColumns);
    final boolean isForeignKeyOptional = fk.isOptional();

    if (isForeignKeyUnique) {
      if (isForeignKeyOptional) {
        cardinality = RelationshipCardinality.zero_one;
      } else {
        cardinality = RelationshipCardinality.one_one;
      }
    } else if (isForeignKeyOptional) {
      cardinality = RelationshipCardinality.zero_many;
    } else {
      cardinality = RelationshipCardinality.one_many;
    }

    return cardinality;
  }

  /**
   * Identifies the entity type of a table. The algorithm uses a set of rules to classify the table
   * into one of several predefined entity types.
   *
   * @return Entity type
   */
  public EntityType inferEntityType() {

    // Step 1: Non-entity tables do not have a primary key.
    if (isPartial(table)) {
      return EntityType.unknown;
    }
    if (!table.hasPrimaryKey()) {
      return EntityType.non_entity;
    }

    // Steps 2 and 3: Check for subtype and weak entity patterns via FK analysis.
    for (final ForeignKey fk : importedForeignKeys) {
      if (isSubtypeRelationship(fk)) {
        return EntityType.subtype;
      }
      if (isWeakEntityRelationship(fk)) {
        return EntityType.weak_entity;
      }
    }

    // Step 4: Strong entities have self-sufficient PKs and low referential
    // connectivity.
    if (isStrongEntity()) {
      return EntityType.strong_entity;
    }

    // Step 5: Tables with ambiguous patterns cannot be confidently classified.
    return EntityType.unknown;
  }

  /**
   * Identifies the supertype of the table, if the table is a subtype.
   *
   * @return Entity type
   */
  public Optional<Table> inferSuperType() {
    return inferSuperTypeReference().map(ForeignKey::getPrimaryKeyTable);
  }

  /**
   * Identifies the foreign key from this subtype to its supertype.
   *
   * @return Foreign key to supertype, if this table is a subtype
   */
  public Optional<ForeignKey> inferSuperTypeReference() {

    if (inferEntityType() != EntityType.subtype) {
      return Optional.empty();
    }

    for (final ForeignKey fk : importedForeignKeys) {
      if (isSubtypeRelationship(fk)) {
        return Optional.of(fk);
      }
    }
    return Optional.empty();
  }

  @Override
  public String toString() {
    return table.toString();
  }

  /** Builds a lookup of all known index column combinations for this table. */
  private void buildIndexesLookup() {
    if (table.hasPrimaryKey()) {
      final Set<Column> pkColumns = Set.copyOf(table.getPrimaryKey().getConstrainedColumns());
      uniqueIndexes.add(pkColumns);
      indexes.add(pkColumns);
    }
    if (table.hasIndexes()) {
      for (final Index index : table.getIndexes()) {
        final Set<Column> indexColumns = Set.copyOf(index.getColumns());
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
          Set.copyOf(
              fk.getColumnReferences().stream().map(ColumnReference::getPrimaryKeyColumn).toList());
      pkColumnsMap.put(fk.key(), fkParentColumns);

      findOrGetImportedKeys(fk);

      final Table parentTable = fk.getPrimaryKeyTable();
      if (!isPartial(parentTable) && parentTable.hasPrimaryKey()) {
        final Set<Column> parentPkColumns =
            Set.copyOf(parentTable.getPrimaryKey().getConstrainedColumns());
        parentPkColumnsMap.put(fk.key(), parentPkColumns);
      } else {
        parentPkColumnsMap.put(fk.key(), Set.of());
      }
    }
  }

  private Set<Column> findOrGetImportedKeys(final TableReference fk) {
    requireNonNull(fk, "No foreign key provided");
    return importedColumnsMap.computeIfAbsent(
        fk.key(),
        key ->
            Set.copyOf(
                fk.getColumnReferences().stream()
                    .map(ColumnReference::getForeignKeyColumn)
                    .toList()));
  }

  private boolean isFkValid(final TableReference fk) {
    final boolean isNotValid =
        fk == null || isPartial(table) || !fk.getForeignKeyTable().equals(table);
    return !isNotValid;
  }

  /**
   * Strong entities have self-sufficient primary keys (no FK columns in PK) and low referential
   * connectivity. A table with FK columns in its PK, or foreign keys to 2 or more distinct parent
   * tables (excluding self-references), is not classified as a strong entity because it may be a
   * bridge table or have a composite FK-based PK.
   */
  private boolean isStrongEntity() {
    final boolean pkHasFkColumn = tablePkColumns.stream().anyMatch(Column::isPartOfForeignKey);
    if (pkHasFkColumn) {
      return false;
    }
    final Set<Table> referencedTables = new HashSet<>(table.getReferencedTables());
    // Self-references don't count towards the limit of 2 other tables
    referencedTables.remove(table);
    return referencedTables.size() < 2;
  }

  /**
   * Subtype tables inherit their entire primary key from a single supertype table. PK(T) exactly
   * matches the child columns of a FK to a parent table P whose primary key PK(P) equals the
   * parent-side columns of that FK.
   */
  private boolean isSubtypeRelationship(final ForeignKey fk) {
    final Set<Column> fkParentColumns = pkColumnsMap.get(fk.key());
    final Set<Column> parentPkColumns = parentPkColumnsMap.get(fk.key());
    final Set<Column> fkChildColumns = importedColumnsMap.get(fk.key());
    return !parentPkColumns.isEmpty()
        && parentPkColumns.equals(fkParentColumns)
        && tablePkColumns.equals(fkChildColumns);
  }

  /**
   * Weak entities combine a parent's full primary key (via an identifying FK) with their own
   * discriminator column(s). PK(T) contains, as a proper superset, the child columns of a FK to
   * parent P whose primary key PK(P) equals the parent-side columns of that FK.
   */
  private boolean isWeakEntityRelationship(final ForeignKey fk) {
    final Set<Column> fkParentColumns = pkColumnsMap.get(fk.key());
    final Set<Column> parentPkColumns = parentPkColumnsMap.get(fk.key());
    final Set<Column> fkChildColumns = importedColumnsMap.get(fk.key());
    return !parentPkColumns.isEmpty()
        && parentPkColumns.equals(fkParentColumns)
        && tablePkColumns.containsAll(fkChildColumns)
        && tablePkColumns.size() > fkChildColumns.size();
  }
}
