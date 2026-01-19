package schemacrawler.ermodel.implementation;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.EntitySubtype;
import schemacrawler.schema.Table;

final class MutableEntitySubtype extends MutableEntity implements EntitySubtype {

  @Serial private static final long serialVersionUID = 8340763685998040751L;

  private Entity supertype;

  public MutableEntitySubtype(final Table table) {
    super(table);
  }

  @Override
  public Entity getSupertype() {
    return supertype;
  }

  void setSupertype(final Entity supertype) {
    this.supertype = requireNonNull(supertype, "No syper-type provided");
    // TODO: Add additional check for matching primary key columns
  }
}
