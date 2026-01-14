/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package us.fatehi.utility.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

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
