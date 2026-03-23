/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static java.util.Objects.requireNonNull;
import static schemacrawler.crawl.RetrieverUtility.lookupOrCreateColumn;
import static schemacrawler.utility.MetaDataUtility.isPartial;
import static us.fatehi.utility.Utility.requireNotBlank;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableReference;
import us.fatehi.utility.Builder;
import us.fatehi.utility.string.StringFormat;

public final class ImplicitAssociationBuilder implements Builder<TableReference> {

  public static final record ImplicitAssociationColumn(
      Schema schema, String tableName, String columnName) {

    public ImplicitAssociationColumn {
      schema = requireNonNull(schema, "No schema provided");
      tableName = requireNotBlank(tableName, "No table name provided");
      columnName = requireNotBlank(columnName, "No column name provided");
    }
  }

  private static final Logger LOGGER = Logger.getLogger(ImplicitAssociationBuilder.class.getName());

  public static ImplicitAssociationBuilder builder(final Catalog catalog) {
    return new ImplicitAssociationBuilder(catalog);
  }

  private final Catalog catalog;
  private final Collection<ColumnReference> columnReferences;

  private ImplicitAssociationBuilder(final Catalog catalog) {
    this.catalog = requireNonNull(catalog, "No catalog provided");
    columnReferences = new HashSet<>();
  }

  public ImplicitAssociationBuilder addColumnReference(
      final Column fkColumn, final Column pkColumn) {
    // Ensure that we have obtained non-null values
    requireNonNull(fkColumn, "No referencing column provided");
    requireNonNull(pkColumn, "No referenced column provided");

    // Implicit associations cannot be self-referencing
    if (fkColumn.equals(pkColumn)) {
      return this;
    }

    // Implicit associations cannot be completely partial
    final boolean isFkColumnPartial = isPartial(fkColumn);
    final boolean isPkColumnPartial = isPartial(pkColumn);
    if (isFkColumnPartial && isPkColumnPartial) {
      return this;
    }

    // Start key sequences at index 1
    final int keySequence = columnReferences.size() + 1;
    final ColumnReference columnReference =
        new ImmutableColumnReference(keySequence, fkColumn, pkColumn);
    columnReferences.add(columnReference);

    return this;
  }

  public ImplicitAssociationBuilder addColumnReference(
      final ImplicitAssociationColumn referencingColumn,
      final ImplicitAssociationColumn referencedColumn) {
    requireNonNull(referencingColumn, "No referencing column provided");
    requireNonNull(referencedColumn, "No referenced column provided");

    final Column fkColumn =
        lookupOrCreateColumn(
            catalog,
            referencingColumn.schema(),
            referencingColumn.tableName(),
            referencingColumn.columnName());
    final Column pkColumn =
        lookupOrCreateColumn(
            catalog,
            referencedColumn.schema(),
            referencedColumn.tableName(),
            referencedColumn.columnName());

    return addColumnReference(fkColumn, pkColumn);
  }

  @Override
  public TableReference build() {
    return findOrCreate().orElse(null);
  }

  public ImplicitAssociationBuilder clear() {
    columnReferences.clear();
    LOGGER.log(Level.FINER, new StringFormat("Builder <%s> cleared", hashCode()));
    return this;
  }

  private Optional<TableReference> findOrCreate() {
    if (columnReferences.isEmpty()) {
      LOGGER.log(
          Level.CONFIG, "Implicit association not built, since there are no column references");
      return Optional.empty();
    }

    final Iterator<ColumnReference> iterator = columnReferences.iterator();

    final ColumnReference someColumnReference = iterator.next();
    final Table referencedTable = someColumnReference.getPrimaryKeyColumn().getParent();
    final Table dependentTable = someColumnReference.getForeignKeyColumn().getParent();

    final String implicitAssociationName =
        RetrieverUtility.constructForeignKeyName(referencedTable, dependentTable);

    final MutableImplicitAssociation implicitAssociation =
        new MutableImplicitAssociation(implicitAssociationName, someColumnReference);
    while (iterator.hasNext()) {
      final ColumnReference columnReference = iterator.next();
      // Add a column reference only if they reference the same two tables
      final boolean addedColumnReference = implicitAssociation.addColumnReference(columnReference);
      if (!addedColumnReference) {
        LOGGER.log(
            Level.CONFIG,
            new StringFormat(
                "Implicit association not built, since column references are not consistent <%s>",
                columnReferences));
        return Optional.empty();
      }
    }

    // If there is a matching foreign key, do not create a similar implicit
    // association
    final Optional<ForeignKey> optionalMatchingForeignKey =
        lookupMatchingForeignKey(implicitAssociation);
    if (optionalMatchingForeignKey.isPresent()) {
      LOGGER.log(
          Level.CONFIG,
          new StringFormat(
              "Implicit association not built, since it matches foreign key <%s>",
              optionalMatchingForeignKey.get()));
      return Optional.empty();
    }

    return Optional.of(implicitAssociation);
  }

  private Optional<ForeignKey> lookupMatchingForeignKey(final TableReference implicitAssociation) {
    requireNonNull(implicitAssociation, "No implicit association provided");

    final Table referencedTable = implicitAssociation.getReferencedTable();
    if (!(referencedTable instanceof MutableTable)) {
      return Optional.empty();
    }

    // Search foreign keys by column references
    final Collection<ForeignKey> exportedForeignKeys = referencedTable.getExportedForeignKeys();
    for (final ForeignKey foreignKey : exportedForeignKeys) {
      if (implicitAssociation.compareTo(foreignKey) == 0) {
        return Optional.of(foreignKey);
      }
    }

    return Optional.empty();
  }
}
