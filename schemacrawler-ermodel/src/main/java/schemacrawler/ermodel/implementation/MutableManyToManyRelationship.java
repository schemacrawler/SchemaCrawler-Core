package schemacrawler.ermodel.implementation;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.ManyToManyRelationship;
import schemacrawler.ermodel.model.RelationshipCardinality;
import schemacrawler.schema.NamedObject;
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

  @Override
  public int hashCode() {
    return key().hashCode();
  }

  void setLeftEntity(final Entity leftEntity) {
    this.leftEntity = requireNonNull(leftEntity, "No left entity provided");
  }

  void setRightEntity(final Entity rightEntity) {
    this.rightEntity = requireNonNull(rightEntity, "No right entity provided");
  }
}
