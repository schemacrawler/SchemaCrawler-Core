/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;

/** Searches the current, visible contents of a catalog without changing it. */
public final class CatalogSearcher {

  public static CatalogSearcher search(final Catalog catalog) {
    return new CatalogSearcher(catalog);
  }

  private final Catalog catalog;

  /** Creates a searcher for a catalog. */
  private CatalogSearcher(final Catalog catalog) {
    this.catalog = requireNonNull(catalog, "No catalog provided");
  }

  /** Finds matching column data types. */
  public Collection<ColumnDataType> findColumnDataTypes(
      final NamedObjectFilter<? super ColumnDataType> filter) {
    if (filter == null) {
      return List.of();
    }
    return catalog.getColumnDataTypes().stream().filter(filter).toList();
  }

  /** Finds matching visible columns. */
  public Collection<Column> findColumns(final NamedObjectFilter<? super Column> filter) {
    if (filter == null) {
      return List.of();
    }
    return catalog.getTables().stream()
        .flatMap(table -> table.getColumns().stream())
        .filter(filter)
        .toList();
  }

  /** Finds matching routines. */
  public Collection<Routine> findRoutines(final NamedObjectFilter<? super Routine> filter) {
    if (filter == null) {
      return List.of();
    }
    return catalog.getRoutines().stream().filter(filter).toList();
  }

  /** Finds matching schemas. */
  public Collection<Schema> findSchemas(final NamedObjectFilter<? super Schema> filter) {
    if (filter == null) {
      return List.of();
    }
    return catalog.getSchemas().stream().filter(filter).toList();
  }

  /** Finds matching sequences. */
  public Collection<Sequence> findSequences(final NamedObjectFilter<? super Sequence> filter) {
    if (filter == null) {
      return List.of();
    }
    return catalog.getSequences().stream().filter(filter).toList();
  }

  /** Finds matching synonyms. */
  public Collection<Synonym> findSynonyms(final NamedObjectFilter<? super Synonym> filter) {
    if (filter == null) {
      return List.of();
    }
    return catalog.getSynonyms().stream().filter(filter).toList();
  }

  /** Finds matching tables. */
  public Collection<Table> findTables(final NamedObjectFilter<? super Table> filter) {
    if (filter == null) {
      return List.of();
    }
    return catalog.getTables().stream().filter(filter).toList();
  }
}
