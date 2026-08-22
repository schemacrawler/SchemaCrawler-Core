/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import us.fatehi.utility.database.JdbcDriverPropertyInfo;

public class JdbcDriverPropertyInfoTest {

  @Test
  public void choicesAreCopiedAndImmutable() {
    final List<String> choices = new ArrayList<>();
    choices.add("a");
    choices.add("b");

    final JdbcDriverPropertyInfo propertyInfo =
        new JdbcDriverPropertyInfo("mode", "connection mode", false, "a", choices);

    choices.add("c");
    assertThat(propertyInfo.choices(), contains("a", "b"));

    assertThrows(UnsupportedOperationException.class, () -> propertyInfo.choices().add("d"));
  }

  @Test
  public void descriptionDefaultsToEmptyWhenBlank() {
    final JdbcDriverPropertyInfo propertyInfo =
        new JdbcDriverPropertyInfo("mode", "  ", false, "default", null);

    assertThat(propertyInfo.getDescription(), is(""));
    assertThat(propertyInfo.getName(), is("mode"));
  }

  @Test
  public void nullChoicesBecomeEmpty() {
    final JdbcDriverPropertyInfo propertyInfo =
        new JdbcDriverPropertyInfo("schema", "schema name", false, "public", null);

    assertThat(propertyInfo.choices(), is(notNullValue()));
    assertThat(propertyInfo.choices().isEmpty(), is(true));
  }

  @Test
  public void passwordLikeNameMasksValue() {
    final JdbcDriverPropertyInfo propertyInfo =
        new JdbcDriverPropertyInfo("dbPassword", "database password", true, "secret", List.of());

    assertThat(propertyInfo.getValue(), is(nullValue()));
  }

  @Test
  public void regularNameRetainsValue() {
    final JdbcDriverPropertyInfo propertyInfo =
        new JdbcDriverPropertyInfo("username", "database user", true, "scott", List.of());

    assertThat(propertyInfo.getValue(), is("scott"));
  }

  @Test
  public void requiresPropertyName() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new JdbcDriverPropertyInfo("  ", "description", false, "value", List.of()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new JdbcDriverPropertyInfo(null, "description", false, "value", List.of()));
  }
}
