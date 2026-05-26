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

import org.junit.jupiter.api.Test;
import us.fatehi.test.utility.extensions.WithSystemProperty;

public class SystemConfigTest {

  private static final String TEST_KEY = "schemacrawler.test.syscfg";

  @Test
  @WithSystemProperty(key = TEST_KEY, value = "sys-value")
  public void containsKeyFromSystemProperty() {
    final SystemConfig config = new SystemConfig();
    assertThat(config.containsKey(TEST_KEY), is(true));
  }

  @Test
  public void doesNotContainMissingKey() {
    final SystemConfig config = new SystemConfig();
    assertThat(config.containsKey("schemacrawler.test.nonexistent.syscfg"), is(false));
  }

  @Test
  @WithSystemProperty(key = TEST_KEY, value = "sys-value")
  public void getStringValueFromSystemProperty() {
    final SystemConfig config = new SystemConfig();
    assertThat(config.getStringValue(TEST_KEY, "default"), is("sys-value"));
  }

  @Test
  public void getStringValueMissingKeyReturnsDefault() {
    final SystemConfig config = new SystemConfig();
    assertThat(
        config.getStringValue("schemacrawler.test.nonexistent.syscfg", "fallback"), is("fallback"));
  }

  @Test
  public void getStringValueMissingKeyReturnsEmptyStringWhenDefaultNull() {
    final SystemConfig config = new SystemConfig();
    // When key not found and defaultValue is null, returns empty string
    assertThat(config.getStringValue("schemacrawler.test.nonexistent.syscfg", null), is(""));
  }
}
