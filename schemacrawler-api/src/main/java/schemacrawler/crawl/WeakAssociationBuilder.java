/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Iterator;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableReference;

public final class WeakAssociationBuilder extends ImplicitAssociationBuilder {

  public static WeakAssociationBuilder builder(final Catalog catalog) {
    return new WeakAssociationBuilder(catalog);
  }

  private WeakAssociationBuilder(final Catalog catalog) {
    super(catalog);
  }

  @Override
  public TableReference build() {
    final TableReference implicitAssociation = super.build();
    if (implicitAssociation == null || implicitAssociation instanceof ForeignKey) {
      return implicitAssociation;
    }

    // If there is a matching weak association (checked by the column references),
    // do not create another
    final TableReference matchingWeakAssociation =
        lookupMatchingWeakAssociation(implicitAssociation);
    if (matchingWeakAssociation != null) {
      return matchingWeakAssociation;
    }
    // Convert to weak association
    final Iterator<ColumnReference> columnRefsIterator =
        implicitAssociation.getColumnReferences().iterator();
    final MutableWeakAssociation weakAssociation =
        new MutableWeakAssociation(implicitAssociation.getName(), columnRefsIterator.next());
    while (columnRefsIterator.hasNext()) {
      final ColumnReference columnReference = columnRefsIterator.next();
      weakAssociation.addColumnReference(columnReference);
    }
    // Add weak association to tables if no matching foreign key is found
    if (weakAssociation.getPrimaryKeyTable() instanceof final MutableTable table) {
      table.addWeakAssociation(weakAssociation);
    }
    if (weakAssociation.getForeignKeyTable() instanceof final MutableTable table) {
      table.addWeakAssociation(weakAssociation);
    }

    return weakAssociation;
  }

  private TableReference lookupMatchingWeakAssociation(final TableReference weakAssociation) {
    requireNonNull(weakAssociation, "No weak association provided");

    final Table referencedTable = weakAssociation.getReferencedTable();
    if (!(referencedTable instanceof MutableTable)) {
      return null;
    }

    // Search weak associations by column references
    final Collection<? extends TableReference> weakAssociations =
        referencedTable.getWeakAssociations();
    for (final TableReference weakAssociationInTable : weakAssociations) {
      if (weakAssociation.compareTo(weakAssociationInTable) == 0) {
        return weakAssociationInTable;
      }
    }

    return null;
  }
}
