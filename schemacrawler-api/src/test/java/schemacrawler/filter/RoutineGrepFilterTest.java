/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import schemacrawler.inclusionrule.InclusionRule;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schema.Routine;
import schemacrawler.schemacrawler.GrepOptions;
import schemacrawler.schemacrawler.GrepOptionsBuilder;
import schemacrawler.test.utility.crawl.LightRoutine;
import schemacrawler.test.utility.crawl.LightRoutineParameter;

class RoutineGrepFilterTest {

  private Routine routine;

  @BeforeEach
  public void setUp() {
    final LightRoutine routine = new LightRoutine("test_routine");
    routine.setDefinition("test_definition");
    routine.setRemarks("test_remarks");

    final LightRoutineParameter parameter = new LightRoutineParameter(routine, "test_param");
    parameter.setRemarks("test_param_remarks");
    routine.addParameter(parameter);

    this.routine = routine;
  }

  @Test
  void testRoutineGrepFilter() {
    final GrepOptions grepOptions = GrepOptionsBuilder.builder().toOptions();
    final RoutineGrepFilter filter = new RoutineGrepFilter(grepOptions);

    assertThat(filter.test(routine), is(true));
  }

  @Test
  void testRoutineGrepFilterWithJustInvertMatch() {
    final GrepOptions grepOptions = GrepOptionsBuilder.builder().invertGrepMatch(true).toOptions();
    final RoutineGrepFilter filter = new RoutineGrepFilter(grepOptions);

    assertThat(filter.test(routine), is(true));
  }

  @Test
  void testRoutineGrepFilterWithParameterInclusionRule() {
    final InclusionRule rule = new RegularExpressionInclusionRule("test_routine\\.test_param");
    final GrepOptions grepOptions =
        GrepOptionsBuilder.builder().includeGreppedRoutineParameters(rule).toOptions();
    final RoutineGrepFilter filter = new RoutineGrepFilter(grepOptions);

    assertThat(filter.test(routine), is(true));
  }

  @Test
  void testRoutineGrepFilterWithNonMatchingParameterInclusionRule() {
    final InclusionRule rule = new RegularExpressionInclusionRule("test_routine\\.other_param");
    final GrepOptions grepOptions =
        GrepOptionsBuilder.builder().includeGreppedRoutineParameters(rule).toOptions();
    final RoutineGrepFilter filter = new RoutineGrepFilter(grepOptions);

    assertThat(filter.test(routine), is(false));
  }

  @Test
  void testRoutineGrepFilterWithDefinitionInclusionRuleOnRemarks() {
    final InclusionRule rule = new RegularExpressionInclusionRule("test_remarks");
    final GrepOptions grepOptions =
        GrepOptionsBuilder.builder().includeGreppedDefinitions(rule).toOptions();
    final RoutineGrepFilter filter = new RoutineGrepFilter(grepOptions);

    assertThat(filter.test(routine), is(true));
  }

  @Test
  void testRoutineGrepFilterWithDefinitionInclusionRuleOnDefinition() {
    final InclusionRule rule = new RegularExpressionInclusionRule("test_definition");
    final GrepOptions grepOptions =
        GrepOptionsBuilder.builder().includeGreppedDefinitions(rule).toOptions();
    final RoutineGrepFilter filter = new RoutineGrepFilter(grepOptions);

    assertThat(filter.test(routine), is(true));
  }

  @Test
  void testRoutineGrepFilterWithDefinitionInclusionRuleOnParameterRemarks() {
    final InclusionRule rule = new RegularExpressionInclusionRule("test_param_remarks");
    final GrepOptions grepOptions =
        GrepOptionsBuilder.builder().includeGreppedDefinitions(rule).toOptions();
    final RoutineGrepFilter filter = new RoutineGrepFilter(grepOptions);

    assertThat(filter.test(routine), is(true));
  }

  @Test
  void testRoutineGrepFilterWithNonMatchingDefinitionInclusionRule() {
    final InclusionRule rule = new RegularExpressionInclusionRule("non_matching");
    final GrepOptions grepOptions =
        GrepOptionsBuilder.builder().includeGreppedDefinitions(rule).toOptions();
    final RoutineGrepFilter filter = new RoutineGrepFilter(grepOptions);

    assertThat(filter.test(routine), is(false));
  }

  @Test
  void testRoutineGrepFilterWithInvertMatch() {
    final InclusionRule rule = new RegularExpressionInclusionRule("test_routine\\.test_param");
    final GrepOptions grepOptions =
        GrepOptionsBuilder.builder()
            .includeGreppedRoutineParameters(rule)
            .invertGrepMatch(true)
            .toOptions();
    final RoutineGrepFilter filter = new RoutineGrepFilter(grepOptions);

    assertThat(filter.test(routine), is(false));
  }

  @Test
  void testRoutineGrepFilterWithInvertMatchForNoMatch() {
    final InclusionRule rule = new RegularExpressionInclusionRule("test_routine\\.other_param");
    final GrepOptions grepOptions =
        GrepOptionsBuilder.builder()
            .includeGreppedRoutineParameters(rule)
            .invertGrepMatch(true)
            .toOptions();
    final RoutineGrepFilter filter = new RoutineGrepFilter(grepOptions);

    assertThat(filter.test(routine), is(true));
  }
}
