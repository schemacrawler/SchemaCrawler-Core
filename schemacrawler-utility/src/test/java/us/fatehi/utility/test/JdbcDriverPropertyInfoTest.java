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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import us.fatehi.utility.database.JdbcDriverProperty;

public class JdbcDriverPropertyInfoTest {

  @Test
  public void choicesAreCopiedAndImmutable() {
    final List<String> choices = new ArrayList<>();
    choices.add("a");
    choices.add("b");

    final JdbcDriverProperty propertyInfo =
        new JdbcDriverProperty("mode", "connection mode", false, "a", choices);

    choices.add("c");
    assertThat(propertyInfo.choices(), contains("a", "b"));

    assertThrows(UnsupportedOperationException.class, () -> propertyInfo.choices().add("d"));
  }

  @Test
  public void descriptionDefaultsToEmptyWhenBlank() {
    final JdbcDriverProperty propertyInfo =
        new JdbcDriverProperty("mode", "  ", false, "default", null);

    assertThat(propertyInfo.getDescription(), is(""));
    assertThat(propertyInfo.getName(), is("mode"));
  }

  @Test
  public void nullChoicesBecomeEmpty() {
    final JdbcDriverProperty propertyInfo =
        new JdbcDriverProperty("schema", "schema name", false, "public", null);

    assertThat(propertyInfo.choices(), is(notNullValue()));
    assertThat(propertyInfo.choices().isEmpty(), is(true));
  }

  @Test
  public void passwordLikeNameMasksValue() {
    final JdbcDriverProperty propertyInfo =
        new JdbcDriverProperty("dbPassword", "database password", true, "secret", List.of());

    assertThat(propertyInfo.getValue(), is(nullValue()));
  }

  @Test
  public void regularNameRetainsValue() {
    final JdbcDriverProperty propertyInfo =
        new JdbcDriverProperty("username", "database user", true, "scott", List.of());

    assertThat(propertyInfo.getValue(), is("scott"));
  }

  @Test
  public void requiredPropertyFlag() {
    final JdbcDriverProperty propertyInfo =
        new JdbcDriverProperty("username", "database user", true, "scott", List.of());

    assertThat(propertyInfo.isRequired(), is(true));
  }

  @Test
  public void toStringWithDescription() {
    final JdbcDriverProperty propertyInfo =
        new JdbcDriverProperty(
            "username", "database user", true, "scott", List.of("scott", "admin"));
    final String newLine = System.lineSeparator();

    final String expected =
        "username = scott"
            + newLine
            + "database user"
            + newLine
            + "  is required? true"
            + newLine
            + "  choices: [scott, admin]";
    assertThat(propertyInfo.toString(), is(expected));
  }

  @Test
  public void toStringWithoutDescription() {
    final JdbcDriverProperty propertyInfo =
        new JdbcDriverProperty("username", "", false, "scott", List.of());

    assertThat(propertyInfo.toString(), is(containsString("username = scott")));
    assertThat(propertyInfo.toString(), is(containsString("  is required? false")));
    assertThat(propertyInfo.toString(), is(containsString("  choices: []")));
  }

  @Test
  public void uppercasePasswordNameMasksValue() {
    final JdbcDriverProperty propertyInfo =
        new JdbcDriverProperty("PASSWORD", "database password", true, "secret", List.of());

    assertThat(propertyInfo.getValue(), is(nullValue()));
  }

  @Test
  public void requiresPropertyName() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new JdbcDriverProperty("  ", "description", false, "value", List.of()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new JdbcDriverProperty(null, "description", false, "value", List.of()));
  }
}
