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
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.RelationshipCardinality;
import schemacrawler.ermodel.model.TableReferenceRelationship;
import schemacrawler.schema.TableReference;
import schemacrawler.schemacrawler.exceptions.ExecutionRuntimeException;

final class MutableTableReferenceRelationship extends AbstractDatabaseObjectBacked<TableReference>
    implements TableReferenceRelationship {

  @Serial private static final long serialVersionUID = 3561028568798848133L;

  private RelationshipCardinality cardinality;
  private Entity leftEntity;
  private Entity rightEntity;

  public MutableTableReferenceRelationship(final TableReference tableReference) {
    super(tableReference);
    cardinality = RelationshipCardinality.unknown;
  }

  @Override
  public Entity getLeftEntity() {
    return leftEntity;
  }

  @Override
  public Entity getRightEntity() {
    return rightEntity;
  }

  @Override
  public TableReference getTableReference() {
    return getDatabaseObject();
  }

  @Override
  public RelationshipCardinality getType() {
    return cardinality;
  }

  void setCardinality(final RelationshipCardinality cardinality) {
    if (cardinality != null) {
      this.cardinality = cardinality;
    }
  }

  void setLeftEntity(final Entity leftEntity) {
    this.leftEntity = requireNonNull(leftEntity, "No left entity provided");
    if (!leftEntity.key().equals(getTableReference().getForeignKeyTable().key())) {
      throw new ExecutionRuntimeException("Table reference left key does not match");
    }
  }

  void setRightEntity(final Entity rightEntity) {
    this.rightEntity = requireNonNull(rightEntity, "No right entity provided");
    if (!rightEntity.key().equals(getTableReference().getPrimaryKeyTable().key())) {
      throw new ExecutionRuntimeException("Table reference right key does not match");
    }
  }
}
