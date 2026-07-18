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

/**
 * Drives an {@link ERModelSummaryHandler} over an ER model using precomputed {@link ERModelStats}.
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

    final ERModelStats erModelStats = ERModelStatsUtility.from(erModel);

    handler.begin();
    handler.handleERModel(erModelStats);
    handler.end();
  }

  private ERModelSummaryTraverser() {
    // Prevent instantiation
  }
}
