/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import java.io.Serial;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.Identifiers;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableConstraintColumn;
import schemacrawler.schema.TableConstraintType;
import schemacrawler.schema.TableReference;

public final class LightTableReference implements TableReference {

  @Serial private static final long serialVersionUID = -5359990477303202179L;

  private final String name;
  private final Table fkTable;
  private final Table pkTable;

  public LightTableReference(final String name, final Table fkTable, final Table pkTable) {
    this.name = name;
    this.fkTable = fkTable;
    this.pkTable = pkTable;
  }

  @Override
  public int compareTo(final NamedObject o) {
    return getFullName().compareTo(o.getFullName());
  }

  @Override
  public <T> T getAttribute(final String name) {
    return null;
  }

  @Override
  public <T> T getAttribute(final String name, final T defaultValue) throws ClassCastException {
    return defaultValue;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return Collections.emptyMap();
  }

  @Override
  public List<ColumnReference> getColumnReferences() {
    return Collections.emptyList();
  }

  @Override
  public List<TableConstraintColumn> getConstrainedColumns() {
    return Collections.emptyList();
  }

  @Override
  public String getDefinition() {
    return null;
  }

  @Override
  public Table getForeignKeyTable() {
    return fkTable;
  }

  @Override
  public String getFullName() {
    return name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Table getParent() {
    return fkTable;
  }

  @Override
  public Table getPrimaryKeyTable() {
    return pkTable;
  }

  @Override
  public String getRemarks() {
    return null;
  }

  @Override
  public Schema getSchema() {
    return null;
  }

  @Override
  public String getShortName() {
    return name;
  }

  @Override
  public TableConstraintType getType() {
    return TableConstraintType.foreign_key;
  }

  @Override
  public boolean hasAttribute(final String name) {
    return false;
  }

  @Override
  public boolean hasDefinition() {
    return false;
  }

  @Override
  public boolean hasRemarks() {
    return false;
  }

  @Override
  public boolean isDeferrable() {
    return false;
  }

  @Override
  public boolean isInitiallyDeferred() {
    return false;
  }

  @Override
  public boolean isOptional() {
    return false;
  }

  @Override
  public boolean isParentPartial() {
    return false;
  }

  @Override
  public boolean isSelfReferencing() {
    return fkTable.equals(pkTable);
  }

  @Override
  public Iterator<ColumnReference> iterator() {
    return getColumnReferences().iterator();
  }

  @Override
  public NamedObjectKey key() {
    return new NamedObjectKey(name);
  }

  @Override
  public <T> Optional<T> lookupAttribute(final String name) {
    return Optional.empty();
  }

  @Override
  public void removeAttribute(final String name) {}

  @Override
  public <T> void setAttribute(final String name, final T value) {}

  @Override
  public void setRemarks(final String remarks) {}

  @Override
  public void withQuoting(final Identifiers identifiers) {}
}
