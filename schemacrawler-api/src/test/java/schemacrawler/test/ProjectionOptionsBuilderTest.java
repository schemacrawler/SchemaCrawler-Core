/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import schemacrawler.schemacrawler.FilterOptionsBuilder;
import schemacrawler.schemacrawler.GrepOptionsBuilder;
import schemacrawler.schemacrawler.ProjectionOptions;
import schemacrawler.schemacrawler.ProjectionOptionsBuilder;

public class ProjectionOptionsBuilderTest {

  @Test
  public void defaults() {
    final ProjectionOptions options = ProjectionOptionsBuilder.newProjectionOptions();

    assertThat(options.filterOptions(), is(FilterOptionsBuilder.newFilterOptions()));
    assertThat(options.grepOptions(), is(GrepOptionsBuilder.newGrepOptions()));
  }

  @Test
  public void fromOptions() {
    final ProjectionOptions options =
        ProjectionOptionsBuilder.builder()
            .withFilterOptions(
                FilterOptionsBuilder.builder()
                    .childTableFilterDepth(1)
                    .parentTableFilterDepth(2)
                    .toOptions())
            .withGrepOptions(GrepOptionsBuilder.builder().invertGrepMatch(true).toOptions())
            .toOptions();

    assertThat(ProjectionOptionsBuilder.builder().fromOptions(options).toOptions(), is(options));
  }

  @Test
  public void nullOptionsKeepDefaults() {
    final ProjectionOptions defaults = ProjectionOptionsBuilder.newProjectionOptions();
    final ProjectionOptions options =
        ProjectionOptionsBuilder.builder()
            .withFilterOptions(null)
            .withGrepOptions(null)
            .fromOptions(null)
            .toOptions();

    assertThat(options, is(defaults));
  }
}
