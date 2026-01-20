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

import java.io.Serial;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import schemacrawler.ermodel.model.TableBacked;
import schemacrawler.schema.Column;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.exceptions.ConfigurationException;

abstract class AbstractTableBacked implements TableBacked {

  @Serial private static final long serialVersionUID = -1252099222675350939L;

  private final Table table;

  public AbstractTableBacked(final Table table) {
    this.table = requireNonNull(table, "No table provided");
    if (isPartial(table)) {
      throw new ConfigurationException("Table cannot be partial");
    }
  }

  @Override
  public int compareTo(final NamedObject o) {
    return table.compareTo(o);
  }

  @Override
  public <T> T getAttribute(final String name) {
    return table.getAttribute(name);
  }

  @Override
  public <T> T getAttribute(final String name, final T defaultValue) throws ClassCastException {
    return table.getAttribute(name, defaultValue);
  }

  @Override
  public Collection<Column> getAttributeColumns() {
    return table.getColumns().stream()
        .filter(column -> !column.isPartOfPrimaryKey() && !column.isPartOfForeignKey())
        .collect(Collectors.toList());
  }

  @Override
  public Map<String, Object> getAttributes() {
    return table.getAttributes();
  }

  @Override
  public String getFullName() {
    return table.getFullName();
  }

  @Override
  public String getName() {
    return table.getName();
  }

  @Override
  public String getRemarks() {
    return table.getRemarks();
  }

  @Override
  public Table getTable() {
    return table;
  }

  @Override
  public boolean hasAttribute(final String name) {
    return table.hasAttribute(name);
  }

  @Override
  public boolean hasRemarks() {
    return table.hasRemarks();
  }

  @Override
  public NamedObjectKey key() {
    return table.key();
  }

  @Override
  public <T> Optional<T> lookupAttribute(final String name) {
    return table.lookupAttribute(name);
  }

  @Override
  public void removeAttribute(final String name) {
    table.removeAttribute(name);
  }

  @Override
  public <T> void setAttribute(final String name, final T value) {
    table.setAttribute(name, value);
  }

  @Override
  public void setRemarks(final String remarks) {
    table.setRemarks(remarks);
  }
}
