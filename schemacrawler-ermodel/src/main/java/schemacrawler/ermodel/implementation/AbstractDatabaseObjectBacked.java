/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.ermodel.implementation;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import java.util.Map;
import java.util.Optional;
import schemacrawler.ermodel.model.DatabaseObjectBacked;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;

abstract class AbstractDatabaseObjectBacked<DO extends DatabaseObject>
    implements DatabaseObjectBacked<DO> {

  @Serial private static final long serialVersionUID = -1252099222675350939L;

  private final DO dbObject;

  public AbstractDatabaseObjectBacked(final DO dbObject) {
    this.dbObject = requireNonNull(dbObject, "No database object provided");
  }

  @Override
  public int compareTo(final NamedObject namedObj) {
    if (namedObj == null) {
      return 1;
    }
    return key().compareTo(namedObj.key());
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof final NamedObject namedObj) {
      return key().equals(namedObj.key());
    }
    return false;
  }

  @Override
  public <T> T getAttribute(final String name) {
    return dbObject.getAttribute(name);
  }

  @Override
  public <T> T getAttribute(final String name, final T defaultValue) throws ClassCastException {
    return dbObject.getAttribute(name, defaultValue);
  }

  @Override
  public Map<String, Object> getAttributes() {
    return dbObject.getAttributes();
  }

  @Override
  public DO getDatabaseObject() {
    return dbObject;
  }

  @Override
  public String getFullName() {
    return dbObject.getFullName();
  }

  @Override
  public String getName() {
    return dbObject.getName();
  }

  @Override
  public String getRemarks() {
    return dbObject.getRemarks();
  }

  @Override
  public boolean hasAttribute(final String name) {
    return dbObject.hasAttribute(name);
  }

  @Override
  public int hashCode() {
    return key().hashCode();
  }

  @Override
  public boolean hasRemarks() {
    return dbObject.hasRemarks();
  }

  @Override
  public NamedObjectKey key() {
    return dbObject.key();
  }

  @Override
  public <T> Optional<T> lookupAttribute(final String name) {
    return dbObject.lookupAttribute(name);
  }

  @Override
  public void removeAttribute(final String name) {
    dbObject.removeAttribute(name);
  }

  @Override
  public <T> void setAttribute(final String name, final T value) {
    dbObject.setAttribute(name, value);
  }

  @Override
  public void setRemarks(final String remarks) {
    dbObject.setRemarks(remarks);
  }

  @Override
  public String toString() {
    return dbObject.toString();
  }
}
