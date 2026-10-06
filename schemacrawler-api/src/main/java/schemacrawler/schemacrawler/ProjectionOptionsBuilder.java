/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.schemacrawler;

import static java.util.Objects.requireNonNull;

import us.fatehi.utility.OptionsBuilder;

/** Builds immutable options for selecting a catalog projection. */
public final class ProjectionOptionsBuilder
    implements OptionsBuilder<ProjectionOptionsBuilder, ProjectionOptions> {

  public static ProjectionOptionsBuilder builder() {
    return new ProjectionOptionsBuilder();
  }

  public static ProjectionOptions newProjectionOptions() {
    return builder().toOptions();
  }

  private FilterOptions filterOptions;
  private GrepOptions grepOptions;

  private ProjectionOptionsBuilder() {
    filterOptions = FilterOptionsBuilder.newFilterOptions();
    grepOptions = GrepOptionsBuilder.newGrepOptions();
  }

  @Override
  public ProjectionOptionsBuilder fromOptions(final ProjectionOptions options) {
    if (options != null) {
      filterOptions = options.filterOptions();
      grepOptions = options.grepOptions();
    }
    return this;
  }

  public ProjectionOptionsBuilder withFilterOptions(final FilterOptions filterOptions) {
    if (filterOptions != null) {
      this.filterOptions = filterOptions;
    }
    return this;
  }

  public ProjectionOptionsBuilder withGrepOptions(final GrepOptions grepOptions) {
    if (grepOptions != null) {
      this.grepOptions = grepOptions;
    }
    return this;
  }

  @Override
  public ProjectionOptions toOptions() {
    return new ProjectionOptions(
        requireNonNull(filterOptions, "No filter options provided"),
        requireNonNull(grepOptions, "No grep options provided"));
  }
}
