package schemacrawler.schema;

/** Represents the entity type of a table. */
public enum EntityType {
  unknown,
  non_entity,
  subtype,
  weak_entity,
  strong_entity
}
