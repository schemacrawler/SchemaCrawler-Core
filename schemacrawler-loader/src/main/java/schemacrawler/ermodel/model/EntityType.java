package schemacrawler.ermodel.model;

/** Represents the entity type of a table. */
public enum EntityType {
  /**
   * Default classification for tables with ambiguous patterns, such as high connectivity, composite
   * FK-based PKs, etc.
   */
  unknown("unknown"),
  /** Tables without a primary key. */
  non_entity("unknown"),
  /**
   * Subtype tables inherit their entire primary key from a single supertype table. The primary key
   * of the subtype table exactly matches the child columns of a foreign key to the supertype table.
   */
  subtype("sub-type"),
  /**
   * Weak entities combine a parent's full primary key (via an identifying foreign key) with their
   * own discriminator column(s). The primary key of the weak entity contains (as a proper subset)
   * the child columns of some foreign key to a parent that exactly map to the parent's primary key.
   */
  weak_entity("weak entity"),
  /**
   * Strong entities have self-sufficient primary keys (no foreign key columns in the primary key)
   * and low referential connectivity to other tables (typically fewer than 2 other tables).
   */
  strong_entity("strong entity"),
  ;

  private final String description;

  private EntityType(String description) {
    this.description = description;
  }

  public String description() {
    return description;
  }
}
