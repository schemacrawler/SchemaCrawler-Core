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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import us.fatehi.test.utility.extensions.WithSystemProperty;

public class SystemConfigTest {

  private static final String TEST_KEY = "schemacrawler.test.syscfg";

  @Test
  public void aliasesPreserveSourcePrecedence() {
    final ReadConfig sys =
        (EnvironmentVariableConfig) () -> Map.of("canonical", "sys", "legacy", "legacy-sys");
    final ReadConfig legacySys = (EnvironmentVariableConfig) () -> Map.of("legacy", "legacy-sys");
    final ReadConfig env =
        (EnvironmentVariableConfig) () -> Map.of("canonical", "env", "legacy", "legacy-env");
    final SystemConfigBuilder builder =
        SystemConfigBuilder.builder()
            .withAlias("canonical", "legacy")
            .withEnvironmentVariables(env);
    assertThat(builder.withSystemProperties(sys).build().getStringValue("canonical"), is("sys"));
    assertThat(
        builder.withSystemProperties(legacySys).build().getStringValue("canonical"),
        is("legacy-sys"));
    final ReadConfig empty = (EnvironmentVariableConfig) Map::of;
    assertThat(builder.withSystemProperties(empty).build().getStringValue("canonical"), is("env"));
    assertThat(
        builder.withEnvironmentVariables(legacySys).build().getStringValue("canonical"),
        is("legacy-sys"));
    final SystemConfig unset = builder.withEnvironmentVariables(empty).build();
    assertThat(unset.containsKey("canonical"), is(false));
    assertThat(unset.getStringValue("canonical", "default"), is("default"));
  }

  @Test
  public void aliasDefinitionsAreSnapshotsButValuesStayLive() {
    final Map<String, String> values = new HashMap<>();
    values.put("first", "one");
    values.put("second", "two");
    final String[] aliases = {"first", "second"};
    final SystemConfigBuilder builder =
        SystemConfigBuilder.builder()
            .withEnvironmentVariables((EnvironmentVariableConfig) () -> values)
            .withAlias("canonical", aliases);
    final SystemConfig config = builder.build();
    aliases[0] = "replacement";
    builder.withAlias("canonical", "replacement");
    assertThat(config.containsKey("canonical"), is(true));
    assertThat(config.getStringValue("canonical"), is("one"));
    values.remove("first");
    assertThat(config.getStringValue("canonical"), is("two"));
    values.put("canonical", "live");
    assertThat(config.getStringValue("canonical"), is("live"));
  }

  @Test
  public void aliasesIgnoreBlankNames() {
    final ReadConfig source =
        (EnvironmentVariableConfig) () -> Map.of("legacy", "value", "", "blank");
    final SystemConfig config =
        SystemConfigBuilder.builder()
            .withEnvironmentVariables(source)
            .withAlias("canonical", null, "", " ", "legacy")
            .build();
    assertThat(config.getStringValue("canonical"), is("value"));
  }

  @Test
  public void builderRequiresSourceSelection() {
    assertThrows(IllegalStateException.class, () -> SystemConfigBuilder.builder().build());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "false", "0", "not-a-number"})
  public void canonicalPresenceDoesNotFallBack(final String value) {
    final ReadConfig source =
        (EnvironmentVariableConfig) () -> Map.of("canonical", value, "legacy", "42");
    final SystemConfig config =
        SystemConfigBuilder.builder()
            .withEnvironmentVariables(source)
            .withAlias("canonical", "legacy")
            .build();
    assertThat(config.getStringValue("canonical"), is(value));
    assertThat(config.getBooleanValue("canonical"), is(false));
    assertThat(config.getIntegerValue("canonical", 5), is("0".equals(value) ? 0 : 5));
  }

  @Test
  @WithSystemProperty(key = TEST_KEY, value = "sys-value")
  public void sourceSelectionIsExplicit() {
    final ReadConfig env = (EnvironmentVariableConfig) () -> Map.of(TEST_KEY, "env-value");
    assertThat(
        SystemConfigBuilder.builder()
            .withEnvironmentVariables(env)
            .build()
            .getStringValue(TEST_KEY),
        is("env-value"));
    assertThat(
        SystemConfigBuilder.builder().withSystemProperties().build().getStringValue(TEST_KEY),
        is("sys-value"));
    assertThat(
        SystemConfigBuilder.builder()
            .withEnvironmentVariables(env)
            .withSystemProperties()
            .build()
            .getStringValue(TEST_KEY),
        is("sys-value"));
  }

  @Test
  @WithSystemProperty(key = TEST_KEY, value = "sys-value")
  public void containsKeyFromSystemProperty() {
    final SystemConfig config =
        SystemConfigBuilder.builder().withSystemProperties().withEnvironmentVariables().build();
    assertThat(config.containsKey(TEST_KEY), is(true));
  }

  @Test
  public void doesNotContainMissingKey() {
    final SystemConfig config =
        SystemConfigBuilder.builder().withSystemProperties().withEnvironmentVariables().build();
    assertThat(config.containsKey("schemacrawler.test.nonexistent.syscfg"), is(false));
  }

  @Test
  @WithSystemProperty(key = TEST_KEY, value = "sys-value")
  public void getStringValueFromSystemProperty() {
    final SystemConfig config =
        SystemConfigBuilder.builder().withSystemProperties().withEnvironmentVariables().build();
    assertThat(config.getStringValue(TEST_KEY, "default"), is("sys-value"));
  }

  @Test
  public void getStringValueMissingKeyReturnsDefault() {
    final SystemConfig config =
        SystemConfigBuilder.builder().withSystemProperties().withEnvironmentVariables().build();
    assertThat(
        config.getStringValue("schemacrawler.test.nonexistent.syscfg", "fallback"), is("fallback"));
  }

  @Test
  public void getStringValueMissingKeyReturnsEmptyStringWhenDefaultNull() {
    final SystemConfig config =
        SystemConfigBuilder.builder().withSystemProperties().withEnvironmentVariables().build();
    // When key not found and defaultValue is null, returns empty string
    assertThat(config.getStringValue("schemacrawler.test.nonexistent.syscfg", null), is(""));
  }
}
