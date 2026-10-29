/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import schemacrawler.schemacrawler.Version;

public class VersionConsistencyTest {

  @Test
  public void testCoreProductVersionExists() {
    final Version version = Version.version();
    assertThat("Version should not be null", version, notNullValue());
    assertThat(
        "Product name should be SchemaCrawler", version.getProductName(), equalTo("SchemaCrawler"));
    assertThat("Version number should not be null", version.getProductVersion(), notNullValue());
  }

  @Test
  public void testCoreVersionInAboutBanner() {
    final String about = Version.about();
    assertThat("About banner should not be null", about, notNullValue());

    // The version should appear in the about banner
    final String productVersion = Version.version().getProductVersion();
    assertThat(
        "About banner should contain product version",
        about.contains(productVersion),
        equalTo(true));
  }

  @Test
  public void testEffectiveMavenVersionMatches() {
    final String projectVersion = System.getProperty("project.version");
    if (projectVersion != null) {
      final String javaVersion = Version.version().getProductVersion();
      assertThat(
          "Java product version should match Maven project version",
          javaVersion,
          equalTo(projectVersion));
    }
  }
}
