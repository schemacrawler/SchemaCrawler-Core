/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */
package schemacrawler.loader.ermodel.summary;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import schemacrawler.ermodel.model.ERModel;
import schemacrawler.ermodel.model.Entity;
import schemacrawler.ermodel.model.EntityType;
import schemacrawler.ermodel.model.Relationship;
import schemacrawler.ermodel.model.RelationshipCardinality;
import us.fatehi.utility.UtilityMarker;

/** Utility methods for building {@link ERModelStats}. */
@UtilityMarker
public final class ERModelStatsUtility {

  public static ERModelStats from(final ERModel erModel) {
    requireNonNull(erModel, "No ER model provided");
    return new ERModelStats(
        entityCounts(erModel.getEntities()),
        relationshipCounts(erModel.getRelationships()),
        erModel.getImplicitRelationships().size(),
        erModel.getUnmodeledTables().size());
  }

  private static ERModelStats.EntityCounts entityCounts(final Collection<Entity> entities) {
    int strongEntities = 0;
    int weakEntities = 0;
    int subtypes = 0;
    int nonEntities = 0;
    int unknown = 0;
    for (final Entity entity : entities) {
      final EntityType entityType = entity.getType();
      switch (entityType) {
        case strong_entity -> strongEntities++;
        case weak_entity -> weakEntities++;
        case subtype -> subtypes++;
        case non_entity -> nonEntities++;
        default -> unknown++;
      }
    }
    return new ERModelStats.EntityCounts(
        entities.size(), strongEntities, weakEntities, subtypes, nonEntities, unknown);
  }

  private static ERModelStats.RelationshipCounts relationshipCounts(
      final Collection<Relationship> relationships) {
    int oneOne = 0;
    int oneMany = 0;
    int zeroOne = 0;
    int zeroMany = 0;
    int manyMany = 0;
    int unknown = 0;
    for (final Relationship relationship : relationships) {
      final RelationshipCardinality cardinality = relationship.getType();
      switch (cardinality) {
        case one_one -> oneOne++;
        case one_many -> oneMany++;
        case zero_one -> zeroOne++;
        case zero_many -> zeroMany++;
        case many_many -> manyMany++;
        default -> unknown++;
      }
    }
    return new ERModelStats.RelationshipCounts(
        relationships.size(), oneOne, oneMany, zeroOne, zeroMany, manyMany, unknown);
  }

  private ERModelStatsUtility() {
    // Prevent instantiation
  }
}
