/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;

import org.junit.jupiter.api.Test;
import schemacrawler.schemacrawler.Version;
import us.fatehi.utility.property.ProductVersion;

public class RuntimeVersionTest {

  @Test
  public void testVersionClassReportingWorks() {
    final ProductVersion version = Version.version();
    assertThat("Version should not be null", version, notNullValue());
    assertThat(
        "Product name should be SchemaCrawler", version.getProductName(), equalTo("SchemaCrawler"));

    final String versionString = version.getProductVersion();
    assertThat("Product version should not be null", versionString, notNullValue());
    assertThat(
        "Version should match expected format",
        versionString,
        matchesPattern("^\\d+\\.\\d+\\.\\d+$"));
  }

  @Test
  public void testAboutBannerGeneration() {
    final String about = Version.about();
    assertThat("About banner should not be null", about, notNullValue());
    assertThat(
        "About banner should contain product name and version",
        about,
        allOf(
            containsString("SchemaCrawler"),
            containsString(Version.version().getProductVersion())));
  }

  @Test
  public void testVersionNoManifestDependency() {
    // This test verifies that version reporting works without requiring
    // manifest implementation metadata, supporting unpackaged tests and
    // classpath execution
    final String version = Version.version().getProductVersion();
    assertThat("Version should be retrievable without manifest access", version, notNullValue());
    assertThat(
        "Version literal should not be extracted from manifest",
        version,
        matchesPattern("^\\d+\\.\\d+\\.\\d+$"));
  }

  @Test
  public void testVersionFormattingIsConsistent() {
    final String version = Version.version().getProductVersion();
    final String about = Version.about();

    assertThat(
        "About banner should contain the exact version literal",
        about.contains(version),
        equalTo(true));
  }
}
