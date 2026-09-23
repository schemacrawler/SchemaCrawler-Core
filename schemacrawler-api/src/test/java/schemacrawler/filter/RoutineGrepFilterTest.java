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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import schemacrawler.inclusionrule.InclusionRule;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schema.Routine;
import schemacrawler.schemacrawler.GrepOptions;
import schemacrawler.schemacrawler.GrepOptionsBuilder;
import schemacrawler.test.utility.crawl.LightProcedure;
import schemacrawler.test.utility.crawl.LightProcedureParameter;

class RoutineGrepFilterTest {

  private Routine routine;

  @BeforeEach
  public void setUp() {
    final LightProcedure routine = new LightProcedure("test_routine");
    routine.setDefinition("test_definition");
    routine.setRemarks("test_remarks");

    final LightProcedureParameter parameter = new LightProcedureParameter(routine, "test_param");
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

  /**
   * EXPOSES A BUG: "grep routine parameters" is being relied on elsewhere (for example, by
   * SchemaCrawler AI's DescribeRoutinesFunctionExecutor) as a way to match a routine by its own
   * name. But a routine with no parameters can never match, no matter how well its name matches
   * the inclusion rule, because there are no parameter full names to test against. This currently
   * FAILS, documenting the bug - a parameterless routine whose own name matches the rule is
   * incorrectly excluded.
   */
  @Test
  @Disabled
  void testRoutineGrepFilterWithNoParametersDoesNotMatchRoutineName() {
    final LightProcedure noParamRoutine = new LightProcedure("no_param_routine");
    final InclusionRule rule = new RegularExpressionInclusionRule(".*no_param_routine.*");
    final GrepOptions grepOptions =
        GrepOptionsBuilder.builder().includeGreppedRoutineParameters(rule).toOptions();
    final RoutineGrepFilter filter = new RoutineGrepFilter(grepOptions);

    assertThat(filter.test(noParamRoutine), is(true));
  }
}
