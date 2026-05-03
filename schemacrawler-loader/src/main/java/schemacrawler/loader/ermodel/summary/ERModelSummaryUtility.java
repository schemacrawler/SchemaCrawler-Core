/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.ermodel.summary;

import static java.util.Objects.requireNonNull;

import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.ermodel.model.ERModel;
import us.fatehi.utility.UtilityMarker;

/** Utility methods for producing ER model summaries. */
@UtilityMarker
public final class ERModelSummaryUtility {

  private static final Logger LOGGER = Logger.getLogger(ERModelSummaryUtility.class.getName());

  /**
   * Logs a YAML summary of the catalog at the given level.
   *
   * @param erModel ERModel to summarize; may be {@code null} (no-op)
   * @param logLevel Log level; may be {@code null} (no-op)
   */
  public static void logSummary(final ERModel erModel, final Level logLevel) {
    if (erModel == null || logLevel == null) {
      return;
    }
    LOGGER.log(logLevel, () -> summarize(erModel));
  }

  /**
   * Produces a YAML summary of the ER model.
   *
   * @param erModel ER model to summarize; must not be {@code null}
   * @return Valid YAML summary
   */
  public static String summarize(final ERModel erModel) {
    if (erModel == null) {
      return "";
    }
    return ERModelSummaryUtility.toYaml(erModel);
  }

  /**
   * Convenience method that traverses {@code erModel} with a {@link YamlERModelSummaryHandler} and
   * returns the resulting YAML string.
   *
   * @param erModel ER model to summarize; must not be {@code null}
   * @return Valid YAML summary of the ER model
   */
  private static String toYaml(final ERModel erModel) {
    requireNonNull(erModel, "No ER model provided");
    final YamlERModelSummaryHandler handler = new YamlERModelSummaryHandler();
    ERModelSummaryTraverser.traverse(erModel, handler);
    return handler.getYaml();
  }

  private ERModelSummaryUtility() {
    // Prevent instantiation
  }
}
