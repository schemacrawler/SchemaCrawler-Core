/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.state;

import schemacrawler.tools.command.BaseCommand;
import us.fatehi.utility.UtilityMarker;

@UtilityMarker
public class ExecutionStateUtility {

  public static void transferState(final ExecutionState from, final ExecutionState to) {
    if (from == null || to == null) {
      return;
    }
    if (from.hasCatalog()) {
      to.setCatalog(from.getCatalog());
    }
    if (from.hasERModel()) {
      to.setERModel(from.getERModel());
    }

    if (to instanceof final BaseCommand command && !command.usesConnection()) {
      return;
    }
    if (from.hasConnectionSource()) {
      to.setConnectionSource(from.getConnectionSource());
    }
  }

  private ExecutionStateUtility() {
    // Prevent instantiation
  }
}
