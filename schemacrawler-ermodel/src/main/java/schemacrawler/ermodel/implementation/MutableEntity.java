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
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.ermodel.model.Relationship;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.exceptions.ConfigurationException;
import schemacrawler.utility.MetaDataUtility;

/** Conceptual entity backed by a SchemaCrawler table. */
public class MutableEntity implements Entity {

  @Serial private static final long serialVersionUID = 3946422106166202467L;

  private final Table table;
  private EntityType entityType;

  public MutableEntity(final Table table) {
    this.table = requireNonNull(table, "No table provided");
    if (MetaDataUtility.isPartial(table)) {
      throw new ConfigurationException("Table cannot be partial");
    }
    entityType = EntityType.unknown;
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
  public Collection<Relationship> getOutgoingRelationships() {
    throw new UnsupportedOperationException("TODO: NOT IMPLEMENTED");
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
  public EntityType getType() {
    return entityType;
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

  void setEntityType(final EntityType entityType) {
    if (entityType == null
        || !EnumSet.of(EntityType.strong_entity, EntityType.weak_entity, EntityType.subtype)
            .contains(entityType)) {
      throw new ConfigurationException("Not a valid entity type <%s>".formatted(entityType));
    }
    this.entityType = entityType;
  }
}
