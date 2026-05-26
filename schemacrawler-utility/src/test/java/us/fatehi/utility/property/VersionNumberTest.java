/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.property;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class VersionNumberTest {

  @Test
  public void validVersionNumber() {
    final VersionNumber version = new VersionNumber(2, 15);
    assertThat(version.major(), is(2));
    assertThat(version.minor(), is(15));
    assertThat(version.toString(), is("2.15"));
  }

  @Test
  public void zeroVersionNumber() {
    final VersionNumber version = new VersionNumber(0, 0);
    assertThat(version.major(), is(0));
    assertThat(version.minor(), is(0));
    assertThat(version.toString(), is("0.0"));
  }

  @Test
  public void negativeMajorThrows() {
    assertThrows(IllegalArgumentException.class, () -> new VersionNumber(-1, 0));
  }

  @Test
  public void negativeMinorThrows() {
    assertThrows(IllegalArgumentException.class, () -> new VersionNumber(1, -1));
  }

  @Test
  public void toStringFormat() {
    final VersionNumber version = new VersionNumber(17, 11);
    assertThat(version.toString(), is("17.11"));
  }
}
