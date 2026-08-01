/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import us.fatehi.utility.OptionalBoolean;

public class OptionalBooleanTest {

  @Test
  public void fromBooleanTest() {
    assertThat(OptionalBoolean.fromBoolean(true), is(OptionalBoolean.true_value));
    assertThat(OptionalBoolean.fromBoolean(false), is(OptionalBoolean.false_value));
  }

  @Test
  public void fromOptionalTest() {
    assertThat(OptionalBoolean.fromOptional(null), is(OptionalBoolean.unknown));
    assertThat(OptionalBoolean.fromOptional(Optional.empty()), is(OptionalBoolean.unknown));
    assertThat(OptionalBoolean.fromOptional(Optional.of(true)), is(OptionalBoolean.true_value));
    assertThat(OptionalBoolean.fromOptional(Optional.of(false)), is(OptionalBoolean.false_value));
  }

  @Test
  public void isKnownTest() {
    assertThat(OptionalBoolean.true_value.isKnown(), is(true));
    assertThat(OptionalBoolean.false_value.isKnown(), is(true));
    assertThat(OptionalBoolean.unknown.isKnown(), is(false));
  }

  @Test
  public void toBooleanTest() {
    assertThat(OptionalBoolean.true_value.toBoolean(), is(true));
    assertThat(OptionalBoolean.false_value.toBoolean(), is(false));
  }

  @Test
  public void toBooleanUnknownExceptionTest() {
    final IllegalStateException exception =
        assertThrows(IllegalStateException.class, OptionalBoolean.unknown::toBoolean);
    assertThat(exception.getMessage(), containsString("Connot convert to boolean"));
  }

  @Test
  public void toBooleanWithDefaultTest() {
    assertThat(OptionalBoolean.true_value.toBoolean(false), is(true));
    assertThat(OptionalBoolean.false_value.toBoolean(true), is(false));
    assertThat(OptionalBoolean.unknown.toBoolean(true), is(true));
    assertThat(OptionalBoolean.unknown.toBoolean(false), is(false));
  }

  @Test
  public void toOptionalTest() {
    assertThat(OptionalBoolean.true_value.toOptional(), is(Optional.of(true)));
    assertThat(OptionalBoolean.false_value.toOptional(), is(Optional.of(false)));
    assertThat(OptionalBoolean.unknown.toOptional(), is(Optional.empty()));
  }

  @Test
  public void valuesTest() {
    assertThat(OptionalBoolean.values(), is(notNullValue()));
    assertThat(OptionalBoolean.valueOf("true_value"), is(OptionalBoolean.true_value));
  }
}
