/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test.utility.crawl;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

import static us.fatehi.utility.Utility.isBlank;
import static us.fatehi.utility.Utility.requireNotBlank;

import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.Identifiers;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;

abstract class AbstractLightDatabaseObject implements DatabaseObject, Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private final Schema schema;
  private final String name;
  private final Map<String, Object> attributes = new HashMap<>();
  private String remarks;

  AbstractLightDatabaseObject(final Schema schema, final String name) {
    this.schema = requireNonNull(schema, "No schema provided");
    this.name = requireNotBlank(name, "No table name provided");
  }

  @Override
  public int compareTo(final NamedObject o) {
    return Comparator.comparing(NamedObject::getName)
        .thenComparing(obj -> getSchema().compareTo(((DatabaseObject) obj).getSchema()))
        .compare(this, o);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof final AbstractLightDatabaseObject other) {
      return Objects.equals(getName(), other.getName())
          && Objects.equals(getSchema(), other.getSchema());
    }
    return false;
  }

  @Override
  public final <T> T getAttribute(final String name) {
    return (T) attributes.get(name);
  }

  @Override
  public final <T> T getAttribute(final String name, final T defaultValue)
      throws ClassCastException {
    if (hasAttribute(name)) {
      return getAttribute(name);
    }
    return defaultValue;
  }

  @Override
  public final Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public String getFullName() {
    final StringBuffer buffer = new StringBuffer();
    buffer.append(schema.getFullName());
    if (!buffer.isEmpty()) {
		buffer.append(".");
	}
    buffer.append(getName());
    return buffer.toString();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getRemarks() {
    return remarks == null ? "" : remarks;
  }

  @Override
  public Schema getSchema() {
    return schema;
  }

  @Override
  public final boolean hasAttribute(final String name) {
    return attributes.containsKey(name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, schema);
  }

  @Override
  public final boolean hasRemarks() {
    return !isBlank(remarks);
  }

  @Override
  public NamedObjectKey key() {
    return schema.key().with(name);
  }

  @Override
  public final <T> Optional<T> lookupAttribute(final String name) {
    return Optional.ofNullable(getAttribute(name));
  }

  @Override
  public final void removeAttribute(final String name) {
    attributes.remove(name);
  }

  @Override
  public final <T> void setAttribute(final String name, final T value) {
    attributes.put(name, value);
  }

  @Override
  public final void setRemarks(final String remarks) {
    this.remarks = remarks;
  }

  @Override
  public String toString() {
    return getFullName();
  }

  @Override
  public void withQuoting(final Identifiers identifiers) {
    // No-op
  }
}
