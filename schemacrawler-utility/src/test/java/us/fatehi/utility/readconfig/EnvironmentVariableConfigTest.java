/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.readconfig;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.Map;
import org.junit.jupiter.api.Test;

public class EnvironmentVariableConfigTest {

  @Test
  public void containsKeyPresent() {
    final EnvironmentVariableConfig config = () -> Map.of("MY_KEY", "my-value");
    assertThat(config.containsKey("MY_KEY"), is(true));
  }

  @Test
  public void containsKeyAbsent() {
    final EnvironmentVariableConfig config = () -> Map.of("MY_KEY", "my-value");
    assertThat(config.containsKey("OTHER_KEY"), is(false));
  }

  @Test
  public void containsKeyNullMap() {
    final EnvironmentVariableConfig config = () -> null;
    assertThat(config.containsKey("ANY_KEY"), is(false));
  }

  @Test
  public void getStringValuePresent() {
    final EnvironmentVariableConfig config = () -> Map.of("MY_KEY", "my-value");
    assertThat(config.getStringValue("MY_KEY", "default"), is("my-value"));
  }

  @Test
  public void getStringValueAbsentReturnsDefault() {
    final EnvironmentVariableConfig config = () -> Map.of("MY_KEY", "my-value");
    assertThat(config.getStringValue("OTHER_KEY", "default"), is("default"));
  }

  @Test
  public void getStringValueNullMapReturnsDefault() {
    final EnvironmentVariableConfig config = () -> null;
    assertThat(config.getStringValue("ANY_KEY", "default"), is("default"));
  }

  @Test
  public void getStringValueNullMapReturnsNullDefault() {
    final EnvironmentVariableConfig config = () -> null;
    assertThat(config.getStringValue("ANY_KEY", null), is(nullValue()));
  }
}
