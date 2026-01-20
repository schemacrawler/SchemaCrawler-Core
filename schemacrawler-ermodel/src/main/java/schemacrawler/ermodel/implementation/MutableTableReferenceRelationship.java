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
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.RelationshipCardinality;
import schemacrawler.ermodel.model.TableReferenceRelationship;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.TableReference;
import schemacrawler.schemacrawler.exceptions.ExecutionRuntimeException;

final class MutableTableReferenceRelationship implements TableReferenceRelationship {

  @Serial private static final long serialVersionUID = 3561028568798848133L;

  private final TableReference tableReference;
  private RelationshipCardinality cardinality;
  private Entity leftEntity;
  private Entity rightEntity;

  public MutableTableReferenceRelationship(final TableReference tableReference) {
    this.tableReference = requireNonNull(tableReference, "No table reference provided");
    cardinality = RelationshipCardinality.unknown;
  }

  @Override
  public int compareTo(final NamedObject o) {
    return tableReference.compareTo(o);
  }

  @Override
  public <T> T getAttribute(final String name) {
    return tableReference.getAttribute(name);
  }

  @Override
  public <T> T getAttribute(final String name, final T defaultValue) throws ClassCastException {
    return tableReference.getAttribute(name, defaultValue);
  }

  @Override
  public Map<String, Object> getAttributes() {
    return tableReference.getAttributes();
  }

  public String getDefinition() {
    return tableReference.getDefinition();
  }

  @Override
  public String getFullName() {
    return tableReference.getFullName();
  }

  @Override
  public Entity getLeftEntity() {
    return leftEntity;
  }

  @Override
  public String getName() {
    return tableReference.getName();
  }

  @Override
  public String getRemarks() {
    return tableReference.getRemarks();
  }

  @Override
  public Entity getRightEntity() {
    return rightEntity;
  }

  @Override
  public TableReference getTableReference() {
    return tableReference;
  }

  @Override
  public RelationshipCardinality getType() {
    return cardinality;
  }

  @Override
  public boolean hasAttribute(final String name) {
    return tableReference.hasAttribute(name);
  }

  @Override
  public boolean hasRemarks() {
    return tableReference.hasRemarks();
  }

  public boolean isOptional() {
    return tableReference.isOptional();
  }

  public boolean isSelfReferencing() {
    return tableReference.isSelfReferencing();
  }

  @Override
  public NamedObjectKey key() {
    return tableReference.key();
  }

  @Override
  public <T> Optional<T> lookupAttribute(final String name) {
    return tableReference.lookupAttribute(name);
  }

  @Override
  public void removeAttribute(final String name) {
    tableReference.removeAttribute(name);
  }

  @Override
  public <T> void setAttribute(final String name, final T value) {
    tableReference.setAttribute(name, value);
  }

  @Override
  public void setRemarks(final String remarks) {
    tableReference.setRemarks(remarks);
  }

  void setCardinality(final RelationshipCardinality cardinality) {
    if (cardinality != null) {
      this.cardinality = cardinality;
    }
  }

  void setLeftEntity(final Entity leftEntity) {
    this.leftEntity = requireNonNull(leftEntity, "No left entity provided");
    if (!leftEntity.key().equals(tableReference.getForeignKeyTable().key())) {
      throw new ExecutionRuntimeException("Table reference left key does not match");
    }
  }

  void setRightEntity(final Entity rightEntity) {
    this.rightEntity = requireNonNull(rightEntity, "No right entity provided");
    if (!rightEntity.key().equals(tableReference.getPrimaryKeyTable().key())) {
      throw new ExecutionRuntimeException("Table reference right key does not match");
    }
  }
}
