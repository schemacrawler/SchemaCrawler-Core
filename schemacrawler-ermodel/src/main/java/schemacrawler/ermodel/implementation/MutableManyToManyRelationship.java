package schemacrawler.ermodel.implementation;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.ManyToManyRelationship;
import schemacrawler.ermodel.model.RelationshipCardinality;
import schemacrawler.schema.Table;

final class MutableManyToManyRelationship extends AbstractTableBacked
    implements ManyToManyRelationship {

  @Serial private static final long serialVersionUID = 867546565892159921L;

  private Entity leftEntity;
  private Entity rightEntity;

  public MutableManyToManyRelationship(final Table table) {
    super(table);
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
  public RelationshipCardinality getType() {
    return RelationshipCardinality.many_many;
  }

  void setLeftEntity(final Entity leftEntity) {
    this.leftEntity = requireNonNull(leftEntity, "No left entity provided");
  }

  void setRightEntity(final Entity rightEntity) {
    this.rightEntity = requireNonNull(rightEntity, "No right entity provided");
  }
}
