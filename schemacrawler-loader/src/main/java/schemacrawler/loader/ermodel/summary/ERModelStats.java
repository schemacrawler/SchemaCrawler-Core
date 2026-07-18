/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */
package schemacrawler.loader.ermodel.summary;

/**
 * Immutable ER model summary statistics.
 *
 * @param entityCounts breakdown of entities by classification type
 * @param relationshipCounts breakdown of relationships by cardinality
 * @param implicitRelationshipCount relationships derived from foreign keys that are not explicitly
 *     represented as ER model relationships
 * @param unmodeledTableCount tables present in the catalog that could not be classified as any
 *     entity type in the ER model
 */
public record ERModelStats(
    EntityCounts entityCounts,
    RelationshipCounts relationshipCounts,
    int implicitRelationshipCount,
    int unmodeledTableCount) {

  /**
   * Counts of entities by classification type.
   *
   * @param count total number of entities
   * @param strongEntities entities with their own primary key
   * @param weakEntities entities whose identity depends on a parent entity
   * @param subtypes entities that specialise a parent entity (ISA / inheritance)
   * @param nonEntities table objects in the ER model that are not classified as entities (e.g.
   *     bridge/association tables)
   * @param unknown entities whose type could not be determined
   */
  public record EntityCounts(
      int count,
      int strongEntities,
      int weakEntities,
      int subtypes,
      int nonEntities,
      int unknown) {}

  /**
   * Counts of relationships by cardinality.
   *
   * @param count total number of relationships
   * @param oneOne one-to-one relationships
   * @param oneMany one-to-many relationships
   * @param zeroOne zero-or-one (optional-one) relationships
   * @param zeroMany zero-or-many (optional-many) relationships
   * @param manyMany many-to-many relationships
   * @param unknown relationships whose cardinality could not be determined
   */
  public record RelationshipCounts(
      int count, int oneOne, int oneMany, int zeroOne, int zeroMany, int manyMany, int unknown) {}
}
