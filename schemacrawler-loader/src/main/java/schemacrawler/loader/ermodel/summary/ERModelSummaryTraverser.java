/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.ermodel.summary;

import static java.util.Objects.requireNonNull;

import schemacrawler.ermodel.model.ERModel;
import schemacrawler.loader.ermodel.summary.ERModelSummaryHandler.EntityCounts;
import schemacrawler.loader.ermodel.summary.ERModelSummaryHandler.RelationshipCounts;

/**
 * Drives an {@link ERModelSummaryHandler} over an ER model. Iterates entities and relationships
 * once each, derives aggregate counts, then invokes the handler methods in order.
 */
public final class ERModelSummaryTraverser {

  /**
   * Convenience method that traverses {@code erModel} with a {@link YamlERModelSummaryHandler} and
   * returns the resulting YAML string.
   *
   * @param erModel ER model to summarize; must not be {@code null}
   * @return Valid YAML summary of the ER model
   */
  static String toYaml(final ERModel erModel) {
    requireNonNull(erModel, "No ER model provided");
    final YamlERModelSummaryHandler handler = new YamlERModelSummaryHandler();
    ERModelSummaryTraverser.traverse(erModel, handler);
    return handler.getYaml();
  }

  /**
   * Traverses {@code erModel} and calls {@code handler} methods in order: {@code begin}, {@code
   * handleERModel}, {@code end}.
   *
   * @param erModel ER model to traverse; must not be {@code null}
   * @param handler Handler to receive traversal events; must not be {@code null}
   */
  private static void traverse(final ERModel erModel, final ERModelSummaryHandler handler) {
    requireNonNull(erModel, "No ER model provided");
    requireNonNull(handler, "No handler provided");

    final EntityCounts entityCounts = EntityCounts.from(erModel.getEntities());
    final RelationshipCounts relationshipCounts =
        RelationshipCounts.from(erModel.getRelationships());
    final int implicitCount = erModel.getImplicitRelationships().size();
    final int unmodeledCount = erModel.getUnmodeledTables().size();

    handler.begin();
    handler.handleERModel(entityCounts, relationshipCounts, implicitCount, unmodeledCount);
    handler.end();
  }

  private ERModelSummaryTraverser() {
    // Prevent instantiation
  }
}
