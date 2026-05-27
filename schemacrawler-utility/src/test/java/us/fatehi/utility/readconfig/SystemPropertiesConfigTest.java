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

public class SystemPropertiesConfigTest {

  private static final String TEST_KEY = "schemacrawler.test.sysprop";

  @Test
  @WithSystemProperty(key = TEST_KEY, value = "test-value")
  public void containsKnownKey() {
    final SystemPropertiesConfig config = new SystemPropertiesConfig();
    assertThat(config.containsKey(TEST_KEY), is(true));
  }

  @Test
  public void doesNotContainMissingKey() {
    final SystemPropertiesConfig config = new SystemPropertiesConfig();
    assertThat(config.containsKey("schemacrawler.test.nonexistent"), is(false));
  }

  @Test
  @WithSystemProperty(key = TEST_KEY, value = "test-value")
  public void getStringValueForKnownKey() {
    final SystemPropertiesConfig config = new SystemPropertiesConfig();
    assertThat(config.getStringValue(TEST_KEY, "default"), is("test-value"));
  }

  @Test
  public void getStringValueForMissingKeyReturnsDefault() {
    final SystemPropertiesConfig config = new SystemPropertiesConfig();
    assertThat(config.getStringValue("schemacrawler.test.nonexistent", "fallback"), is("fallback"));
  }

  @Test
  public void getStringValueForNullKeyReturnsDefault() {
    final SystemPropertiesConfig config = new SystemPropertiesConfig();
    assertThat(config.getStringValue(null, "fallback"), is("fallback"));
  }
}
