/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import java.util.Iterator;
import schemacrawler.crawl.ImplicitAssociationBuilder.ImplicitAssociationColumn;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.TableReference;

public final class WeakAssociationBuilder {

  public static WeakAssociationBuilder builder(final Catalog catalog) {
    return new WeakAssociationBuilder(catalog);
  }

  private final ImplicitAssociationBuilder implicitAssociationBuilder;

  private WeakAssociationBuilder(final Catalog catalog) {
    implicitAssociationBuilder = ImplicitAssociationBuilder.builder(catalog);
  }

  public ImplicitAssociationBuilder addColumnReference(
      final Column fkColumn, final Column pkColumn) {
    return implicitAssociationBuilder.addColumnReference(fkColumn, pkColumn);
  }

  public ImplicitAssociationBuilder addColumnReference(
      final ImplicitAssociationColumn referencingColumn,
      final ImplicitAssociationColumn referencedColumn) {
    return implicitAssociationBuilder.addColumnReference(referencingColumn, referencedColumn);
  }

  public TableReference build() {
    final TableReference implicitAssociation = implicitAssociationBuilder.build();
    if (implicitAssociation == null || implicitAssociation instanceof ForeignKey) {
      return implicitAssociation;
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

  public ImplicitAssociationBuilder withName(final String weakAssociationName) {
    return implicitAssociationBuilder.withName(weakAssociationName);
  }
}
