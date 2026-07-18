/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.loader.ermodel.summary;

/**
 * Handler interface for ER model summary traversal. Implementations receive structured count data
 * for entities, relationships, and unmodeled tables through {@link ERModelStats}.
 */
public interface ERModelSummaryHandler {

  void begin();

  void end();

  void handleERModel(ERModelStats erModelStats);
}
