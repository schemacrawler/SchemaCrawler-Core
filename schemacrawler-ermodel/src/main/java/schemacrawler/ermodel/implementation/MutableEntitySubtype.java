package schemacrawler.ermodel.implementation;

import static java.util.Objects.requireNonNull;
import static schemacrawler.ermodel.model.EntityType.subtype;

import java.io.Serial;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.EntitySubtype;
import schemacrawler.schema.Table;

final class MutableEntitySubtype extends MutableEntity implements EntitySubtype {

  @Serial private static final long serialVersionUID = 8340763685998040751L;

  private Entity supertype;

  public MutableEntitySubtype(final Table table) {
    super(table, subtype);
  }

  @Override
  public Entity getSupertype() {
    return supertype;
  }

  @Override
  public boolean hasSupertype() {
    return supertype != null;
  }

  void setSupertype(final Entity supertype) {
    this.supertype = requireNonNull(supertype, "No supertype provided");
    // No additional check for matching primary key columns
  }
}
