/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.ermodel.summary;

/** Produces a valid, parseable YAML summary of an ER model. */
final class YamlERModelSummaryHandler implements ERModelSummaryHandler {

  private final StringBuilder sb = new StringBuilder();

  @Override
  public void begin() {
    // Nothing to do
  }

  @Override
  public void end() {
    // Nothing to do
  }

  @Override
  public void handleERModel(
      final EntityCounts entityCounts,
      final RelationshipCounts relationshipCounts,
      final int implicitRelationshipCount,
      final int unmodeledTableCount) {
    sb.append("ermodel:\n");
    sb.append("  entities:\n");
    count("    ", "count", entityCounts.count());
    count("    ", "strong-entities", entityCounts.strongEntities());
    count("    ", "weak-entities", entityCounts.weakEntities());
    count("    ", "subtypes", entityCounts.subtypes());
    count("    ", "non-entities", entityCounts.nonEntities());
    count("    ", "unknown", entityCounts.unknown());
    sb.append("  relationships:\n");
    count("    ", "count", relationshipCounts.count());
    count("    ", "one-to-one", relationshipCounts.oneOne());
    count("    ", "one-to-many", relationshipCounts.oneMany());
    count("    ", "zero-to-one", relationshipCounts.zeroOne());
    count("    ", "zero-to-many", relationshipCounts.zeroMany());
    count("    ", "many-to-many", relationshipCounts.manyMany());
    count("    ", "unknown", relationshipCounts.unknown());
    sb.append("  implicit-relationships:\n");
    count("    ", "count", implicitRelationshipCount);
    sb.append("  unmodeled-tables:\n");
    count("    ", "count", unmodeledTableCount);
  }

  String getYaml() {
    return sb.toString();
  }

  private void count(final String indent, final String key, final int n) {
    sb.append(indent).append(key).append(": ").append(n).append("\n");
  }
}
