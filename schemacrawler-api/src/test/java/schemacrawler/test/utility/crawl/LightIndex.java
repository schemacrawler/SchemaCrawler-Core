/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import java.io.Serial;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import schemacrawler.schema.Index;
import schemacrawler.schema.IndexColumn;
import schemacrawler.schema.IndexType;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;

/** A light index, which has a name and a table, and holds no columns. */
public class LightIndex extends AbstractLightDatabaseObject implements Index {

  @Serial private static final long serialVersionUID = 1L;

  private final Table table;

  public LightIndex(final Table table, final String name) {
    super(table.getSchema(), name);
    this.table = table;
  }

  @Override
  public long getCardinality() {
    return 0;
  }

  @Override
  public List<IndexColumn> getColumns() {
    return List.of();
  }

  @Override
  public String getDefinition() {
    return "";
  }

  @Override
  public String getFilterCondition() {
    return null;
  }

  @Override
  public IndexType getIndexType() {
    return IndexType.unknown;
  }

  @Override
  public long getPages() {
    return 0;
  }

  @Override
  public Table getParent() {
    return table;
  }

  @Override
  public String getShortName() {
    return getName();
  }

  @Override
  public IndexType getType() {
    return getIndexType();
  }

  @Override
  public boolean hasDefinition() {
    return false;
  }

  @Override
  public boolean hasFilterCondition() {
    return false;
  }

  @Override
  public boolean isParentPartial() {
    return false;
  }

  @Override
  public boolean isPrimaryKeyIndex() {
    return false;
  }

  @Override
  public boolean isUnique() {
    return false;
  }

  @Override
  public Iterator<IndexColumn> iterator() {
    return getColumns().iterator();
  }

  @Override
  public NamedObjectKey key() {
    return table.key().with(getName());
  }

  @Override
  public <C extends IndexColumn> Optional<C> lookupColumn(final String name) {
    return Optional.empty();
  }
}
