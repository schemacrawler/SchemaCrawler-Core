/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static java.util.regex.Pattern.DOTALL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesRegex;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import schemacrawler.schemacrawler.Version;

public class VersionTest {

  private static final String majorVersion = "17";
  private static final String semverPatternString =
      majorVersion
          + "\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?";

  @Test
  public void version() throws Exception {
    final Pattern VERSION =
        Pattern.compile("SchemaCrawler " + semverPatternString + "\\R.*", DOTALL);

    final String about = Version.about();

    assertThat(about, matchesRegex(VERSION));
  }
}
