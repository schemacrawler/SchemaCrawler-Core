/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import java.util.stream.Stream;
import schemacrawler.inclusionrule.InclusionRule;
import schemacrawler.schema.Routine;
import schemacrawler.schemacrawler.GrepOptions;

class RoutineGrepFilter extends AbstractGrepFilter<Routine> {

  RoutineGrepFilter(final GrepOptions options) {
    super(options);
  }

  @Override
  protected Stream<String> definitionTexts(final Routine routine) {
    final Stream.Builder<String> definitionTexts = Stream.builder();
    definitionTexts.add(routine.getRemarks());
    definitionTexts.add(routine.getDefinition());
    routine.getParameters().stream()
        .map(parameter -> parameter.getRemarks())
        .forEach(definitionTexts::add);
    return definitionTexts.build();
  }

  @Override
  protected boolean checkIncludeForMembers(final Routine routine) {
    return checkIncludeForParameters(routine);
  }

  @Override
  protected boolean checkIncludeForNamedObjects(final Routine routine) {
    return checkIncludeForRoutines(routine);
  }

  @Override
  protected boolean isGrepForMembers() {
    return options.isGrepRoutineParameters();
  }

  @Override
  protected boolean isGrepForNamedObjects() {
    return options.isGrepRoutines();
  }

  private boolean checkIncludeForParameters(final Routine routine) {
    final InclusionRule rule = options.grepRoutineParameterInclusionRule();
    return checkIncludeForMembers(routine.getParameters(), rule);
  }

  private boolean checkIncludeForRoutines(final Routine routine) {
    return checkIncludeForNamedObject(routine, options.grepRoutineInclusionRule());
  }
}
