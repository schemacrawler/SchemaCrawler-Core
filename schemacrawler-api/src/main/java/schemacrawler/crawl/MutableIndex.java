/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.crawl;

import static java.util.Objects.requireNonNull;
import static us.fatehi.utility.Utility.isBlank;

import java.io.Serial;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import schemacrawler.namedobject.ConcurrentDatabaseObjectIndex;
import schemacrawler.schema.Index;
import schemacrawler.schema.IndexColumn;
import schemacrawler.schema.IndexType;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Table;
import us.fatehi.utility.CollectionsUtility;

/** Represents an index on a database table. */
final class MutableIndex extends AbstractDependantObject<Table> implements Index {

  @Serial private static final long serialVersionUID = 4051326747138079028L;

  private final ConcurrentDatabaseObjectIndex<MutableIndexColumn> columns =
      new ConcurrentDatabaseObjectIndex<>();
  private String definition;
  private long cardinality;
  private IndexType indexType;
  private boolean isUnique;
  private long pages;
  private String filterCondition;

  MutableIndex(final Table parent, final String name) {
    super(new TablePointer(parent), name);
    // Default values
    indexType = IndexType.unknown;
    definition = "";
    filterCondition = "";
  }

  /**
   * {@inheritDoc}
   *
   * <p>NOTE: compareTo is not compatible with equals. equals compares the full name of a database
   * object, but compareTo uses more fields to define a "natural" sorting order. compareTo may
   * return incorrect results until the object is fully built by SchemaCrawler.
   *
   * <p>Note: Since indexes are not always explicitly named in databases, the sorting routine orders
   * the indexes by the names of the columns in the index.
   */
  @Override
  public int compareTo(final NamedObject obj) {
    if (obj == null || !(obj instanceof final Index that)) {
      return -1;
    }

    int compareTo = 0;

    if (compareTo == 0) {
      final List<IndexColumn> thisColumns = getColumns();
      final List<IndexColumn> thatColumns = that.getColumns();

      compareTo = CollectionsUtility.compareLists(thisColumns, thatColumns);
    }

    if (compareTo == 0 && isUnique != that.isUnique()) {
      compareTo = isUnique ? -1 : 1;
    }

    if (compareTo == 0) {
      compareTo = indexType.ordinal() - that.getIndexType().ordinal();
    }

    if (compareTo == 0) {
      compareTo = super.compareTo(obj);
    }

    return compareTo;
  }

  /** {@inheritDoc} */
  @Override
  public long getCardinality() {
    return cardinality;
  }

  /** {@inheritDoc} */
  @Override
  public List<IndexColumn> getColumns() {
    return List.copyOf(columns.getObjects());
  }

  /** {@inheritDoc} */
  @Override
  public String getDefinition() {
    return definition;
  }

  @Override
  public String getFilterCondition() {
    return filterCondition;
  }

  /** {@inheritDoc} */
  @Override
  public IndexType getIndexType() {
    return indexType;
  }

  /** {@inheritDoc} */
  @Override
  public long getPages() {
    return pages;
  }

  /** {@inheritDoc} */
  @Override
  public IndexType getType() {
    return getIndexType();
  }

  @Override
  public boolean hasDefinition() {
    return !isBlank(definition);
  }

  @Override
  public boolean hasFilterCondition() {
    return !isBlank(filterCondition);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isUnique() {
    return isUnique;
  }

  @Override
  public Iterator<IndexColumn> iterator() {
    return getColumns().iterator();
  }

  /** {@inheritDoc} */
  @Override
  public Optional<MutableIndexColumn> lookupColumn(final String name) {
    // NOTE: Index columns are still table columns, so they need to be
    // looked up with a table lookup key
    return columns.lookup(getParent(), name);
  }

  void addColumn(final MutableIndexColumn column) {
    columns.add(column);
  }

  void setCardinality(final long cardinality) {
    this.cardinality = cardinality;
  }

  void setDefinition(final String definition) {
    if (!hasDefinition() && !isBlank(definition)) {
      this.definition = definition;
    }
  }

  void setFilterCondition(final String filterCondition) {
    if (!hasFilterCondition() && !isBlank(filterCondition)) {
      this.filterCondition = filterCondition;
    }
  }

  void setIndexType(final IndexType indexType) {
    this.indexType = requireNonNull(indexType, "Null index type");
  }

  void setPages(final long pages) {
    this.pages = pages;
  }

  void setUnique(final boolean unique) {
    isUnique = unique;
  }
}
