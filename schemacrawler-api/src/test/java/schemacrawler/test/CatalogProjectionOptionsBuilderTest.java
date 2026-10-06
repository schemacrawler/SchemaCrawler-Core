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
import schemacrawler.schemacrawler.CatalogProjectionOptions;
import schemacrawler.schemacrawler.CatalogProjectionOptionsBuilder;
import schemacrawler.schemacrawler.FilterOptionsBuilder;
import schemacrawler.schemacrawler.GrepOptionsBuilder;

public class CatalogProjectionOptionsBuilderTest {

  @Test
  public void defaults() {
    final CatalogProjectionOptions options =
        CatalogProjectionOptionsBuilder.newCatalogProjectionOptions();

    assertThat(options.filterOptions(), is(FilterOptionsBuilder.newFilterOptions()));
    assertThat(options.grepOptions(), is(GrepOptionsBuilder.newGrepOptions()));
  }

  @Test
  public void fromOptions() {
    final CatalogProjectionOptions options =
        CatalogProjectionOptionsBuilder.builder()
            .withFilterOptions(
                FilterOptionsBuilder.builder()
                    .childTableFilterDepth(1)
                    .parentTableFilterDepth(2)
                    .toOptions())
            .withGrepOptions(GrepOptionsBuilder.builder().invertGrepMatch(true).toOptions())
            .toOptions();

    assertThat(
        CatalogProjectionOptionsBuilder.builder().fromOptions(options).toOptions(), is(options));
  }

  @Test
  public void nullOptionsKeepDefaults() {
    final CatalogProjectionOptions defaults =
        CatalogProjectionOptionsBuilder.newCatalogProjectionOptions();
    final CatalogProjectionOptions options =
        CatalogProjectionOptionsBuilder.builder()
            .withFilterOptions(null)
            .withGrepOptions(null)
            .fromOptions(null)
            .toOptions();

    assertThat(options, is(defaults));
  }
}
