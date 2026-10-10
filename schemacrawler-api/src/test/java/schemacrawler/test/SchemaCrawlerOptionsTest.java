/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import schemacrawler.schemacrawler.FilterOptions;
import schemacrawler.schemacrawler.FilterOptionsBuilder;
import schemacrawler.schemacrawler.GrepOptions;
import schemacrawler.schemacrawler.GrepOptionsBuilder;
import schemacrawler.schemacrawler.LimitOptions;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.LoadOptions;
import schemacrawler.schemacrawler.LoadOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;

public class SchemaCrawlerOptionsTest {

  @Test
  public void defaultTitleIsEmpty() {
    final SchemaCrawlerOptions options = SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions();
    assertThat(options.title(), is(""));
    assertThat(options.crawlOptions().title(), is(""));
  }

  @Test
  public void defaultsExposeDirectOptionsAndDerivedCrawlOptions() {
    final SchemaCrawlerOptions options = SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions();

    assertThat(options.loadOptions(), is(LoadOptionsBuilder.newLoadOptions()));
    assertThat(
        options.limitOptions().routineTypes(),
        is(LimitOptionsBuilder.newLimitOptions().routineTypes()));
    assertThat(options.limitOptions().tableNamePattern(), is(nullValue()));
    assertThat(options.filterOptions(), is(FilterOptionsBuilder.newFilterOptions()));
    assertThat(options.grepOptions(), is(GrepOptionsBuilder.newGrepOptions()));
    assertThat(options.crawlOptions().title(), is(options.title()));
    assertThat(options.crawlOptions().loadOptions(), is(options.loadOptions()));
    assertThat(options.crawlOptions().limitOptions(), is(options.limitOptions()));
    assertThat(options.withTitle(options.title()), is(options));
    assertThat(options.crawlOptions(), is(options.crawlOptions()));
  }

  @Test
  public void withTitleTrimsValue() {
    final SchemaCrawlerOptions options =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions().withTitle("  My Title  ");
    assertThat(options.title(), is("My Title"));
  }

  @Test
  public void withersUpdateOptionsThroughTheirGroups() {
    final SchemaCrawlerOptions defaults = SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions();
    final LoadOptions loadOptions =
        LoadOptionsBuilder.builder()
            .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum())
            .toOptions();
    final LimitOptions limitOptions =
        LimitOptionsBuilder.builder().includeAllRoutines().toOptions();
    final FilterOptions filterOptions =
        FilterOptionsBuilder.builder().parentTableFilterDepth(2).toOptions();
    final GrepOptions grepOptions = GrepOptionsBuilder.builder().invertGrepMatch(true).toOptions();

    final SchemaCrawlerOptions options =
        defaults
            .withLoadOptions(loadOptions)
            .withLimitOptions(limitOptions)
            .withFilterOptions(filterOptions)
            .withGrepOptions(grepOptions);

    assertThat(options.loadOptions(), is(loadOptions));
    assertThat(options.limitOptions(), is(limitOptions));
    assertThat(options.filterOptions(), is(filterOptions));
    assertThat(options.grepOptions(), is(grepOptions));
    assertThat(options.crawlOptions().loadOptions(), is(loadOptions));
    assertThat(options.crawlOptions().limitOptions(), is(limitOptions));
    assertThat(defaults.grepOptions().isGrepInvertMatch(), is(false));
  }
}
