/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.inclusionrule.InclusionRule;
import schemacrawler.schema.Routine;
import schemacrawler.schemacrawler.GrepOptions;
import us.fatehi.utility.string.StringFormat;

class RoutineGrepFilter implements Predicate<Routine> {

  private static final Logger LOGGER = Logger.getLogger(RoutineGrepFilter.class.getName());

  private final GrepOptions options;

  public RoutineGrepFilter(final GrepOptions options) {
    this.options = requireNonNull(options, "No grep options provided");
  }

  /**
   * Special case for "grep" like functionality. Handle routine if a routine parameter inclusion
   * rule is found, and at least one parameter matches the rule.
   *
   * @param routine Routine to check
   * @return Whether the routine should be included
   */
  @Override
  public boolean test(final Routine routine) {
    final boolean checkIncludeForParameters = options.isGrepRoutineParameters();
    final boolean checkIncludeForDefinitions = options.isGrepDefinitions();

    if (!checkIncludeForParameters && !checkIncludeForDefinitions) {
      if (options.isGrepInvertMatch()) {
        LOGGER.log(
            Level.FINE,
            new StringFormat(
                "Ignoring the invert match setting for routine <%s>, "
                    + "since no inclusion rules are set",
                routine));
      }
      return true;
    }

    final boolean includeForParameters =
        checkIncludeForParameters && checkIncludeForParameters(routine);
    final boolean includeForDefinitions =
        checkIncludeForDefinitions && checkIncludeForDefinitions(routine);

    boolean include = includeForParameters || includeForDefinitions;
    if (options.isGrepInvertMatch()) {
      include = !include;
    }

    if (!include) {
      LOGGER.log(Level.FINE, new StringFormat("Excluding routine <%s>", routine));
    }
    return include;
  }

  private boolean checkIncludeForDefinitions(final Routine routine) {
    final InclusionRule rule = options.grepDefinitionInclusionRule();
    return rule.test(routine.getRemarks())
        || rule.test(routine.getDefinition())
        || routine.getParameters().stream().anyMatch(p -> rule.test(p.getRemarks()));
  }

  private boolean checkIncludeForParameters(final Routine routine) {
    final InclusionRule rule = options.grepRoutineParameterInclusionRule();
    return routine.getParameters().stream().anyMatch(p -> rule.test(p.getFullName()));
  }
}
