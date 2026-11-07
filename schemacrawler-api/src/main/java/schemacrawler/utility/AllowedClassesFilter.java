/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.utility;

import static us.fatehi.utility.Utility.isBlank;

import java.util.function.Predicate;
import us.fatehi.utility.Utility;

final class AllowedClassesFilter implements Predicate<String> {

  private static final Predicate<String> ALLOWED_CLASS =
      Utility.patternsToPredicate(
          "(\\[L)?java\\.(lang|util)\\..*", "java\\.(sql|math|time|net)\\..*", "\\[[BC]");

  @Override
  public boolean test(final String className) {
    if (isBlank(className)) {
      return true;
    }
    return ALLOWED_CLASS.test(className);
  }
}
